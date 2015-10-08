package org.mousephenotype.cda.solr.web.dto;

import org.mousephenotype.cda.db.pojo.Strain;
import org.mousephenotype.cda.enumerations.SexType;
import org.mousephenotype.cda.enumerations.ZygosityType;
import org.mousephenotype.cda.solr.service.dto.BasicBean;
import org.mousephenotype.cda.solr.service.dto.ImpressBaseDTO;
import org.mousephenotype.cda.solr.service.dto.MarkerBean;

import java.util.List;


public class PhenotypeCallSummaryDTO {

	protected Integer id;
	protected SexType sex;
	protected ZygosityType zygosity;
	protected BasicBean datasource;
	protected BasicBean project;
	protected BasicBean organisation;
	protected BasicBean phenotypeTerm;
	protected double pValue = 0;
	protected double effectSize = 0;
	protected Strain strain;
	protected MarkerBean allele;
	protected MarkerBean gene;
	protected ImpressBaseDTO pipeline;
	protected ImpressBaseDTO procedure;
	protected ImpressBaseDTO parameter;
	protected String phenotypingCenter;
	protected double colorIndex;
	protected List<BasicBean> topLevelPhenotypeTerms;
	protected boolean isPreQC;
	protected String gId; // preqc only, param needed for phenoview graph links
	protected String lifeStageAcc;
	protected String lifeStageName;

	/**
	 * @return the gId
	 */
	public String getgId() {

		return gId;
	}

	/**
	 * @param gId the gId to set
	 */
	public void setgId(String gId) {

		this.gId = gId;
	}

	/**
	 * @return the isPreQC
	 */
	public boolean isPreQC() {

		return isPreQC;
	}

	/**
	 * @param isPreQC the isPreQC to set
	 */
	public void setPreQC(boolean isPreQC) {

		this.isPreQC = isPreQC;
	}

	public PhenotypeCallSummaryDTO() {

	}

	public String getPhenotypingCenter(){
		return phenotypingCenter;
	}

	public void setPhenotypingCenter(String phenotypingCenter){
		this.phenotypingCenter = phenotypingCenter;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the datasource
	 */
	public BasicBean getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(BasicBean datasource) {
		this.datasource = datasource;
	}

	/**
	 * @return the project
	 */
	public BasicBean getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(BasicBean project) {
		this.project = project;
	}

	/**
	 * @return the organisation
	 */
	public BasicBean getOrganisation() {
		return organisation;
	}

	/**
	 * @param organisation the organisation to set
	 */
	public void setOrganisation(BasicBean organisation) {
		this.organisation = organisation;
	}

	/**
	 * @return the gene
	 */
	public MarkerBean getGene() {
		return gene;
	}

	/**
	 * @param gene the gene to set
	 */
	public void setGene(MarkerBean gene) {
		this.gene = gene;
	}

	/**
	 * @return the phenotypeTerm
	 */
	public BasicBean getPhenotypeTerm() {
		return phenotypeTerm;
	}

	/**
	 * @param phenotypeTerm the phenotypeTerm to set
	 */
	public void setPhenotypeTerm(BasicBean phenotypeTerm) {
		this.phenotypeTerm = phenotypeTerm;
	}

	/**
	 * @return the strain
	 */
	public Strain getStrain() {
		return strain;
	}

	/**
	 * @param strain the strain to set
	 */
	public void setStrain(Strain strain) {
		this.strain = strain;
	}

	/**
	 * @return the allele
	 */
	public MarkerBean getAllele() {
		return allele;
	}

	/**
	 * @param allele the allele to set
	 */
	public void setAllele(MarkerBean allele) {
		this.allele = allele;
	}

	/**
	 * @return the pipeline
	 */
	public ImpressBaseDTO getPipeline() {
		return pipeline;
	}

	/**
	 * @param pipeline the pipeline to set
	 */
	public void setPipeline(ImpressBaseDTO pipeline) {
		this.pipeline = pipeline;
	}

	/**
	 * @return the procedure
	 */
	public ImpressBaseDTO getProcedure() {
		return procedure;
	}

	/**
	 * @param procedure the procedure to set
	 */
	public void setProcedure(ImpressBaseDTO procedure) {
		this.procedure = procedure;
	}

	/**
	 * @return the parameter
	 */
	public ImpressBaseDTO getParameter() {
		return parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(ImpressBaseDTO parameter) {
		this.parameter = parameter;
	}

	/**
	 * @return the sex
	 */
	public SexType getSex() {
		return sex;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setSex(SexType sex) {
		this.sex = sex;
	}

	/**
	 * @return the zygosity
	 */
	public ZygosityType getZygosity() {
		return zygosity;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setZygosity(ZygosityType zygosity) {
		this.zygosity = zygosity;
	}

	/**
	 * @return the pValue
	 */
	public double getpValue() {
		return pValue;
	}

	/**
	 * @param pValue the pValue to set
	 */
	public void setpValue(double pValue) {
		this.pValue = pValue;
	}

	/**
	 * @return the effectSize
	 */
	public double getEffectSize() {
		return effectSize;
	}

	/**
	 * @param effectSize the effectSize to set
	 */
	public void setEffectSize(double effectSize) {
		this.effectSize = effectSize;
	}

	/**
	 * @return the topLevelPhenotypeTerms
	 */
	public List<BasicBean> getTopLevelPhenotypeTerms() {
		return topLevelPhenotypeTerms;
	}

	/**
	 * @param topLevelPhenotypeTerms the topLevelPhenotypeTerms to set
	 */
	public void setTopLevelPhenotypeTerms(List<BasicBean> topLevelPhenotypeTerms) {
		this.topLevelPhenotypeTerms = topLevelPhenotypeTerms;
	}

	/**
	 * @return the colorIndex
	 */
	public double getColorIndex() {
		return colorIndex;
	}

	/**
	 * @param colorIndex the colorIndex to set
	 */
	public void setColorIndex(double colorIndex) {
		this.colorIndex = colorIndex;
	}

	/**
	 * Return a -Log10 value to generate a scale
	 * @return -Math.log10(pValue)
	 */
	public double getLogValue() {
		if (pValue < 1E-20) {
			return -Math.log10(1E-20);
		}
		return -Math.log10(pValue);
	}

	/**
	 * @return the life stage accession id
	 */
	public String getLifeStageAcc() {

		return lifeStageAcc;
	}

	/**
	 * @param lifeStageAcc the lifeStageAcc to set
	 */
	public void setLifeStageAcc(String lifeStageAcc) {

		this.lifeStageAcc = lifeStageAcc;
	}

	/**
	 * @return the life stage name
	 */
	public String getLifeStageName() {

		return lifeStageName;
	}

	/**
	 * @param lifeStageName the lifeStageName to set
	 */
	public void setLifeStageName(String lifeStageName) {

		this.lifeStageName = lifeStageName;
	}
}
