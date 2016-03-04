/*******************************************************************************
 * Copyright 2015 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 *******************************************************************************/
package org.mousephenotype.cda.indexers;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.mousephenotype.cda.constants.Constants;
import org.mousephenotype.cda.enumerations.BiologicalSampleType;
import org.mousephenotype.cda.enumerations.SexType;
import org.mousephenotype.cda.enumerations.ZygosityType;
import org.mousephenotype.cda.indexers.exceptions.IndexerException;
import org.mousephenotype.cda.indexers.utils.IndexerMap;
import org.mousephenotype.cda.solr.service.dto.ImpressBaseDTO;
import org.mousephenotype.cda.solr.service.dto.ObservationDTOWrite;
import org.mousephenotype.cda.solr.service.dto.ParameterDTO;
import org.mousephenotype.cda.utilities.CommonUtils;
import org.mousephenotype.cda.utilities.RunStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Populate the experiment core
 */
public class ObservationIndexer extends AbstractIndexer {

	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
	private static Connection connection;
	CommonUtils commonUtils = new CommonUtils();

	@Autowired
	@Qualifier("komp2DataSource")
	DataSource komp2DataSource;

	@Autowired
	@Qualifier("observationIndexing")
	SolrServer observationSolrServer;

	Map<String, BiologicalDataBean> biologicalData = new HashMap<>();
	Map<String, BiologicalDataBean> lineBiologicalData = new HashMap<>();

	Map<Integer, ImpressBaseDTO> pipelineMap = new HashMap<>();
	Map<Integer, ImpressBaseDTO> procedureMap = new HashMap<>();
	Map<Integer, ParameterDTO> parameterMap = new HashMap<>();

	Map<Integer, DatasourceBean> datasourceMap = new HashMap<>();
	Map<Integer, DatasourceBean> projectMap = new HashMap<>();
	Map<Integer, List<ParameterAssociationBean>> parameterAssociationMap = new HashMap<>();

	Map<Integer, List<WeightBean>> weightMap = new HashMap<>();
	Map<Integer, WeightBean> ipgttWeightMap = new HashMap<>();

	Map<String, Map<String, String>> translateCategoryNames = new HashMap<>();

	public static final String ipgttWeightParameter = "IMPC_IPG_001_001";
	public static final List<String> maleFertilityParameters = Arrays.asList("IMPC_FER_001_001", "IMPC_FER_006_001", "IMPC_FER_007_001", "IMPC_FER_008_001", "IMPC_FER_009_001");
	public static final List<String> femaleFertilityParameters = Arrays.asList("IMPC_FER_019_001", "IMPC_FER_010_001", "IMPC_FER_011_001", "IMPC_FER_012_001", "IMPC_FER_013_001");

	public ObservationIndexer() {
	}

	@Override
	public RunStatus validateBuild() throws IndexerException {
		return super.validateBuild(observationSolrServer);
	}

	public static void main(String[] args) throws IndexerException {

		RunStatus runStatus = new RunStatus();
		ObservationIndexer main = new ObservationIndexer();
		main.initialise(args, runStatus);
		main.run();
		main.validateBuild();

	}


	@Override
	public void initialise(String[] args, RunStatus runStatus) throws IndexerException {

		super.initialise(args, runStatus);

		try {

			connection = komp2DataSource.getConnection();

			System.out.println("populating pipeline map");
			pipelineMap = IndexerMap.getImpressPipelines(connection);
			System.out.println("populating procedure map");
			procedureMap = IndexerMap.getImpressProcedures(connection);
			System.out.println("populating parameter map");
			parameterMap = IndexerMap.getImpressParameters(connection);

		} catch (SQLException e) {
			e.printStackTrace();
			throw new IndexerException(e);
		}

		printConfiguration();

	}

	@Override
	public RunStatus run() throws IndexerException {
		long count = 0;
		RunStatus runStatus = new RunStatus();
		long start = System.currentTimeMillis();

		try {

			System.out.println("pop datasource map");
			populateDatasourceDataMap();
			System.out.println("pop categorynames map");
			populateCategoryNamesDataMap();
			System.out.println("pop biological data map");
			populateBiologicalDataMap();
			System.out.println("pop line data map");
			populateLineBiologicalDataMap();
			populateParameterAssociationMap();
			System.out.println("pop weight map");
			populateWeightMap();
			System.out.println("pop ipgt map");
			populateIpgttWeightMap();

			count = populateObservationSolrCore(runStatus);

		} catch (SolrServerException | SQLException | IOException e) {
			e.printStackTrace();
			throw new IndexerException(e);
		}

		logger.info(" Added {} total beans in {}", count, commonUtils.msToHms(System.currentTimeMillis() - start));

		return runStatus;
	}

