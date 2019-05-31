package org.mousephenotype.impress2;

import java.io.Serializable;

public class ImpressParamMpterm implements Serializable {


    private Integer paramMptermId;
    private Integer ontologyTermId;
    private int weight;
    private boolean isDeleted;
    private String optionText;
    private Integer incrementId;
    private Integer parameterId;
    private String sex;
    private String selectionOutcome;

    public ImpressParamMpterm() {
    }

    public ImpressParamMpterm(Integer paramMptermId) {
        this.paramMptermId = paramMptermId;
    }

    public ImpressParamMpterm(Integer paramMptermId, int weight, boolean deleted) {
        this.paramMptermId = paramMptermId;
        this.weight = weight;
        this.isDeleted = deleted;
    }

    public Integer getParamMptermId() {
        return paramMptermId;
    }

    public void setParamMptermId(Integer paramMptermId) {
        this.paramMptermId = paramMptermId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getIncrementId() {
        return incrementId;
    }

    public void setIncrementId(Integer incrementId) {
        this.incrementId = incrementId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSelectionOutcome() {
        return selectionOutcome;
    }

    public void setSelectionOutcome(String selectionOutcome) {
        this.selectionOutcome = selectionOutcome;
    }

    public Integer getOntologyTermId() {
        return ontologyTermId;
    }

    public void setOntologyTermId(Integer ontologyTermId) {
        this.ontologyTermId = ontologyTermId;
    }

    public Integer getParameterId() {
        return parameterId;
    }

    public void setParameterId(Integer parameterId) {
        this.parameterId = parameterId;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (paramMptermId != null ? paramMptermId.hashCode() : 0);
        return hash;
    }
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImpressParamMpterm)) {
            return false;
        }
        ImpressParamMpterm other = (ImpressParamMpterm) object;
        return !((this.paramMptermId == null && other.paramMptermId != null) || (this.paramMptermId != null && !this.paramMptermId.equals(other.paramMptermId)));
    }
}