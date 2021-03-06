package org.mousephenotype.cda.db.statistics;


import org.mousephenotype.cda.db.pojo.PhenotypeAnnotationType;
import org.mousephenotype.cda.enumerations.SexType;
import org.mousephenotype.cda.enumerations.ZygosityType;

public class ResultDTO {
    protected Long         resultId;
    protected Long         dataSourceId;
    protected Long         projectId;
    protected Long         centerId;
    protected String       centerName;
    protected SexType      sex;
    protected String       colonyId;
    protected ZygosityType zygosity;
    protected Double       nullTestPvalue;
    protected Double       genotypeEffectPvalue;
    protected Double       genotypeEffectSize;
    protected Double       femalePvalue;
    protected Double       femaleEffectSize;
    protected Double       malePvalue;
    protected Double       maleEffectSize;
    protected Integer      femaleControls;
    protected Integer      femaleMutants;
    protected Integer      maleControls;
    protected Integer      maleMutants;
    protected Long         procedureId;
    protected Long         pipelineId;
    protected Long         parameterId;
    protected String       geneAcc;
    protected Long         geneDbId;
    protected String       alleleAcc;
    protected Long         alleleDbId;
    protected String       strainAcc;
    protected Long         strainDbId;
    protected String       categoryA;
    protected String       categoryB;
    protected String       mpTerm;
    protected Long         ontologyDbId;

    protected String backgroundStrainName;
    protected String pipelineStableId;
    protected String procedureGroup;
    protected String parameterStableId;
    protected String procedureName;
    protected String parameterName;
    protected String geneSymbol;


    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getBackgroundStrainName() {
        return backgroundStrainName;
    }

    public void setBackgroundStrainName(String backgroundStrainName) {
        this.backgroundStrainName = backgroundStrainName;
    }

    public String getPipelineStableId() {
        return pipelineStableId;
    }

    public void setPipelineStableId(String pipelineStableId) {
        this.pipelineStableId = pipelineStableId;
    }

    public String getProcedureGroup() {
        return procedureGroup;
    }

    public void setProcedureGroup(String procedureGroup) {
        this.procedureGroup = procedureGroup;
    }

    public String getParameterStableId() {
        return parameterStableId;
    }