	public long populateObservationSolrCore(RunStatus runStatus) throws SQLException, IOException, SolrServerException {

		int count = 0;

		observationSolrServer.deleteByQuery("*:*");

        String query = "SELECT o.id as id, o.db_id as datasource_id, o.parameter_id as parameter_id, o.parameter_stable_id,\n"
                + "o.observation_type, o.missing, o.parameter_status, o.parameter_status_message,\n"
                + "o.biological_sample_id,\n"
                + "e.project_id as project_id, e.pipeline_id as pipeline_id, e.procedure_id as procedure_id,\n"
                + "e.date_of_experiment, e.external_id, e.id as experiment_id,\n"
                + "e.metadata_combined as metadata_combined, e.metadata_group as metadata_group,\n"
                + "co.category as raw_category,\n"
                + "uo.data_point as unidimensional_data_point,\n"
                + "mo.data_point as multidimensional_data_point,\n"
                + "tso.data_point as time_series_data_point,\n"
                + "tro.text as text_value,\n"
                + "ontE.term_value,\n"
                + "mo.order_index,\n"
                + "mo.dimension,\n"
                + "tso.time_point,\n"
                + "tso.discrete_point,\n"
                + "iro.file_type,\n"
                + "iro.download_file_path\n"
                + "FROM observation o\n"
                + "LEFT OUTER JOIN categorical_observation co ON o.id=co.id\n"
                + "LEFT OUTER JOIN unidimensional_observation uo ON o.id=uo.id\n"
                + "LEFT OUTER JOIN multidimensional_observation mo ON o.id=mo.id\n"
                + "LEFT OUTER JOIN time_series_observation tso ON o.id=tso.id\n"
                + "LEFT OUTER JOIN image_record_observation iro ON o.id=iro.id\n"
                + "LEFT OUTER JOIN text_observation tro ON o.id=tro.id\n"
                +" LEFT OUTER JOIN ontology_entity ontE ON o.id=ontE.ontology_observation_id\n"
                + "INNER JOIN experiment_observation eo ON eo.observation_id=o.id\n"
                + "INNER JOIN experiment e on eo.experiment_id=e.id\n"
                + "WHERE o.missing=0";
        
        

        try (PreparedStatement p = connection.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)) {

	        p.setFetchSize(Integer.MIN_VALUE);
	        System.out.println("runnin main prep statement");
	        ResultSet r = p.executeQuery();
	        System.out.println("executed query");
	        while (r.next()) {

		        ObservationDTOWrite o = new ObservationDTOWrite();
		        o.setId(r.getInt("id"));
		        o.setParameterId(r.getInt("parameter_id"));
		        o.setExperimentId(r.getInt("experiment_id"));
		        o.setExperimentSourceId(r.getString("external_id"));

		        ZonedDateTime dateOfExperiment = null;
		        try {
			        dateOfExperiment = ZonedDateTime.parse(r.getString("date_of_experiment"), DateTimeFormatter.ofPattern(DATETIME_FORMAT).withZone(ZoneId.of("UTC")));
			        o.setDateOfExperiment(dateOfExperiment);
		        } catch (NullPointerException e) {
		        	e.printStackTrace();
			        logger.debug("No date of experiment set for experiment external ID: {}", r.getString("external_id"));
			        o.setDateOfExperiment(null);
		        }


		        o.setParameterId(parameterMap.get(r.getInt("parameter_id")).getId());
		        o.setParameterName(parameterMap.get(r.getInt("parameter_id")).getName());
		        o.setParameterStableId(parameterMap.get(r.getInt("parameter_id")).getStableId());

		        o.setProcedureId(procedureMap.get(r.getInt("procedure_id")).getId());
		        o.setProcedureName(procedureMap.get(r.getInt("procedure_id")).getName());
		        String procedureStableId = procedureMap.get(r.getInt("procedure_id")).getStableId();
		        o.setProcedureStableId(procedureStableId);
		        o.setProcedureGroup(procedureStableId.substring(0, procedureStableId.lastIndexOf("_")));

		        o.setPipelineId(pipelineMap.get(r.getInt("pipeline_id")).getId());
		        o.setPipelineName(pipelineMap.get(r.getInt("pipeline_id")).getName());
		        o.setPipelineStableId(pipelineMap.get(r.getInt("pipeline_id")).getStableId());

		        o.setDataSourceId(datasourceMap.get(r.getInt("datasource_id")).id);
		        o.setDataSourceName(datasourceMap.get(r.getInt("datasource_id")).name);

		        o.setProjectId(projectMap.get(r.getInt("project_id")).id);
		        o.setProjectName(projectMap.get(r.getInt("project_id")).name);

		        o.setMetadataGroup(r.getString("metadata_group"));
		        if (r.wasNull()) {
			        o.setMetadataGroup("");
			        o.setMetadata(new ArrayList<>());
		        }

		        String metadataCombined = r.getString("metadata_combined");
		        if (!r.wasNull()) {
			        o.setMetadata(new ArrayList<>(Arrays.asList(metadataCombined.split("::"))));
		        }

		        // Add the Biological data
		        String bioSampleId = r.getString("biological_sample_id");
		        if (r.wasNull()) {
			        // Line level data

			        BiologicalDataBean b = lineBiologicalData.get(r.getString("experiment_id"));
			        if (b == null) {
				        runStatus.addError(" Cannot find biological data for experiment " + r.getString("experiment_id"));
				        continue;
			        }
			        o.setBiologicalModelId(b.biologicalModelId);
			        o.setGeneAccession(b.geneAcc);
			        o.setGeneSymbol(b.geneSymbol);
			        o.setAlleleAccession(b.alleleAccession);
			        o.setAlleleSymbol(b.alleleSymbol);
			        o.setStrainAccessionId(b.strainAcc);
			        o.setStrainName(b.strainName);
			        o.setGeneticBackground(b.geneticBackground);
			        o.setPhenotypingCenter(b.phenotypingCenterName);
			        o.setPhenotypingCenterId(b.phenotypingCenterId);
			        o.setColonyId(b.colonyId);

			        // Viability applies to both sexes
			        if (o.getParameterStableId().contains("_VIA_")) {
				        o.setSex(SexType.both.getName());
			        } else {
				        // Fertility applies to the sex tested, separate parameters per male//female
				        if (maleFertilityParameters.contains(o.getParameterStableId())) {
					        o.setSex(SexType.male.getName());
				        } else if (femaleFertilityParameters.contains(o.getParameterStableId())) {
					        o.setSex(SexType.female.getName());
				        }
				        if (o.getSex() == null) {
					        o.setSex(SexType.both.getName());
				        }
			        }

			        if (b.zygosity != null) {
				        o.setZygosity(b.zygosity);
			        } else {
				        // Default to hom
				        o.setZygosity(ZygosityType.homozygote.getName());
			        }

			        // All line level parameters are sample group "experimental" due to the nature of the
			        // procedures (i.e. no control mice will go through VIA or FER procedures.)
			        o.setGroup(BiologicalSampleType.experimental.getName());

		        } else {
			        // Specimen level data

			        BiologicalDataBean b = biologicalData.get(bioSampleId);
			        o.setBiologicalModelId(b.biologicalModelId);
			        o.setGeneAccession(b.geneAcc);
			        o.setGeneSymbol(b.geneSymbol);
			        o.setAlleleAccession(b.alleleAccession);
			        o.setAlleleSymbol(b.alleleSymbol);
			        o.setStrainAccessionId(b.strainAcc);
			        o.setStrainName(b.strainName);
			        o.setGeneticBackground(b.geneticBackground);
			        o.setPhenotypingCenter(b.phenotypingCenterName);
			        o.setPhenotypingCenterId(b.phenotypingCenterId);

			        o.setColonyId(b.colonyId);
			        o.setZygosity(b.zygosity);
			        o.setDateOfBirth(b.dateOfBirth);
			        o.setSex(b.sex);
			        o.setGroup(b.sampleGroup);
			        o.setBiologicalSampleId(b.biologicalSampleId);
			        o.setExternalSampleId(b.externalSampleId);
			        o.setDevelopmentStageAcc(b.developmentalStageAcc);
			        o.setDevelopmentStageName(b.developmentalStageName);

			        if (b.productionCenterName!=null){
				        o.setProductionCenter(b.productionCenterName);
			        }
			        if (b.productionCenterId!=null){
				        o.setProductionCenterId(b.productionCenterId);
			        }
			        if (b.litterId!=null){
				        o.setLitterId(b.litterId);
			        }

		        }

		        o.setObservationType(r.getString("observation_type"));

		        String cat = r.getString("raw_category");
		        if (!r.wasNull()) {

			        String param = r.getString("parameter_stable_id");
			        if (translateCategoryNames.containsKey(param)) {

				        String transCat = translateCategoryNames.get(param).get(cat);
				        //System.out.println("param with cat is="+param+" cat="+cat);
				        //System.out.println("transCat="+transCat);
				        if (transCat != null && !transCat.equals("")) {
					        o.setCategory(transCat);
				        } else {
					        o.setCategory(cat);
				        }

			        } else {
				        o.setCategory(cat);
			        }
		        }

		        // Add the correct "data point" for the type
		        switch (r.getString("observation_type")) {
			        case "unidimensional":
				        o.setDataPoint(r.getFloat("unidimensional_data_point"));
				        break;
			        case "multidimensional":
				        o.setDataPoint(r.getFloat("multidimensional_data_point"));
				        break;
			        case "time_series":
				        o.setDataPoint(r.getFloat("time_series_data_point"));
				        break;
		        }

		        Integer order_index = r.getInt("order_index");
		        if (!r.wasNull()) {
			        o.setOrderIndex(order_index);
		        }

		        String dimension = r.getString("dimension");
		        if (!r.wasNull()) {
			        o.setDimension(dimension);
		        }

		        String time_point = r.getString("time_point");
		        if (!r.wasNull()) {
			        o.setTimePoint(time_point);
		        }

		        Float discrete_point = r.getFloat("discrete_point");
		        if (!r.wasNull()) {
			        o.setDiscretePoint(discrete_point);
		        }
		        
		        String text_value = r.getString("text_value");
		        if (!r.wasNull()) {
			        o.setTextValue(text_value);
		        }
		        
		        String term_value = r.getString("term_value");
		        if (!r.wasNull()) {
			        o.setTermValue(term_value);
		        }

		        String file_type = r.getString("file_type");
		        if (!r.wasNull()) {
			        o.setFileType(file_type);
		        }

		        String download_file_path = r.getString("download_file_path");
		        if (!r.wasNull()) {
			        o.setDownloadFilePath(download_file_path);
		        }

		        if (parameterAssociationMap.containsKey(r.getInt("id"))) {
			        for (ParameterAssociationBean pb : parameterAssociationMap.get(r.getInt("id"))) {

				        // Will never be null, we hope
				        o.addParameterAssociationStableId(pb.parameterStableId);
				        o.addParameterAssociationName(pb.parameterAssociationName);
				        if (StringUtils.isNotEmpty(pb.parameterAssociationValue)) {
					        o.addParameterAssociationValue(pb.parameterAssociationValue);
				        }
				        if (StringUtils.isNotEmpty(pb.sequenceId)) {
					        o.addParameterAssociationSequenceId(pb.sequenceId);
				        }

				        if (StringUtils.isNotEmpty(pb.dimId)) {
					        o.addParameterAssociationDimId(pb.dimId);
				        }

			        }
		        }

		        // Add weight parameters only if this observation isn't for a weight parameter
		        if (!Constants.weightParameters.contains(o.getParameterStableId()) && !ipgttWeightParameter.equals(o.getParameterStableId())) {

			        WeightBean b = getNearestWeight(o.getBiologicalSampleId(), dateOfExperiment);

			        if (o.getProcedureGroup().contains("_IPG")) {
				        b = getNearestIpgttWeight(o.getBiologicalSampleId());
			        }

			        if (b != null) {
				        o.setWeight(b.weight);
				        o.setWeightDate(b.date);
				        o.setWeightDaysOld(b.daysOld);
				        o.setWeightParameterStableId(b.parameterStableId);
			        }
		        }

		        // 60 seconds between commits
		        documentCount++;
		        observationSolrServer.addBean(o, 60000);

		        count++;

		        if (count % 200000 == 0) {
			        logger.info(" Added " + count + " beans");
		        }
	        }

	        // Final commit to save the rest of the docs
	        observationSolrServer.commit();

        } catch (Exception e) {
			e.printStackTrace();
	        System.out.println(" Big error :" + e.getMessage());
        }

