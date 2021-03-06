package org.mousephenotype.cda.db.statistics;

import org.mousephenotype.cda.db.utilities.SqlUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatisticalResultCategorical implements StatisticalResult, Serializable {

	protected static final long serialVersionUID = 4716400683455025616L;

	public final String insertStatement = "INSERT INTO stats_categorical_results(control_id, control_sex, experimental_id, experimental_sex, experimental_zygosity, external_db_id, project_id, organisation_id, pipeline_id, procedure_id, parameter_id, colony_id, dependent_variable, control_selection_strategy, mp_acc, mp_db_id, male_controls, male_mutants, female_controls, female_mutants, metadata_group, statistical_method, workflow, weight_available, status, category_a, category_b, p_value, effect_size, male_p_value, male_effect_size, female_p_value, female_effect_size, classification_tag, raw_output) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private Double pValue;
	private Double effectSize;
	private Double malePValue;
	private Double maleEffectSize;
	private Double femalePValue;
	private Double femaleEffectSize;
	private String classificationTag;

	/**
	 * Prepare the statement to insert this result into the database
	 *
	 * @param connection connection object to use to save the result
	 * @param generalResult the base result object to populate the common parameters
	 * @throws SQLException
	 */
	public PreparedStatement getSaveResultStatement(Connection connection, LightweightResult generalResult) throws SQLException {

		LightweightCategoricalResult result = (LightweightCategoricalResult) generalResult;
		PreparedStatement s = connection.prepareStatement(insertStatement);

		int i = 1;

		SqlUtils.setSqlParameter(s, result.getControlId(), i++);
		SqlUtils.setSqlParameter(s, result.getSex(), i++);
		SqlUtils.setSqlParameter(s, result.getExperimentalId(), i++);
		SqlUtils.setSqlParameter(s, result.getSex(), i++);
		SqlUtils.setSqlParameter(s, result.getZygosity(), i++);

		SqlUtils.setSqlParameter(s, result.getDataSourceId(), i++);
		SqlUtils.setSqlParameter(s, result.getProjectId(), i++);
		SqlUtils.setSqlParameter(s, result.getOrganisationId(), i++);

		SqlUtils.setSqlParameter(s, result.getPipelineId(), i++);
		SqlUtils.setSqlParameter(s, result.getProcedureId(), i++);
		SqlUtils.setSqlParameter(s, result.getParameterId(), i++);
		SqlUtils.setSqlParameter(s, result.getColonyId(), i++);
		SqlUtils.setSqlParameter(s, result.getDependentVariable(), i++);
		SqlUtils.setSqlParameter(s, result.getControlSelectionMethod().name(), i++);

		SqlUtils.setSqlParameter(s, result.getMpAcc(), i++);
		SqlUtils.setSqlParameter(s, 5, i++); // MP external DB ID = 5
		SqlUtils.setSqlParameter(s, result.getMaleControlCount(), i++);
		SqlUtils.setSqlParameter(s, result.getMaleMutantCount(), i++);
		SqlUtils.setSqlParameter(s, result.getFemaleControlCount(), i++);
		SqlUtils.setSqlParameter(s, result.getFemaleMutantCount(), i++);

		SqlUtils.setSqlParameter(s, result.getMetadataGroup(), i++);
		SqlUtils.setSqlParameter(s, result.getStatisticalMethod(), i++);
		SqlUtils.setSqlParameter(s, result.getWorkflow().toString(), i++);
		SqlUtils.setSqlParameter(s, result.getWeightAvailable().toString(), i++);
		SqlUtils.setSqlParameter(s, result.getStatus(), i++);

		SqlUtils.setSqlParameter(s, (String)null, i++);
		SqlUtils.setSqlParameter(s, (String) null, i++);
		SqlUtils.setSqlParameter(s, this.getpValue(), i++);
		SqlUtils.setSqlParameter(s, this.getEffectSize(), i++);
		SqlUtils.setSqlParameter(s, this.getMalePValue(), i++);
		SqlUtils.setSqlParameter(s, this.getMaleEffectSize(), i++);
		SqlUtils.setSqlParameter(s, this.getFemalePValue(), i++);
		SqlUtils.setSqlParameter(s, this.getFemaleEffectSize(), i++);
		SqlUtils.setSqlParameter(s, this.getClassificationTag(), i++);
		SqlUtils.setSqlParameter(s, result.getAdditionalInformation(), i++);

		return s;
	}

	// Generated methods


	public Double getpValue() {
		return pValue;
	}

	public void setpValue(Double pValue) {
		this.pValue = pValue;
	}

	public Double getEffectSize() {
		return effectSize;
	}

	public void setEffectSize(Double effectSize) {
		this.effectSize = effectSize;
	}

	public Double getMalePValue() {
		return malePValue;
	}

	public void setMalePValue(Double malePValue) {
		this.malePValue = malePValue;
	}

	public Double getMaleEffectSize() {
		return maleEffectSize;
	}

	public void setMaleEffectSize(Double maleEffectSize) {
		this.maleEffectSize = maleEffectSize;
	}

	public Double getFemalePValue() {
		return femalePValue;
	}

	public void setFemalePValue(Double femalePValue) {
		this.femalePValue = femalePValue;
	}

	public Double getFemaleEffectSize() {
		return femaleEffectSize;
	}

	public void setFemaleEffectSize(Double femaleEffectSize) {
		this.femaleEffectSize = femaleEffectSize;
	}

	public String getClassificationTag() {
		return classificationTag;
	}

	public void setClassificationTag(String classificationTag) {
		this.classificationTag = classificationTag;
	}
}
