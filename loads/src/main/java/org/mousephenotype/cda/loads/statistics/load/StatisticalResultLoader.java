package org.mousephenotype.cda.loads.statistics.load;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang.math.NumberUtils;
import org.mousephenotype.cda.enumerations.BatchClassification;
import org.mousephenotype.cda.enumerations.ControlStrategy;
import org.mousephenotype.cda.enumerations.SexType;
import org.mousephenotype.cda.solr.service.BasicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@Import(value = {StatisticalResultLoaderConfig.class})
public class StatisticalResultLoader extends BasicService implements CommandLineRunner {



    final private Logger logger = LoggerFactory.getLogger(getClass());
    final private DataSource komp2DataSource;


    Map<String, NameIdDTO> organisationMap = new HashMap<>();
    Map<String, NameIdDTO> pipelineMap = new HashMap<>();
    Map<String, NameIdDTO> procedureMap = new HashMap<>();
    Map<String, NameIdDTO> parameterMap = new HashMap<>();

    Map<String, Integer> datasourceMap = new HashMap<>();
    Map<String, Integer> projectMap = new HashMap<>();
    Map<String, String> colonyAlleleMap = new HashMap<>();


    void populateColonyAlleleMap() throws SQLException {
        Map map = colonyAlleleMap;

        String query = "SELECT DISTINCT colony_id, allele_acc " +
                "FROM live_sample ls " +
                "INNER JOIN biological_model_sample bms ON bms.biological_sample_id=ls.id " +
                "INNER JOIN biological_model_allele bma ON bma.biological_model_id=bms.biological_model_id " +
                "INNER JOIN allele a ON a.acc=bma.allele_acc " ;

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                map.put(r.getString("colony_id"), r.getString("allele_acc"));
            }
        }

        logger.info(" Mapped {} datasource entries", map.size());
    }

    void populateDatasourceMap() throws SQLException {
        Map map = datasourceMap;

        String query = "SELECT * FROM external_db";

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                map.put(r.getString("short_name"), r.getInt("id"));
            }
        }

        logger.info(" Mapped {} datasource entries", map.size());
    }

    void populateProjectMap() throws SQLException {
        Map map = projectMap;

        String query = "SELECT * FROM project";

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                map.put(r.getString("name"), r.getInt("id"));
            }
        }

        logger.info(" Mapped {} project entries", map.size());
    }



    void populateOrganisationMap() throws SQLException {
        Map map = organisationMap;

        // Populate the organisation map with this query
        String query = "SELECT * FROM organisation";

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                map.put(r.getString("name"), new NameIdDTO(r.getInt("id"), r.getString("name")));
            }
        }

        logger.info(" Mapped {} organisation entries", map.size());
    }

    void populatePipelineMap() throws SQLException {
        Map map = pipelineMap;

        String query = "SELECT * FROM phenotype_pipeline";

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                map.put(r.getString("stable_id"), new NameIdDTO(r.getInt("id"), r.getString("name"), r.getString("stable_id")));
            }
        }

        logger.info(" Mapped {} pipeline entries", map.size());
    }

    void populateProcedureMap() throws SQLException {
        Map map = procedureMap;

        String query = "SELECT * FROM phenotype_procedure ORDER BY id";

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                String procGroup = r.getString("stable_id");
                map.put(procGroup, new NameIdDTO(r.getInt("id"), r.getString("name"), r.getString("stable_id")));
            }
        }

        logger.info(" Mapped {} procedure entries", map.size());
    }

    void populateParameterMap() throws SQLException {
        Map map = parameterMap;

        String query = "SELECT * FROM phenotype_parameter";

        try (Connection connection = komp2DataSource.getConnection(); PreparedStatement p = connection.prepareStatement(query)) {
            ResultSet r = p.executeQuery();
            while (r.next()) {
                map.put(r.getString("stable_id"), new NameIdDTO(r.getInt("id"), r.getString("name"), r.getString("stable_id")));
            }
        }

        logger.info(" Mapped {} parameter entries", map.size());
    }



    private String fileLocation;

    @Inject
    public StatisticalResultLoader(@Named("komp2DataSource") DataSource komp2DataSource) {
        Assert.notNull(komp2DataSource, "Komp2 datasource cannot be null");
        this.komp2DataSource = komp2DataSource;
    }



    private boolean isValidInt(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private Double getDoubleField(String str) {
        return (NumberUtils.isNumber(str)) ? Double.parseDouble(str) : null;
    }

    private Integer getIntegerField(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length == 0) {
            return null;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return null;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
        }

        return Integer.parseInt(str);
    }

    private String getStringField(String str) {

        if (str.isEmpty() || str.equals("NA")) {
            return "";
        }

        return str;
    }

    /**
     * Process a string from a results file into a LineStatisticalResult object
     *
     * field list
     0	metadata_group
     1	zygosity
     2	colony_id
     3	depvar
     4	status
     5	code
     6	count cm
     7	count cf
     8	count mm
     9	count mf
     10	mean cm
     11	mean cf
     12	mean mm
     13	mean mf
     14	control_strategy
     15	workflow
     16	weight available
     17	Method
     18	Dependent variable
     19	Batch included
     20	Residual variances homogeneity
     21	Genotype contribution
     22	Genotype estimate
     23	Genotype standard error
     24	Genotype p-Val
     25	Genotype percentage change
     26	Sex estimate
     27	Sex standard error
     28	Sex p-val
     29	Weight estimate
     30	Weight standard error
     31	Weight p-val
     32	Gp1 genotype
     33	Gp1 Residuals normality test
     34	Gp2 genotype
     35	Gp2 Residuals normality test
     36	Blups test
     37	Rotated residuals normality test
     38	Intercept estimate
     39	Intercept standard error
     40	Interaction included
     41	Interaction p-val
     42	Sex FvKO estimate
     43	Sex FvKO standard error
     44	Sex FvKO p-val
     45	Sex MvKO estimate
     46	Sex MvKO standard error
     47	Sex MvKO p-val
     48	Classification tag
     49	Additional information
     * @param data a line from a statistical results file
     * @throws IOException
     */
    LineStatisticalResult getResult(String data) {

        if (data.contains("metadata_group")) {
            // This is a header
            return null;
        }

        LineStatisticalResult result = new LineStatisticalResult();


        // Several attributes come from the filename
        String filename = Paths.get(fileLocation).getFileName().toString().replaceAll("\\.result", "");
        List<String> fileMetaData = Arrays.asList(filename.trim().split("::"));

        if ( ! filename.contains("::")) {
            String dataString = filename.replaceAll("M-G-P", "M+G+P");
            fileMetaData = Arrays.stream(dataString.trim().split("-")).map(x -> x.replaceAll("M+G+P", "M-G-P")).collect(Collectors.toList());
        }

        String dataSource = fileMetaData.get(0);
        String project    = fileMetaData.get(1);
        String center     = fileMetaData.get(2);
        String pipeline   = fileMetaData.get(3);
        String procedure  = fileMetaData.get(4);
        String strain     = fileMetaData.get(5);

        // Strain in the filename does not include a ":"
        if (strain.contains("MGI")) {
            strain = strain.replaceAll("MGI", "MGI:");

        }

        result.setDataSource(dataSource);
        result.setProject(project);
        result.setCenter(center);
        result.setPipeline(pipeline);
        result.setProcedure(procedure);
        result.setStrain(strain);


        String [] fields = data.replace(System.getProperty("line.separator"), "").split("\t", -1);
        System.out.println(org.apache.commons.lang.StringUtils.join(fields, ","));


        result.setMetadataGroup( getStringField(fields[0]) );
        result.setZygosity( getStringField(fields[1]) );
        result.setColonyId( getStringField(fields[2]) );
        result.setDependentVariable( getStringField(fields[3]) );

        StatusCode status = StatusCode.valueOf(fields[4]);
        result.setStatus( status.name() );

        result.setCode( getStringField(fields[5]) );
        result.setCountControlMale( getIntegerField(fields[6]) );
        result.setCountControlFemale( getIntegerField(fields[7]) );
        result.setCountMutantMale( getIntegerField(fields[8]) );
        result.setCountMutantFemale( getIntegerField(fields[9]) );

        result.setMaleControlMean ( getDoubleField(fields[10]) );
        result.setFemaleControlMean ( getDoubleField(fields[11]) );
        result.setMaleMutantMean ( getDoubleField(fields[12]) );
        result.setFemaleMutantMean ( getDoubleField(fields[13]) );

        result.setControlSelection( getStringField(fields[14]) );
        result.setWorkflow( getStringField(fields[15]) );
        result.setWeightAvailable( getStringField(fields[16]) );
        result.setStatisticalMethod( getStringField(fields[17]) );

        // fields[18] is a duplicate of fields[3]


        if (fields.length < 40) {
            return result;
        }

        switch(status) {
            case TESTED:
                // Result was processed successfully by PhenStat, load the result object

                // Vector output results from PhenStat start at field 19
                int i = 19;

                result.setBatchIncluded( getStringField(fields[i++]) );
                result.setResidualVariancesHomogeneity( getStringField(fields[i++]) );
                result.setGenotypeContribution( getStringField(fields[i++]) );
                result.setGenotypeEstimate( getStringField(fields[i++]) );
                result.setGenotypeStandardError( getStringField(fields[i++]) );
                result.setGenotypePVal( getStringField(fields[i++]) );
                result.setGenotypePercentageChange( getStringField(fields[i++]) );
                result.setSexEstimate( getStringField(fields[i++]) );
                result.setSexStandardError( getStringField(fields[i++]) );
                result.setSexPVal( getStringField(fields[i++]) );
                result.setWeightEstimate( getStringField(fields[i++]) );
                result.setWeightStandardError( getStringField(fields[i++]) );
                result.setWeightPVal( getStringField(fields[i++]) );
                result.setGroup1Genotype( getStringField(fields[i++]) );
                result.setGroup1ResidualsNormalityTest( getStringField(fields[i++]) );
                result.setGroup2Genotype( getStringField(fields[i++]) );
                result.setGroup2ResidualsNormalityTest( getStringField(fields[i++]) );
                result.setBlupsTest( getStringField(fields[i++]) );
                result.setRotatedResidualsNormalityTest( getStringField(fields[i++]) );
                result.setInterceptEstimate( getStringField(fields[i++]) );
                result.setInterceptStandardError( getStringField(fields[i++]) );
                result.setInteractionIncluded( getStringField(fields[i++]) );
                result.setInteractionPVal( getStringField(fields[i++]) );
                result.setSexFvKOEstimate( getStringField(fields[i++]) );
                result.setSexFvKOStandardError( getStringField(fields[i++]) );
                result.setSexFvKOPVal( getStringField(fields[i++]) );
                result.setSexMvKOEstimate( getStringField(fields[i++]) );
                result.setSexMvKOStandardError( getStringField(fields[i++]) );
                result.setSexMvKOPVal( getStringField(fields[i++]) );
                result.setClassificationTag( getStringField(fields[i++]) );
                result.setAdditionalInformation( getStringField(fields[i++]) );

                logger.debug("Last iteration left index i at: ", i);

                break;
            case FAILED:
                // Result failed to be processed by PhenStat
                break;
            default:
                break;
        }

        return result;
    }


    private enum StatusCode {
            TESTED,
            FAILED;
    }

    private class NameIdDTO {

        int dbId;
        String name;
        String stableId;

        public NameIdDTO(int dbId, String name) {
            this.dbId = dbId;
            this.name = name;
        }

        public NameIdDTO(int dbId, String name, String stableId) {
            this.dbId = dbId;
            this.name = name;
            this.stableId = stableId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDbId() {
            return dbId;
        }

        public void setDbId(int dbId) {
            this.dbId = dbId;
        }

        public String getStableId() {
            return stableId;
        }

        public void setStableId(String stableId) {
            this.stableId = stableId;
        }
    }


    /**
     * Generate the data set to process
     *
     * @param data    The data result from a stats analysis
     * @return result object partially populated with basic information
     */
    public LightweightUnidimensionalResult getBaseResult(LineStatisticalResult data) {

        if (data == null) {
            return null;
        }

        NameIdDTO center = organisationMap.get(data.getCenter());
        NameIdDTO pipeline = pipelineMap.get(data.getPipeline());
        NameIdDTO procedure = pipelineMap.get(data.getProcedure()); // Procedure group e.g. IMPC_CAL
        NameIdDTO parameter = parameterMap.get(data.getDependentVariable());
        ControlStrategy strategy = ControlStrategy.valueOf(data.getControlSelection());

        // result contains a "statistical result" that has the
        // ability to produce a PreparedStatement ready for database insertion
        LightweightUnidimensionalResult result = new LightweightUnidimensionalResult();

        result.setMetadataGroup(data.getMetadataGroup());

        result.setDataSourceId(datasourceMap.get(data.getDataSourceName()));
        result.setProjectId(projectMap.get(data.getProjectName()));

        result.setOrganisationId(center.getDbId());
        result.setOrganisationName(center.getName());

        result.setPipelineId(pipeline.getDbId());
        result.setPipelineStableId(pipeline.getStableId());

        result.setProcedureId(procedure.getDbId());
        result.setProcedureGroup(data.getProcedure());

        result.setParameterId(parameter.getDbId());
        result.setParameterStableId(parameter.getStableId());
        result.setDependentVariable(parameter.getStableId());

        result.setColonyId(data.getColonyId());
        result.setStrain(data.getStrain());
        result.setZygosity(data.getZygosity());

        result.setExperimentalZygosity(data.getZygosity());

        result.setControlSelectionMethod(strategy);

        // TODO: Lookup from specimen?
//        result.setControlId(data.getControlBiologicalModelId());
//        result.setExperimentalId(data.getMutantBiologicalModelId());

        // Lookup from colony ID
        result.setAlleleAccessionId(colonyAlleleMap.get(data.getColonyId()));

        result.setMaleControlCount(data.getCountControlMale());
        result.setMaleMutantCount(data.getCountMutantMale());
        result.setFemaleControlCount(data.getCountControlFemale());
        result.setFemaleMutantCount(data.getCountMutantFemale());

        result.setFemaleControlMean(data.getFemaleControlMean());
        result.setFemaleExperimentalMean(data.getFemaleMutantMean());
        result.setMaleControlMean(data.getMaleControlMean());
        result.setMaleExperimentalMean(data.getMaleMutantMean());

        Set<String> sexes = new HashSet<>();
        if (data.getCountMutantMale()>3) {
            sexes.add("male");
        }
        if (data.getCountMutantFemale()>3) {
            sexes.add("female");
        }

        // Set the sex(es) of the result set
        result.setSex(SexType.both.getName());
        if (sexes.size() == 1) {
            result.setSex(new ArrayList<String>(sexes).get(0));
        }

        BatchClassification batches = BatchClassification.valueOf(data.getWorkflow());
        result.setWorkflow(batches);
        result.setWeightAvailable(data.getWeightAvailable() != null && data.getWeightAvailable().equals("TRUE"));

        result.setCalculationTimeNanos(0L);

        return result;
    }


    private void processFile(String loc) throws IOException {

        for (String line : Files.readAllLines(Paths.get(loc))) {

            LightweightUnidimensionalResult result = getBaseResult(getResult(line));

            if (result == null) {
                // Skipping record
                continue;
            }

            try (Connection connection = komp2DataSource.getConnection()) {

                result.getStatisticalResult().getSaveResultStatement(connection, result);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void run(String... strings) throws Exception {

        logger.info("Starting statistical result loader");

        // Populate lookups
        populateOrganisationMap();
        populatePipelineMap();
        populateProcedureMap();
        populateParameterMap();
        populateDatasourceMap();
        populateProjectMap();
        populateColonyAlleleMap();


        // parameter to indicate the location of the result file(s)
        OptionParser parser = new OptionParser();
        parser.accepts("location").withRequiredArg().ofType(String.class).isRequired();
        OptionSet options = parser.parse(strings);
        if ( ! options.hasArgument("location") ) {
            logger.error("location argument missing");
            return;
        }
        fileLocation = (String) options.valuesOf("location").get(0);

        // If the location is a single file, parse it
        boolean regularFile = Files.isRegularFile(Paths.get(fileLocation));
        boolean directory = Files.isDirectory(Paths.get(fileLocation));

        if (regularFile) {

            // process the file
            processFile(fileLocation);

        } else if (directory) {

            // process all files in the directoy
            try (Stream<Path> paths = Files.walk(Paths.get(fileLocation), 1)) {
                for (Path path : paths.collect(Collectors.toList())) {
                    if (path.endsWith("result")) {
                        if ( ! Files.isRegularFile(path)) {
                            logger.warn("File " + path + " is not a regular file");
                            continue;
                        }

                        processFile(path.toString());
                    }
                }
            }
        } else {
            logger.warn("File " + fileLocation + " is not a regular file or a directory");
        }


    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StatisticalResultLoader.class, args);
    }



}