	    return count;
    }

	/**
	 * Add all the relevant data required quickly looking up biological data associated to a biological sample
	 *
	 * @throws SQLException when a database exception occurs
	 */
	public void populateBiologicalDataMap() throws SQLException {

		String featureFlagMeansQuery = "SELECT column_name FROM information_schema.COLUMNS WHERE TABLE_NAME='biological_sample' AND TABLE_SCHEMA=(SELECT database())";
		Set<String> featureFlags = new HashSet<>();
		try (PreparedStatement p = connection.prepareStatement(featureFlagMeansQuery)) {
			ResultSet r = p.executeQuery();
			while (r.next()) {
				featureFlags.add(r.getString("column_name"));
			}
		}


		String query = "SELECT CAST(bs.id AS CHAR) as biological_sample_id, bs.organisation_id as phenotyping_center_id, "
                + "org.name as phenotyping_center_name, bs.sample_group, bs.external_id as external_sample_id, "
                + "ls.date_of_birth, ls.colony_id, ls.sex as sex, ls.zygosity, ls.developmental_stage_acc, ot.name AS developmental_stage_name, ot.acc as developmental_stage_acc,"
                + "bms.biological_model_id, "
                + "strain.acc as strain_acc, strain.name as strain_name, bm.genetic_background, "
                + "(select distinct allele_acc from biological_model_allele bma WHERE bma.biological_model_id=bms.biological_model_id) as allele_accession, "
                + "(select distinct a.symbol from biological_model_allele bma INNER JOIN allele a on (a.acc=bma.allele_acc AND a.db_id=bma.allele_db_id) WHERE bma.biological_model_id=bms.biological_model_id)  as allele_symbol, "
                + "(select distinct gf_acc from biological_model_genomic_feature bmgf WHERE bmgf.biological_model_id=bms.biological_model_id) as acc, "
                + "(select distinct gf.symbol from biological_model_genomic_feature bmgf INNER JOIN genomic_feature gf on gf.acc=bmgf.gf_acc WHERE bmgf.biological_model_id=bms.biological_model_id)  as symbol ";

			if (featureFlags.contains("production_center_id") && featureFlags.contains("litter_id")) {
				query += ", bs.production_center_id, prod_org.name as production_center_name, bs.litter_id ";
			}

            query += "FROM biological_sample bs "
                + "INNER JOIN organisation org ON bs.organisation_id=org.id "
                + "INNER JOIN live_sample ls ON bs.id=ls.id "
                + "INNER JOIN biological_model_sample bms ON bs.id=bms.biological_sample_id "
                + "INNER JOIN biological_model_strain bmstrain ON bmstrain.biological_model_id=bms.biological_model_id "
                + "INNER JOIN strain strain ON strain.acc=bmstrain.strain_acc "
                + "INNER JOIN biological_model bm ON bm.id = bms.biological_model_id "
                + "INNER JOIN ontology_term ot ON ot.acc=ls.developmental_stage_acc ";

		if (featureFlags.contains("production_center_id") && featureFlags.contains("litter_id")) {
			query += "INNER JOIN organisation prod_org ON bs.organisation_id=prod_org.id ";
		}

		try (PreparedStatement p = connection.prepareStatement(query)) {

			ResultSet resultSet = p.executeQuery();

			while (resultSet.next()) {
				BiologicalDataBean b = new BiologicalDataBean();

				b.alleleAccession = resultSet.getString("allele_accession");
				b.alleleSymbol = resultSet.getString("allele_symbol");
				b.biologicalModelId = resultSet.getInt("biological_model_id");
				b.biologicalSampleId = resultSet.getInt("biological_sample_id");
				b.colonyId = resultSet.getString("colony_id");

				try {
					b.dateOfBirth = ZonedDateTime.parse(resultSet.getString("date_of_birth"), DateTimeFormatter.ofPattern(DATETIME_FORMAT).withZone(ZoneId.of("UTC")));
				} catch (NullPointerException e) {
					e.printStackTrace();
					b.dateOfBirth = null;
					logger.debug("No date of birth set for specimen external ID: {}", resultSet.getString("external_sample_id"));
				}

				b.externalSampleId = resultSet.getString("external_sample_id");
				b.geneAcc = resultSet.getString("acc");
				b.geneSymbol = resultSet.getString("symbol");
				b.phenotypingCenterId = resultSet.getInt("phenotyping_center_id");
				b.phenotypingCenterName = resultSet.getString("phenotyping_center_name");
				b.sampleGroup = resultSet.getString("sample_group");
				b.sex = resultSet.getString("sex");
				b.strainAcc = resultSet.getString("strain_acc");
				b.strainName = resultSet.getString("strain_name");
				b.geneticBackground = resultSet.getString("genetic_background");
				b.zygosity = resultSet.getString("zygosity");
				b.developmentalStageAcc = resultSet.getString("developmental_stage_acc");
				b.developmentalStageName = resultSet.getString("developmental_stage_name");


				if (featureFlags.contains("production_center_id") && featureFlags.contains("litter_id")) {
					b.productionCenterId = resultSet.getInt("production_center_id");
					b.productionCenterName = resultSet.getString("production_center_name");
					b.litterId = resultSet.getString("litter_id");

				}

				biologicalData.put(resultSet.getString("biological_sample_id"), b);
			}
		}
    }

	/**
	 * Add all the relevant data required quickly looking up biological data associated to a biological model (really an
	 * experiment)
	 *
	 * @throws SQLException when a database exception occurs
	 */
	public void populateLineBiologicalDataMap() throws SQLException {

        String query = "SELECT e.id as experiment_id, e.colony_id, e.biological_model_id, "
                + "e.organisation_id as phenotyping_center_id, org.name as phenotyping_center_name, "
                + "strain.acc as strain_acc, strain.name as strain_name, bm.genetic_background, "
                + "(select distinct allele_acc from biological_model_allele bma WHERE bma.biological_model_id=e.biological_model_id) as allele_accession, "
                + "(select distinct a.symbol from biological_model_allele bma INNER JOIN allele a on (a.acc=bma.allele_acc AND a.db_id=bma.allele_db_id) WHERE bma.biological_model_id=e.biological_model_id)  as allele_symbol, "
                + "(select distinct gf_acc from biological_model_genomic_feature bmgf WHERE bmgf.biological_model_id=e.biological_model_id) as acc, "
                + "(select distinct gf.symbol from biological_model_genomic_feature bmgf INNER JOIN genomic_feature gf on gf.acc=bmgf.gf_acc WHERE bmgf.biological_model_id=e.biological_model_id)  as symbol "
                + "FROM experiment e "
                + "INNER JOIN organisation org ON e.organisation_id=org.id "
                + "INNER JOIN biological_model_strain bm_strain ON bm_strain.biological_model_id=e.biological_model_id "
                + "INNER JOIN strain strain ON strain.acc=bm_strain.strain_acc "
                + "INNER JOIN biological_model bm ON bm.id = e.biological_model_id";

	    try (PreparedStatement p = connection.prepareStatement(query)) {

		    ResultSet resultSet = p.executeQuery();

		    while (resultSet.next()) {

			    BiologicalDataBean b = new BiologicalDataBean();

			    b.colonyId = resultSet.getString("colony_id");
			    b.phenotypingCenterId = resultSet.getInt("phenotyping_center_id");
			    b.phenotypingCenterName = resultSet.getString("phenotyping_center_name");
			    b.strainAcc = resultSet.getString("strain_acc");
			    b.strainName = resultSet.getString("strain_name");
			    b.geneticBackground = resultSet.getString("genetic_background");
			    b.alleleAccession = resultSet.getString("allele_accession");
			    b.alleleSymbol = resultSet.getString("allele_symbol");
			    b.biologicalModelId = resultSet.getInt("biological_model_id");
			    b.geneAcc = resultSet.getString("acc");
			    b.geneSymbol = resultSet.getString("symbol");


			    if (b.alleleAccession == null && b.colonyId != null) {
				    // Override the biological model with one that has the
				    // correct gene/allele/strain
				    String query2 = "SELECT DISTINCT bm.id as biological_model_id, " +
                            " (select distinct allele_acc from biological_model_allele bma WHERE bma.biological_model_id=bm.id) as allele_accession, " +
                            " (select distinct a.symbol from biological_model_allele bma INNER JOIN allele a on (a.acc=bma.allele_acc AND a.db_id=bma.allele_db_id) WHERE bma.biological_model_id=bm.id) as allele_symbol, " +
                            " (select distinct gf_acc from biological_model_genomic_feature bmgf WHERE bmgf.biological_model_id=bm.id) as acc, " +
                            " (select distinct gf.symbol from biological_model_genomic_feature bmgf INNER JOIN genomic_feature gf on gf.acc=bmgf.gf_acc WHERE bmgf.biological_model_id=bm.id)  as symbol, " +
                            " strain.acc as strain_acc, strain.name as strain_name, bm.genetic_background " +
                            " FROM live_sample ls " +
                            " INNER JOIN biological_model_sample bms ON bms.biological_sample_id=ls.id " +
                            " INNER JOIN biological_model bm ON bm.id=bms.biological_model_id " +
                            " INNER JOIN biological_model_strain bm_strain ON bm_strain.biological_model_id=bm.id " +
                            " INNER JOIN strain strain ON strain.acc=bm_strain.strain_acc " +
                            " WHERE bm.allelic_composition !='' AND ls.colony_id = ? LIMIT 1 " ;
	                try (PreparedStatement p2 = connection.prepareStatement(query2)) {
		                p2.setString(1, resultSet.getString("colony_id"));
		                ResultSet resultSet2 = p2.executeQuery();
		                resultSet2.next();
		                b.strainAcc = resultSet2.getString("strain_acc");
		                b.strainName = resultSet2.getString("strain_name");
		                b.geneticBackground = resultSet2.getString("genetic_background");
		                b.alleleAccession = resultSet2.getString("allele_accession");
		                b.alleleSymbol = resultSet2.getString("allele_symbol");
		                b.biologicalModelId = resultSet2.getInt("biological_model_id");
		                b.geneAcc = resultSet2.getString("acc");
		                b.geneSymbol = resultSet2.getString("symbol");
	                }
                }


	            lineBiologicalData.put(resultSet.getString("experiment_id"), b);
            }
        }
    }

	/**
	 * Add all the relevant data required for translating the category names in the cases where the category names are
	 * numerals, but the actual name is in the description field
	 *
	 * @throws SQLException when a database exception occurs
	 */
	public void populateCategoryNamesDataMap() throws SQLException {

		String query = "SELECT pp.stable_id, ppo.name, ppo.description FROM phenotype_parameter pp "
			+ "INNER JOIN phenotype_parameter_lnk_option pplo ON pp.id=pplo.parameter_id "
			+ "INNER JOIN phenotype_parameter_option ppo ON ppo.id=pplo.option_id "
			+ "WHERE ppo.name NOT REGEXP '^[a-zA-Z]' AND ppo.description!='' ";

		try (PreparedStatement p = connection.prepareStatement(query)) {

			ResultSet resultSet = p.executeQuery();

			while (resultSet.next()) {

				String stableId = resultSet.getString("stable_id");
				logger.debug(" parameter_stable_id for numeric category: {}", stableId);

				if (!translateCategoryNames.containsKey(stableId)) {
					translateCategoryNames.put(stableId, new HashMap<>());
				}

				String name = resultSet.getString("name");
				String description = resultSet.getString("description");
				if (name.matches("[0-9]+")) {

					// add .0 onto string as this is what our numerical categories look in solr!!!!
					name += ".0";

					translateCategoryNames.get(stableId).put(name, description);
				} else {
					logger.debug(" Not translating non alphabetical category for parameter: " + stableId + ", name: " + name + ", desc:" + description);
				}

			}
		}
	}

	public void populateParameterAssociationMap() throws SQLException {

		Map<String, String> stableIdToNameMap = this.getAllParameters();
		String query = "SELECT id, observation_id, parameter_id, sequence_id, dim_id, parameter_association_value FROM parameter_association";

		try (PreparedStatement p = connection.prepareStatement(query)) {

			ResultSet resultSet = p.executeQuery();

			while (resultSet.next()) {

				Integer obsId = resultSet.getInt("observation_id");

				ParameterAssociationBean pb = new ParameterAssociationBean();
				pb.observationId = obsId;
				pb.parameterStableId = resultSet.getString("parameter_id");
				pb.parameterAssociationValue = resultSet.getString("parameter_association_value");
				if (stableIdToNameMap.get(pb.parameterStableId) != null) {
					pb.parameterAssociationName = stableIdToNameMap.get(pb.parameterStableId);
				}
				pb.sequenceId = resultSet.getString("sequence_id");
				pb.dimId = resultSet.getString("dim_id");

				if (!parameterAssociationMap.containsKey(obsId)) {
					parameterAssociationMap.put(obsId, new ArrayList<>());
				}

				parameterAssociationMap.get(obsId).add(pb);
			}
		}

	}

	/**
	 * Return all parameter stable ids and names
	 *
	 * @throws SQLException When a database error occurrs
	 */
	public Map<String, String> getAllParameters() throws SQLException {
		Map<String, String> parameters = new HashMap<>();

		String query = "SELECT stable_id, name FROM komp2.phenotype_parameter";

		try (PreparedStatement statement = getConnection().prepareStatement(query)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				parameters.put(resultSet.getString("stable_id"), resultSet.getString("name"));
			}
		}

		return parameters;
	}

	public void populateDatasourceDataMap() throws SQLException {

		List<String> queries = new ArrayList<>();
		queries.add("SELECT id, short_name as name, 'DATASOURCE' as datasource_type FROM external_db");
		queries.add("SELECT id, name, 'PROJECT' as datasource_type FROM project");

		for (String query : queries) {

			try (PreparedStatement p = connection.prepareStatement(query)) {

				ResultSet resultSet = p.executeQuery();

				while (resultSet.next()) {

					DatasourceBean b = new DatasourceBean();

					b.id = resultSet.getInt("id");
					b.name = resultSet.getString("name");

					switch (resultSet.getString("datasource_type")) {
						case "DATASOURCE":
							datasourceMap.put(resultSet.getInt("id"), b);
							break;
						case "PROJECT":
							projectMap.put(resultSet.getInt("id"), b);
							break;
					}
				}
			}
		}
	}


	/**
	 * Compare all weight dates to select the nearest to the date of experiment
	 *
	 * @param specimenID       the specimen
	 * @param dateOfExperiment the date
	 * @return the nearest weight bean to the date of the experiment
	 */
	public WeightBean getNearestWeight(Integer specimenID, ZonedDateTime dateOfExperiment) {

		WeightBean nearest = null;

		if (dateOfExperiment != null && weightMap.containsKey(specimenID)) {

			for (WeightBean candidate : weightMap.get(specimenID)) {

				if (nearest == null) {
					nearest = candidate;
					continue;
				}

				if (Math.abs(dateOfExperiment.toInstant().toEpochMilli() - candidate.date.toInstant().toEpochMilli()) < Math.abs(dateOfExperiment.toInstant().toEpochMilli() - nearest.date.toInstant().toEpochMilli())) {
					nearest = candidate;
				}
			}
		}

		// Do not return weight that is > 4 days away from the experiment
		// since the weight of the specimen become less and less relevant
		// (Heuristic from Natasha Karp @ WTSI)
		// 4 days = 345,600,000 ms
		if (nearest != null && Math.abs(dateOfExperiment.toInstant().toEpochMilli() - nearest.date.toInstant().toEpochMilli()) > 3.456E8) {
			nearest = null;
		}
		return nearest;
	}

	/**
	 * Select date of experiment
	 *
	 * @param specimenID the specimen
	 * @return the nearest weight bean to the date of the experiment
	 */
	public WeightBean getNearestIpgttWeight(Integer specimenID) {

		WeightBean nearest = null;

		if (ipgttWeightMap.containsKey(specimenID)) {
			nearest = ipgttWeightMap.get(specimenID);
		}

		return nearest;
	}

    /**
     * Return map of specimen ID => List of all weights ordered by date ASC
     *
     * @exception SQLException When a database error occurs
     */
    public void populateWeightMap() throws SQLException {

        int count=0;

        String query = "SELECT\n" +
            "  o.biological_sample_id, \n" +
            "  data_point AS weight, \n" +
            "  parameter_stable_id, \n" +
            "  date_of_experiment, \n" +
            "  datediff(date_of_experiment, ls.date_of_birth) as days_old, \n" +
            "  e.organisation_id \n" +
            "FROM observation o \n" +
            "  INNER JOIN unidimensional_observation uo ON uo.id = o.id \n" +
            "  INNER JOIN live_sample ls ON ls.id=o.biological_sample_id \n" +
            "  INNER JOIN experiment_observation eo ON o.id = eo.observation_id \n" +
            "  INNER JOIN experiment e ON e.id = eo.experiment_id \n" +
            "WHERE parameter_stable_id IN ("+StringUtils.join(Constants.weightParameters, ",")+") AND data_point > 0" +
            "  ORDER BY biological_sample_id, date_of_experiment ASC \n" ;

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
	        ResultSet resultSet = statement.executeQuery();
	        while (resultSet.next()) {

		        WeightBean b = new WeightBean();
		        try {
			        b.date = ZonedDateTime.parse(resultSet.getString("date_of_experiment"), DateTimeFormatter.ofPattern(DATETIME_FORMAT).withZone(ZoneId.of("UTC")));
		        } catch (NullPointerException e) {
		        	e.printStackTrace();
			        b.date = null;
			        logger.debug("No date of experiment set for sample id {} parameter {}", resultSet.getString("biological_sample_id"), resultSet.getString("parameter_stable_id"));
		        }

		        b.weight = resultSet.getFloat("weight");
		        b.parameterStableId = resultSet.getString("parameter_stable_id");
		        b.daysOld = resultSet.getInt("days_old");

		        final Integer specimenId = resultSet.getInt("biological_sample_id");

		        if (!weightMap.containsKey(specimenId)) {
			        weightMap.put(specimenId, new ArrayList<>());
		        }

		        weightMap.get(specimenId).add(b);
		        count += 1;
	        }
        }

	    logger.info(" Added {} specimen weight data map entries", count, weightMap.size());
    }

    /**
     * Return map of specimen ID => weight for
     *
     * @exception SQLException When a database error occurrs
     */
    public void populateIpgttWeightMap() throws SQLException {

        String query = "SELECT o.biological_sample_id, data_point AS weight, parameter_stable_id, " +
            "date_of_experiment, DATEDIFF(date_of_experiment, ls.date_of_birth) AS days_old " +
            "FROM observation o " +
            "  INNER JOIN unidimensional_observation uo ON uo.id = o.id " +
            "  INNER JOIN live_sample ls ON ls.id=o.biological_sample_id " +
            "  INNER JOIN experiment_observation eo ON o.id = eo.observation_id " +
            "  INNER JOIN experiment e ON e.id = eo.experiment_id " +
            "WHERE parameter_stable_id = '"+ ipgttWeightParameter +"' " ;

	    try (PreparedStatement statement = getConnection().prepareStatement(query)) {
		    ResultSet resultSet = statement.executeQuery();
		    while (resultSet.next()) {

			    WeightBean b = new WeightBean();
			    try {
				    b.date = ZonedDateTime.parse(resultSet.getString("date_of_experiment"), DateTimeFormatter.ofPattern(DATETIME_FORMAT).withZone(ZoneId.of("UTC")));
			    } catch (NullPointerException e) {
			    	e.printStackTrace();
				    b.date = null;
				    logger.debug("No date of experiment set for sample id {} parameter {}", resultSet.getString("biological_sample_id"), resultSet.getString("parameter_stable_id"));

			    }
			    b.weight = resultSet.getFloat("weight");
			    b.parameterStableId = resultSet.getString("parameter_stable_id");
			    b.daysOld = resultSet.getInt("days_old");

			    final Integer specimenId = resultSet.getInt("biological_sample_id");
			    ipgttWeightMap.put(specimenId, b);
		    }
	    }

    }


	public static Connection getConnection() {
		return connection;
	}

	public Map<String, Map<String, String>> getTranslateCategoryNames() {
		return translateCategoryNames;
	}

	public Map<String, BiologicalDataBean> getLineBiologicalData() {
		return lineBiologicalData;
	}

	public Map<String, BiologicalDataBean> getBiologicalData() {
		return biologicalData;
	}

	public Map<Integer, DatasourceBean> getDatasourceMap() {
		return datasourceMap;
	}

	public Map<Integer, DatasourceBean> getProjectMap() {
		return projectMap;
	}

	public Map<Integer, List<WeightBean>> getWeightMap() {
		return weightMap;
	}


	/**
	 * Internal class to act as Map value DTO for biological data
	 */
	protected class BiologicalDataBean {

		public String alleleAccession;
		public String alleleSymbol;
		public Integer biologicalModelId;
		public Integer biologicalSampleId;
		public String colonyId;
		public ZonedDateTime dateOfBirth;
		public String externalSampleId;
		public String geneAcc;
		public String geneSymbol;
		public String phenotypingCenterName;
		public Integer phenotypingCenterId;
		public String sampleGroup;
		public String sex;
		public String strainAcc;
		public String strainName;
		public String geneticBackground;
		public String zygosity;
		public String developmentalStageAcc;
		public String developmentalStageName;
		public String productionCenterName;
		public Integer productionCenterId;
		public String litterId;

	}

	/**
	 * Internal class to act as Map value DTO for weight data
	 */
	protected class WeightBean {
		public String parameterStableId;
		public ZonedDateTime date;
		public Float weight;
		public Integer daysOld;

		@Override
		public String toString() {
			return "WeightBean{" +
				"parameterStableId='" + parameterStableId + '\'' +
				", date=" + date +
				", weight=" + weight +
				", daysOld=" + daysOld +
				'}';
		}
	}

	/**
	 * Internal class to act as Map value DTO for datasource data
	 */
	protected class DatasourceBean {

		public Integer id;
		public String name;
	}

	/**
	 * Internal class to act as Map value DTO for datasource data
	 */
	protected class ParameterAssociationBean {

		public String parameterAssociationName;
		public String parameterAssociationValue;
		public Integer id;
		public Integer observationId;
		public String parameterStableId;
		public String sequenceId;
		public String dimId;
	}

}