    public void setParameterStableId(String parameterStableId) {
        this.parameterStableId = parameterStableId;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Long getOntologyDbId() {
        return ontologyDbId;
    }

    public void setOntologyDbId(Long ontologyDbId) {
        this.ontologyDbId = ontologyDbId;
    }

    public String getMpTerm() {

        return mpTerm;
    }


    public void setMpTerm(String mpTerm) {

        this.mpTerm = mpTerm;
    }


    public ResultDTO() {
        // default
    }

    public ResultDTO(LightweightResult r) {

        if (r instanceof LightweightUnidimensionalResult) {
            processUnidimensionalResult((LightweightUnidimensionalResult)r);
    }
        if (r instanceof LightweightCategoricalResult) {
            processCategoricalResult((LightweightCategoricalResult)r);
        }

    }


    public ResultDTO(LightweightUnidimensionalResult r) {
        processUnidimensionalResult(r);


    }

    private void processUnidimensionalResult(LightweightUnidimensionalResult r) {
        if (r.getSex() != null) {
            this.sex = SexType.valueOf(r.getSex());
        }
        if (r.getZygosity() != null) {
            this.zygosity = ZygosityType.valueOf(r.getZygosity());
        }
        this.femaleControls = r.getFemaleControlCount();
        this.femaleMutants = r.getFemaleMutantCount();
        this.maleControls = r.getMaleControlCount();
        this.maleMutants = r.getMaleMutantCount();
        this.pipelineId = r.getPipelineId();
        this.parameterId = r.getParameterId();

        if (r.getStatisticalResult() instanceof StatisticalResultMixedModel) {
            StatisticalResultMixedModel rmm = (StatisticalResultMixedModel) r.getStatisticalResult();
            this.nullTestPvalue = rmm.getNullTestSignificance();
            this.genotypeEffectPvalue = rmm.getGenotypeEffectPValue();
            this.genotypeEffectSize = rmm.getGenotypeParameterEstimate();
            this.femalePvalue = rmm.getGenderFemaleKoPValue();
            this.femaleEffectSize = rmm.getGenderFemaleKoEstimate();
            this.malePvalue = rmm.getGenderMaleKoPValue();
            this.maleEffectSize = rmm.getGenderMaleKoEstimate();

        } else if (r.getStatisticalResult() instanceof StatisticalResultReferenceRangePlus) {

            StatisticalResultReferenceRangePlus rmm = (StatisticalResultReferenceRangePlus) r.getStatisticalResult();

            Float genotypeLow = rmm.getEffect(null, PhenotypeAnnotationType.decreased);
            Float genotypeHigh = rmm.getEffect(null, PhenotypeAnnotationType.increased);
            Float maleLow = rmm.getEffect(SexType.male, PhenotypeAnnotationType.decreased);
            Float maleHigh = rmm.getEffect(SexType.male, PhenotypeAnnotationType.increased);
            Float femaleLow = rmm.getEffect(SexType.female, PhenotypeAnnotationType.decreased);
            Float femaleHigh = rmm.getEffect(SexType.female, PhenotypeAnnotationType.increased);

            if (genotypeLow != null && genotypeHigh != null) {
                if (genotypeLow > genotypeHigh) {
                    this.nullTestPvalue = (double) rmm.getPvalue(null, PhenotypeAnnotationType.decreased);
                    this.genotypeEffectPvalue = this.nullTestPvalue;
                    this.genotypeEffectSize = (double) -genotypeLow;
                } else if (genotypeLow < genotypeHigh) {
                    this.nullTestPvalue = (double) rmm.getPvalue(null, PhenotypeAnnotationType.increased);
                    this.genotypeEffectPvalue = this.nullTestPvalue;
                    this.genotypeEffectSize = (double) genotypeHigh;
                }
            }

            if (maleLow != null && maleHigh != null) {
                if (maleLow > maleHigh) {
                    this.malePvalue = (double) rmm.getPvalue(SexType.male, PhenotypeAnnotationType.decreased);
                    this.maleEffectSize = (double) -maleLow;
                } else if (maleLow < maleHigh) {
                    this.malePvalue = (double) rmm.getPvalue(SexType.male, PhenotypeAnnotationType.increased);
                    this.maleEffectSize = (double) maleHigh;
                }
            }

            if (femaleLow != null && femaleHigh != null) {
                if (femaleLow > femaleHigh) {
                    this.femalePvalue = (double) rmm.getPvalue(SexType.female, PhenotypeAnnotationType.decreased);
                    this.femaleEffectSize = (double) -femaleLow;
                } else if (femaleLow < femaleHigh) {
                    this.femalePvalue = (double) rmm.getPvalue(SexType.female, PhenotypeAnnotationType.increased);
                    this.femaleEffectSize = (double) femaleHigh;
                }
            }

        }
    }

    public ResultDTO(LightweightCategoricalResult r) {
        processCategoricalResult(r);
    }

    private void processCategoricalResult(LightweightCategoricalResult r) {
        if (r.getSex() != null) {
            this.sex = SexType.valueOf(r.getSex());
        }
        if (r.getZygosity() != null) {
            this.zygosity = ZygosityType.valueOf(r.getZygosity());
        }
        this.categoryA = r.getCategoryA();
        this.categoryB = r.getCategoryB();
        this.nullTestPvalue = r.getpValue();
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public SexType getSex() {
        return sex;
    }

    public void setSex(SexType sex) {
        this.sex = sex;
    }

    public String getColonyId() {
        return colonyId;
    }

    public void setColonyId(String colonyId) {
        this.colonyId = colonyId;
    }

    public ZygosityType getZygosity() {
        return zygosity;
    }

    public void setZygosity(ZygosityType zygosity) {
        this.zygosity = zygosity;
    }

    public Double getNullTestPvalue() {
        return nullTestPvalue;
    }

    public void setNullTestPvalue(Double nullTestPvalue) {
        this.nullTestPvalue = nullTestPvalue;
    }

    public Double getGenotypeEffectPvalue() {
        return genotypeEffectPvalue;
    }

    public void setGenotypeEffectPvalue(Double genotypeEffectPvalue) {
        this.genotypeEffectPvalue = genotypeEffectPvalue;
    }

    public Double getGenotypeEffectSize() {
        return genotypeEffectSize;
    }

    public void setGenotypeEffectSize(Double genotypeEffectSize) {
        this.genotypeEffectSize = genotypeEffectSize;
    }

    public Double getFemalePvalue() {
        return femalePvalue;
    }

    public void setFemalePvalue(Double femalePvalue) {
        this.femalePvalue = femalePvalue;
    }

    public Double getFemaleEffectSize() {
        return femaleEffectSize;
    }

    public void setFemaleEffectSize(Double femaleEffectSize) {
        this.femaleEffectSize = femaleEffectSize;
    }

    public Double getMalePvalue() {
        return malePvalue;
    }

    public void setMalePvalue(Double malePvalue) {
        this.malePvalue = malePvalue;
    }

    public Double getMaleEffectSize() {
        return maleEffectSize;
    }

    public void setMaleEffectSize(Double maleEffectSize) {
        this.maleEffectSize = maleEffectSize;
    }

    public Integer getFemaleControls() {
        return femaleControls;
    }

    public void setFemaleControls(Integer femaleControls) {
        this.femaleControls = femaleControls;
    }

    public Integer getFemaleMutants() {
        return femaleMutants;
    }

    public void setFemaleMutants(Integer femaleMutants) {
        this.femaleMutants = femaleMutants;
    }

    public Integer getMaleControls() {
        return maleControls;
    }

    public void setMaleControls(Integer maleControls) {
        this.maleControls = maleControls;
    }

    public Integer getMaleMutants() {
        return maleMutants;
    }

    public void setMaleMutants(Integer maleMutants) {
        this.maleMutants = maleMutants;
    }

    public Long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Long procedureId) {
        this.procedureId = procedureId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getParameterId() {
        return parameterId;
    }

    public void setParameterId(Long parameterId) {
        this.parameterId = parameterId;
    }

    public String getGeneAcc() {
        return geneAcc;
    }

    public void setGeneAcc(String geneAcc) {
        this.geneAcc = geneAcc;
    }

    public Long getGeneDbId() {
        return geneDbId;
    }

    public void setGeneDbId(Long geneDbId) {
        this.geneDbId = geneDbId;
    }

    public String getAlleleAcc() {
        return alleleAcc;
    }

    public void setAlleleAcc(String alleleAcc) {
        this.alleleAcc = alleleAcc;
    }

    public Long getAlleleDbId() {
        return alleleDbId;
    }

    public void setAlleleDbId(Long alleleDbId) {
        this.alleleDbId = alleleDbId;
    }

    public String getStrainAcc() {
        return strainAcc;
    }

    public void setStrainAcc(String strainAcc) {
        this.strainAcc = strainAcc;
    }

    public Long getStrainDbId() {
        return strainDbId;
    }

    public void setStrainDbId(Long strainDbId) {
        this.strainDbId = strainDbId;
    }

    public String getCategoryA() {
        return categoryA;
    }

    public void setCategoryA(String categoryA) {
        this.categoryA = categoryA;
    }

    public String getCategoryB() {
        return categoryB;
    }

    public void setCategoryB(String categoryB) {
        this.categoryB = categoryB;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ResultDTO)) return false;

        ResultDTO resultDTO = (ResultDTO) o;

        if (alleleAcc != null ? !alleleAcc.equals(resultDTO.alleleAcc) : resultDTO.alleleAcc != null) return false;
        if (alleleDbId != null ? !alleleDbId.equals(resultDTO.alleleDbId) : resultDTO.alleleDbId != null) return false;
        if (categoryA != null ? !categoryA.equals(resultDTO.categoryA) : resultDTO.categoryA != null) return false;
        if (categoryB != null ? !categoryB.equals(resultDTO.categoryB) : resultDTO.categoryB != null) return false;
        if (centerId != null ? !centerId.equals(resultDTO.centerId) : resultDTO.centerId != null) return false;
        if (colonyId != null ? !colonyId.equals(resultDTO.colonyId) : resultDTO.colonyId != null) return false;
        if (dataSourceId != null ? !dataSourceId.equals(resultDTO.dataSourceId) : resultDTO.dataSourceId != null)
            return false;
        if (femaleControls != null ? !femaleControls.equals(resultDTO.femaleControls) : resultDTO.femaleControls != null)
            return false;
        if (femaleEffectSize != null ? !femaleEffectSize.equals(resultDTO.femaleEffectSize) : resultDTO.femaleEffectSize != null)
            return false;
        if (femaleMutants != null ? !femaleMutants.equals(resultDTO.femaleMutants) : resultDTO.femaleMutants != null)
            return false;
        if (femalePvalue != null ? !femalePvalue.equals(resultDTO.femalePvalue) : resultDTO.femalePvalue != null)
            return false;
        if (geneAcc != null ? !geneAcc.equals(resultDTO.geneAcc) : resultDTO.geneAcc != null) return false;
        if (geneDbId != null ? !geneDbId.equals(resultDTO.geneDbId) : resultDTO.geneDbId != null) return false;
        if (genotypeEffectPvalue != null ? !genotypeEffectPvalue.equals(resultDTO.genotypeEffectPvalue) : resultDTO.genotypeEffectPvalue != null)
            return false;
        if (genotypeEffectSize != null ? !genotypeEffectSize.equals(resultDTO.genotypeEffectSize) : resultDTO.genotypeEffectSize != null)
            return false;
        if (maleControls != null ? !maleControls.equals(resultDTO.maleControls) : resultDTO.maleControls != null)
            return false;
        if (maleEffectSize != null ? !maleEffectSize.equals(resultDTO.maleEffectSize) : resultDTO.maleEffectSize != null)
            return false;
        if (maleMutants != null ? !maleMutants.equals(resultDTO.maleMutants) : resultDTO.maleMutants != null)
            return false;
        if (malePvalue != null ? !malePvalue.equals(resultDTO.malePvalue) : resultDTO.malePvalue != null) return false;
        if (mpTerm != null ? !mpTerm.equals(resultDTO.mpTerm) : resultDTO.mpTerm != null) return false;
        if (nullTestPvalue != null ? !nullTestPvalue.equals(resultDTO.nullTestPvalue) : resultDTO.nullTestPvalue != null)
            return false;
        if (parameterId != null ? !parameterId.equals(resultDTO.parameterId) : resultDTO.parameterId != null)
            return false;
        if (pipelineId != null ? !pipelineId.equals(resultDTO.pipelineId) : resultDTO.pipelineId != null) return false;
        if (procedureId != null ? !procedureId.equals(resultDTO.procedureId) : resultDTO.procedureId != null)
            return false;
        if (projectId != null ? !projectId.equals(resultDTO.projectId) : resultDTO.projectId != null) return false;
        if (resultId != null ? !resultId.equals(resultDTO.resultId) : resultDTO.resultId != null) return false;
        if (sex != resultDTO.sex) return false;
        if (strainAcc != null ? !strainAcc.equals(resultDTO.strainAcc) : resultDTO.strainAcc != null) return false;
        if (strainDbId != null ? !strainDbId.equals(resultDTO.strainDbId) : resultDTO.strainDbId != null) return false;
        if (zygosity != resultDTO.zygosity) return false;

        return true;
    }


