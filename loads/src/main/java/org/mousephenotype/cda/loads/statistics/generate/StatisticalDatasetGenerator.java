package org.mousephenotype.cda.loads.statistics.generate;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.mousephenotype.cda.enumerations.BiologicalSampleType;
import org.mousephenotype.cda.enumerations.ObservationType;
import org.mousephenotype.cda.solr.SolrUtils;
import org.mousephenotype.cda.solr.service.BasicService;
import org.mousephenotype.cda.solr.service.dto.ImpressDTO;
import org.mousephenotype.cda.solr.service.dto.ObservationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate input files for batch stats processing with PhenStat
 * Read from solr cores and produce files for all parameters to be analyzed
 *
 * @author Andrew Tikhonov
 * @author Jeremy Mason
 *
 */
@Import(value = {StatisticalDatasetGeneratorConfig.class})
public class StatisticalDatasetGenerator extends BasicService implements CommandLineRunner {

    final private Logger logger = LoggerFactory.getLogger(getClass());
    final private SolrClient experimentCore;
    final private SolrClient pipelineCore;


    @Inject
    public StatisticalDatasetGenerator(
            @Named("experimentCore") SolrClient experimentCore,
            @Named("pipelineCore") SolrClient pipelineCore) {
        Assert.notNull(experimentCore, "Experiment core cannot be null");
        Assert.notNull(pipelineCore, "Pipeline core cannot be null");

        this.experimentCore = experimentCore;
        this.pipelineCore = pipelineCore;
    }


    private final static List<String> PIVOT = Arrays.asList(
            ObservationDTO.DATASOURCE_NAME,
            ObservationDTO.PROJECT_NAME,
            ObservationDTO.PHENOTYPING_CENTER,
            ObservationDTO.PIPELINE_STABLE_ID,
            ObservationDTO.PROCEDURE_GROUP,
            ObservationDTO.STRAIN_ACCESSION_ID);