    @Override
    public int hashCode() {

        int result = resultId != null ? resultId.hashCode() : 0;
        result = 31 * result + (dataSourceId != null ? dataSourceId.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (centerId != null ? centerId.hashCode() : 0);
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (colonyId != null ? colonyId.hashCode() : 0);
        result = 31 * result + (zygosity != null ? zygosity.hashCode() : 0);
        result = 31 * result + (nullTestPvalue != null ? nullTestPvalue.hashCode() : 0);
        result = 31 * result + (genotypeEffectPvalue != null ? genotypeEffectPvalue.hashCode() : 0);
        result = 31 * result + (genotypeEffectSize != null ? genotypeEffectSize.hashCode() : 0);
        result = 31 * result + (femalePvalue != null ? femalePvalue.hashCode() : 0);
        result = 31 * result + (femaleEffectSize != null ? femaleEffectSize.hashCode() : 0);
        result = 31 * result + (malePvalue != null ? malePvalue.hashCode() : 0);
        result = 31 * result + (maleEffectSize != null ? maleEffectSize.hashCode() : 0);
        result = 31 * result + (femaleControls != null ? femaleControls.hashCode() : 0);
        result = 31 * result + (femaleMutants != null ? femaleMutants.hashCode() : 0);
        result = 31 * result + (maleControls != null ? maleControls.hashCode() : 0);
        result = 31 * result + (maleMutants != null ? maleMutants.hashCode() : 0);
        result = 31 * result + (procedureId != null ? procedureId.hashCode() : 0);
        result = 31 * result + (pipelineId != null ? pipelineId.hashCode() : 0);
        result = 31 * result + (parameterId != null ? parameterId.hashCode() : 0);
        result = 31 * result + (geneAcc != null ? geneAcc.hashCode() : 0);
        result = 31 * result + (geneDbId != null ? geneDbId.hashCode() : 0);
        result = 31 * result + (alleleAcc != null ? alleleAcc.hashCode() : 0);
        result = 31 * result + (alleleDbId != null ? alleleDbId.hashCode() : 0);
        result = 31 * result + (strainAcc != null ? strainAcc.hashCode() : 0);
        result = 31 * result + (strainDbId != null ? strainDbId.hashCode() : 0);
        result = 31 * result + (categoryA != null ? categoryA.hashCode() : 0);
        result = 31 * result + (categoryB != null ? categoryB.hashCode() : 0);
        result = 31 * result + (mpTerm != null ? mpTerm.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {

        return "ResultDTO{" +
                "resultId=" + resultId +
                ", dataSourceId=" + dataSourceId +
                ", projectId=" + projectId +
                ", centerId=" + centerId +
                ", sex=" + sex +
                ", colonyId='" + colonyId + '\'' +
                ", zygosity=" + zygosity +
                ", nullTestPvalue=" + nullTestPvalue +
                ", genotypeEffectPvalue=" + genotypeEffectPvalue +
                ", genotypeEffectSize=" + genotypeEffectSize +
                ", femalePvalue=" + femalePvalue +
                ", femaleEffectSize=" + femaleEffectSize +
                ", malePvalue=" + malePvalue +
                ", maleEffectSize=" + maleEffectSize +
                ", femaleControls=" + femaleControls +
                ", femaleMutants=" + femaleMutants +
                ", maleControls=" + maleControls +
                ", maleMutants=" + maleMutants +
                ", procedureId=" + procedureId +
                ", pipelineId=" + pipelineId +
                ", parameterId=" + parameterId +
                ", geneAcc='" + geneAcc + '\'' +
                ", geneDbId=" + geneDbId +
                ", alleleAcc='" + alleleAcc + '\'' +
                ", alleleDbId=" + alleleDbId +
                ", strainAcc='" + strainAcc + '\'' +
                ", strainDbId=" + strainDbId +
                ", categoryA='" + categoryA + '\'' +
                ", categoryB='" + categoryB + '\'' +
                ", mpTerm='" + mpTerm + '\'' +
                '}';
    }
}