    @Override
    public void run(String... strings) throws Exception {

        logger.info("Starting statistical dataset generation");

        logger.info("Populating normal category lookup");
        Map<String, String> normalEyeCategory = getNormalEyeCategories();


        try {


            Map<String, SortedSet<String>> parameters = getParameterMap();
            logger.info("Prepared " + parameters.keySet().size() + " procedure groups");

             SolrQuery query = new SolrQuery();
            query.setQuery("*:*")

                    // Filter out line level parameters
                    .addFilterQuery("-procedure_group:(IMPC_VIA OR IMPC_FER OR IMPC_EVL OR IMPC_EVM OR IMPC_EVO OR IMPC_EVP)")

                    // Only processing categorical and unidimensional parameters
                    .addFilterQuery("observation_type:(categorical OR unidimensional)")

                    // Filter out incorrect M-G-P pipeline bodyweight
                    .addFilterQuery("-parameter_stable_id:M-G-P_022_001_001")

                    // Filter out IMM results unwil we have normalised parameters
                    .addFilterQuery("-parameter_stable_id:*_IMM_*")

                    // Include only parameters for which we have experimental data
                    .addFilterQuery("biological_sample_group:experimental")

                    .setRows(0)
                    .setFacet(true)
                    .setFacetLimit(-1)
                    .addFacetPivotField(PIVOT.stream().collect(Collectors.joining(",")));

            logger.info(SolrUtils.getBaseURL(experimentCore) + "/select" + query.toQueryString());
            List<Map<String, String>> results = getFacetPivotResults(experimentCore.query(query), false);

            logger.info("Processing {} data sets", results.size());

            int i = 1;
            for (Map<String, String> result : results) {

                if (parameters.get(result.get(ObservationDTO.PROCEDURE_GROUP)) == null) {
                    logger.info("  Skipping procedure {} -- not in parameters map (no parameters to annotate)", result.get(ObservationDTO.PROCEDURE_STABLE_ID));
                    continue;
                }

                String filename = "tsvs/" + Stream.of(
                        result.get(ObservationDTO.DATASOURCE_NAME).replace(" ","_"),
                        result.get(ObservationDTO.PROJECT_NAME).replace(" ","_"),
                        result.get(ObservationDTO.PHENOTYPING_CENTER).replace(" ","_"),
                        result.get(ObservationDTO.PIPELINE_STABLE_ID),
                        result.get(ObservationDTO.PROCEDURE_GROUP),
                        result.get(ObservationDTO.STRAIN_ACCESSION_ID).replace(":","")).collect(Collectors.joining("::")) + ".tsv";
                Path p = new File(filename).toPath();
                logger.info("Writing file {} ({})", filename, p);

                logger.info("Processing {} {} {} {} {}", i++, result.get(ObservationDTO.PHENOTYPING_CENTER), result.get(ObservationDTO.PIPELINE_STABLE_ID), result.get(ObservationDTO.PROCEDURE_GROUP), result.get(ObservationDTO.STRAIN_ACCESSION_ID));

                String fields = "date_of_experiment,external_sample_id,strain_name,allele_accession_id,gene_accession_id,gene_symbol,weight,sex,zygosity,biological_sample_group,colony_id,metadata_group,parameter_stable_id,parameter_name,data_point,category,observation_type";

                query = new SolrQuery();
                query.setQuery("*:*")
                        .addFilterQuery(ObservationDTO.PARAMETER_STABLE_ID + ":(" + parameters.get(result.get(ObservationDTO.PROCEDURE_GROUP)).stream().collect(Collectors.joining(" OR ")) + ")")
                        .addFilterQuery(ObservationDTO.PHENOTYPING_CENTER + ":\"" + result.get(ObservationDTO.PHENOTYPING_CENTER) + "\"")
                        .addFilterQuery(ObservationDTO.PIPELINE_STABLE_ID + ":" + result.get(ObservationDTO.PIPELINE_STABLE_ID))
                        .addFilterQuery(ObservationDTO.PROCEDURE_GROUP + ":" + result.get(ObservationDTO.PROCEDURE_GROUP))
                        .addFilterQuery(ObservationDTO.STRAIN_ACCESSION_ID + ":\"" + result.get(ObservationDTO.STRAIN_ACCESSION_ID) + "\"")
                        .addFilterQuery("observation_type:(categorical OR unidimensional)")
                        .setFields(fields)
                        .setRows(Integer.MAX_VALUE)
                ;

                logger.debug(SolrUtils.getBaseURL(experimentCore) + "/select" + query.toQueryString());

                List<ObservationDTO> observationDTOs = experimentCore.query(query).getBeans(ObservationDTO.class);

                // Populate specimen parameter map
                Map<String, Map<String, String>> specimenParameterMap = new HashMap<>();
                for (ObservationDTO observationDTO : observationDTOs) {

                    String colonyId = observationDTO.getGroup().equals(BiologicalSampleType.control.getName()) ? "+/+" : observationDTO.getColonyId();
                    String zygosity = observationDTO.getGroup().equals(BiologicalSampleType.control.getName()) ? "" : observationDTO.getZygosity();
                    String alleleAccessionId = observationDTO.getGroup().equals(BiologicalSampleType.control.getName()) ? "" : observationDTO.getAlleleAccession();
                    String geneAccessionId = observationDTO.getGroup().equals(BiologicalSampleType.control.getName()) ? "" : observationDTO.getGeneAccession();
                    String specimenId = observationDTO.getExternalSampleId();
                    String dateOfExperiment = observationDTO.getDateOfExperimentString();
                    String weight = observationDTO.getWeight()!=null ? observationDTO.getWeight().toString() : "";
                    String sex = observationDTO.getSex();
                    String biologicalSampleGroup = observationDTO.getGroup();
                    String strainName = observationDTO.getStrainName();
                    String metadataGroup = observationDTO.getMetadataGroup();

                    String key = Stream.of(specimenId, biologicalSampleGroup, strainName, colonyId, geneAccessionId, alleleAccessionId, dateOfExperiment, metadataGroup, zygosity, weight, sex).collect(Collectors.joining("\t"));

                    if ( ! specimenParameterMap.containsKey(key)) {
                        specimenParameterMap.put(key, new HashMap<>());
                    }

                    String dataValue;

                    switch (ObservationType.valueOf(observationDTO.getObservationType())) {
                        case categorical:
                            dataValue = observationDTO.getCategory();
                            break;
                        case unidimensional:
                            dataValue = observationDTO.getDataPoint().toString();
                            break;
                        default:
                            // No value
                            continue;
                    }

                    specimenParameterMap.get(key).put(observationDTO.getParameterStableId(), dataValue);


                    // Add a column for the MAPPED category for EYE parameters
                    if (ObservationType.valueOf(observationDTO.getObservationType()) == ObservationType.categorical &&
                            (
                                observationDTO.getParameterStableId().toUpperCase().contains("_EYE_") ||
                                observationDTO.getParameterStableId().toUpperCase().contains("M-G-P_014") ||
                                observationDTO.getParameterStableId().toUpperCase().contains("ESLIM_014")
                            )
                        ) {

                        // Get mapped data category
                        String mappedDataValue = observationDTO.getCategory();
                        switch (observationDTO.getCategory()) {

                            case "imageOnly":
                            case "no data":
                            case "no data for both eyes":
                            case "No data":
                            case "not defined":
                            case "unobservable":
                                mappedDataValue = "";
                                break;

                            case "no data left eye":
                            case "no data right eye":
                                // Map to normal category
                                mappedDataValue = normalEyeCategory.get(observationDTO.getParameterStableId());
                                break;

                            case "no data left eye, present right eye":
                                mappedDataValue = "present right eye";
                                break;

                            case "no data right eye, present left eye":
                                mappedDataValue = "present left eye";
                                break;

                            case "no data left eye, right eye abnormal":
                                mappedDataValue = "right eye abnormal";
                                break;

                            case "no data right eye, left eye abnormal":
                                mappedDataValue = "left eye abnormal";
                                break;

                            default:
                                break;
                        }

                        String mappedCategory = observationDTO.getParameterStableId() + "_MAPPED";
                        specimenParameterMap.get(key).put(mappedCategory, mappedDataValue);

                    }


                }

                logger.info("  Has {} specimens with {} parameters", specimenParameterMap.size(), specimenParameterMap.values().stream().mapToInt(value -> value.keySet().size()).sum());
                if (specimenParameterMap.size() < 5) {
                    logger.info("  Not processing due to low N", specimenParameterMap.size(), specimenParameterMap.values().stream().mapToInt(value -> value.keySet().size()).sum());
                    continue;
                }

                List<String> headers = new ArrayList<>(Arrays.asList("specimen_id", "group", "background_strain_name", "colony_id", "marker_accession_id", "allele_accession_id", "batch", "metadata_group", "zygosity", "weight", "sex", "::"));

                final SortedSet<String> sortedParameters = parameters.get(result.get(ObservationDTO.PROCEDURE_GROUP));
                headers.addAll(sortedParameters);

                List<List<String>> lines = new ArrayList<>();
                lines.add(headers);

               for (String key : specimenParameterMap.keySet()) {

                    // If the specimen doesn't have any parameters associated, skip it
                    if (specimenParameterMap.get(key).values().size() < 1) {
                        continue;
                    }

                    Map<String, String> data = specimenParameterMap.get(key);

                    List<String> line = new ArrayList<>();
                    line.addAll(Arrays.asList(key.split("\t")));
                    line.add("::"); // Separator column

                    for (String parameter : sortedParameters) {
                        line.add(data.getOrDefault(parameter, ""));
                    }

                    lines.add(line);

                }

                // Create directory if not exists
                File d = new File("tsvs");
                if (!d.exists()) {
                    d.mkdir();
                }

                StringBuilder sb = new StringBuilder();
                for (List<String> line : lines) {
                    sb.append(line.stream().collect(Collectors.joining("\t")));
                    sb.append("\n");
                }

                Files.write(p, sb.toString().getBytes());


            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Get list of "normal" category
     * @return map of categories
     */
    private Map<String,String> getNormalEyeCategories() {

    Map<String,String> map = new HashMap<>();

        map.put("ESLIM_014_001_001", "normal");
        map.put("ESLIM_014_001_003", "normal");
        map.put("ESLIM_014_001_004", "absent");
        map.put("ESLIM_014_001_005", "normal");
        map.put("ESLIM_014_001_006", "normal");
        map.put("ESLIM_014_001_007", "normal");
        map.put("ESLIM_014_001_008", "absent");
        map.put("ESLIM_014_001_009", "normal");
        map.put("ESLIM_014_001_010", "normal");
        map.put("ESLIM_014_001_011", "normal");
        map.put("ESLIM_014_001_012", "normal");
        map.put("ESLIM_014_001_013", "normal");
        map.put("ESLIM_014_001_014", "normal");
        map.put("ESLIM_014_001_015", "normal");
        map.put("IMPC_EYE_001_001",  "present");
        map.put("IMPC_EYE_002_001",  "absent");
        map.put("IMPC_EYE_003_001",  "absent");
        map.put("IMPC_EYE_004_001",  "normal");
        map.put("IMPC_EYE_005_001",  "normal");
        map.put("IMPC_EYE_006_001",  "normal");
        map.put("IMPC_EYE_007_001",  "normal");
        map.put("IMPC_EYE_008_001",  "absent");
        map.put("IMPC_EYE_009_001",  "absent");
        map.put("IMPC_EYE_010_001",  "normal");
        map.put("IMPC_EYE_011_001",  "normal");
        map.put("IMPC_EYE_012_001",  "normal");
        map.put("IMPC_EYE_013_001",  "normal");
        map.put("IMPC_EYE_014_001",  "normal");
        map.put("IMPC_EYE_015_001",  "normal");
        map.put("IMPC_EYE_016_001",  "normal");
        map.put("IMPC_EYE_017_001",  "absent");
        map.put("IMPC_EYE_018_001",  "absent");
        map.put("IMPC_EYE_019_001",  "absent");
        map.put("IMPC_EYE_020_001",  "normal");
        map.put("IMPC_EYE_021_001",  "normal");
        map.put("IMPC_EYE_022_001",  "normal");
        map.put("IMPC_EYE_023_001",  "normal");
        map.put("IMPC_EYE_024_001",  "normal");
        map.put("IMPC_EYE_025_001",  "normal");
        map.put("IMPC_EYE_026_001",  "normal");
        map.put("IMPC_EYE_027_001",  "absent");
        map.put("IMPC_EYE_080_001",  "absent");
        map.put("IMPC_EYE_081_001",  "absent");
        map.put("IMPC_EYE_082_001",  "normal");
        map.put("IMPC_EYE_083_001",  "normal");
        map.put("IMPC_EYE_084_001",  "absent");
        map.put("IMPC_EYE_085_001",  "absent");
        map.put("IMPC_EYE_086_001",  "absent");
        map.put("M-G-P_014_001_001", "normal");
        map.put("M-G-P_014_001_003", "normal");
        map.put("M-G-P_014_001_004", "absent");
        map.put("M-G-P_014_001_005", "normal");
        map.put("M-G-P_014_001_006", "normal");
        map.put("M-G-P_014_001_007", "normal");
        map.put("M-G-P_014_001_008", "absent");
        map.put("M-G-P_014_001_009", "normal");
        map.put("M-G-P_014_001_010", "normal");
        map.put("M-G-P_014_001_011", "normal");
        map.put("M-G-P_014_001_012", "normal");
        map.put("M-G-P_014_001_013", "normal");
        map.put("M-G-P_014_001_014", "normal");
        map.put("M-G-P_014_001_015", "normal");

        return map;
    }

    /**
     * Gets a map of procedure group -> SortedSet(paramter stable IDs) for each procedure group
     * The parameters that are not to be annotated have been removed
     *
     * @return Map of procedure group key to a Set of parameter stable IDs from IMPReSS
     */
    private Map<String, SortedSet<String>> getParameterMap() throws SolrServerException, IOException {

        Map<String, SortedSet<String>> parameters = new HashMap<>();

        SolrQuery query = new SolrQuery()
            .setQuery("*:*")
                .addFilterQuery("annotate:true")
                .addFilterQuery("observation_type:(categorical OR unidimensional)")
            .setFields(ImpressDTO.PROCEDURE_STABLE_ID, ImpressDTO.PARAMETER_STABLE_ID)
            .setRows(Integer.MAX_VALUE);

        pipelineCore
            .query(query)
            .getBeans(ImpressDTO.class)
            .forEach(x -> {

                final String procedureStableId = x.getProcedureStableId();
                final String procedureGroup = procedureStableId.substring(0, procedureStableId.lastIndexOf("_"));

                if ( ! parameters.containsKey(procedureGroup)) {
                    // Use an ordered set (TreeSet) to keep the parameters sorted low to high
                    parameters.put(procedureGroup, new TreeSet<>());
                }

                parameters.get(procedureGroup).add(x.getParameterStableId());

                // Add another column to the EYE procedures to store the mapped categories (or slit lamp for legacy procedures)
                // as agreed at the 20170824 Dev call
                if (x.getParameterStableId().toUpperCase().contains("IMPC_EYE") ||
                        procedureGroup.toUpperCase().contains("M-G-P_014") ||
                        procedureGroup.toUpperCase().contains("ESLIM_014")) {
                    String mappedParameter = x.getParameterStableId() + "_MAPPED";
                    parameters.get(procedureGroup).add(mappedParameter);

                }


            });

        return parameters;
    }

    public static void main(String[] args) {
        SpringApplication.run(StatisticalDatasetGenerator.class, args);
    }


}