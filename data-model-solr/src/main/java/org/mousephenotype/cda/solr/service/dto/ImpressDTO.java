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
package org.mousephenotype.cda.solr.service.dto;

import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class ImpressDTO {

	private static final String ID_ID_ID = "ididid"; // unique key

	public static final String PIPELINE_ID = ObservationDTO.PIPELINE_ID;
	public static final String PIPELINE_STABLE_ID = ObservationDTO.PIPELINE_STABLE_ID;
	public static final String PIPELINE_STABLE_KEY = "pipeline_stable_key";
	public static final String PIPELINE_NAME = ObservationDTO.PIPELINE_NAME;

	public static final String PROCEDURE_ID = ObservationDTO.PROCEDURE_ID;
	public static final String PROCEDURE_STABLE_ID = ObservationDTO.PROCEDURE_STABLE_ID;
	public static final String PROCEDURE_STABLE_KEY = "procedure_stable_key";
	public static final String PROCEDURE_NAME = ObservationDTO.PROCEDURE_NAME;

	public static final String PARAMETER_ID = ObservationDTO.PARAMETER_ID;
	public static final String PARAMETER_STABLE_ID = ObservationDTO.PARAMETER_STABLE_ID;
	public static final String PARAMETER_STABLE_KEY = "parameter_stable_key";
	public static final String PARAMETER_NAME = ObservationDTO.PARAMETER_NAME;

	public static final String REQUIRED = "required";
	public static final String MP_TERMS = "mp_terms";
	public static final String DESCRIPTION = "description";
	public static final String UNIT = "unit";
	public static final String INCREMENT = "increment";
	public static final String METADATA = "metadata";
	public static final String HAS_OPTIONS = "has_options";
	public static final String CATEGORIES = "categories";
	public static final String DERIVED = "derived";
	public static final String MEDIA = "media";
	public static final String ANNOTATE = "annotate";
	public static final String OBSERVATION_TYPE = ObservationDTO.OBSERVATION_TYPE;


	public static final String MP_ID = MpDTO.MP_ID;
	public static final String MP_TERM = MpDTO.MP_TERM;
	public static final String MP_TERM_SYNONYM = MpDTO.MP_TERM_SYNONYM;
	public static final String TOP_LEVEL_MP_ID = MpDTO.TOP_LEVEL_MP_ID;
	public static final String TOP_LEVEL_MP_TERM = MpDTO.TOP_LEVEL_MP_TERM;
	public static final String TOP_LEVEL_MP_TERM_SYNONYM = MpDTO.TOP_LEVEL_MP_TERM_SYNONYM;
	public static final String INTERMEDIATE_MP_ID = MpDTO.INTERMEDIATE_MP_ID;
	public static final String INTERMEDIATE_MP_TERM = MpDTO.INTERMEDIATE_MP_TERM;
	public static final String INTERMEDIATE_MP_TERM_SYNONYM = MpDTO.INTERMEDIATE_MP_TERM_SYNONYM;
	public static final String ABNORMAL_MP_ID = "abnormal_mp_id";
	public static final String INCREASED_MP_ID = "increased_mp_id";
	public static final String DECREASED_MP_ID = "decreased_mp_id";
	public static final String ABNORMAL_MP_TERM = "abnormal_mp_term";
	public static final String INCREASED_MP_TERM = "increased_mp_term";
	public static final String DECREASED_MP_TERM = "decreased_mp_term";

	public static final String MA_ID = "ma_id";
	public static final String MA_TERM = "ma_term";
	public static final String INFERRED_MA_ID = MpDTO.INFERRED_MA_ID;
	public static final String INFERRED_MA_TERM_SYNONYM = MpDTO.INFERRED_MA_TERM_SYNONYM;
	public static final String INFERRED_SELECTED_TOP_LEVEL_MA_ID = MpDTO.INFERRED_SELECTED_TOP_LEVEL_MA_ID;
	public static final String INFERRED_SELECTED_TOP_LEVEL_MA_TERM = MpDTO.INFERRED_SELECTED_TOP_LEVEL_MA_TERM;
	public static final String INFERRED_SELECTED_TOP_LEVEL_MA_TERM_SYNONYM = MpDTO.INFERRED_SELECTED_TOP_LEVEL_MA_TERM_SYNONYM;

	public static final String EMAP_ID = "emap_id";
	public static final String EMAP_TERM = "emap_term";

	@Field(INCREASED_MP_ID)
	List<String> increasedMpId;

	@Field(ABNORMAL_MP_ID)
	List<String> abnormalMpId;

	@Field(DECREASED_MP_ID)
	List<String> decreasedMpId;


	@Field(INCREASED_MP_TERM)
	List<String> increasedMpTerm;

	@Field(ABNORMAL_MP_TERM)
	List<String> abnormalMpTerm;

	@Field(DECREASED_MP_TERM)
	List<String> decreasedMpTerm;
	
	@Field(CATEGORIES)
	private List<String> catgories;

	@Field(UNIT)
	private String unit;

	@Field(INCREMENT)
	private boolean increment;

	@Field(METADATA)
	private boolean metadata;

	@Field(HAS_OPTIONS)
	private boolean hasOptions;

	@Field(DERIVED)
	private boolean derived;

	@Field(MEDIA)
	private boolean media;

	@Field(ANNOTATE)
	private boolean annotate;

	@Field(REQUIRED)
	private boolean required;

	@Field(DESCRIPTION)
	private String description;

	@Field(MP_TERMS)
	private List<String> mpTerms;

	@Field(OBSERVATION_TYPE)
	private String observationType;

	@Field(PARAMETER_ID)
	private int parameterId;

	@Field(PARAMETER_STABLE_ID)
	private String parameterStableId;

	@Field(PARAMETER_NAME)
	private String parameterName;

	@Field(PARAMETER_STABLE_KEY)
	private int parameterStableKey;


	@Field(PROCEDURE_ID)
	private Integer procedureId;

	@Field(PROCEDURE_STABLE_ID)
	private String procedureStableId;

	@Field(PROCEDURE_NAME)
	private String procedureName;

	@Field(PROCEDURE_STABLE_KEY)
	private int procedureStableKey;

	@Field(PIPELINE_ID)
	private int pipelineId;

	@Field(PIPELINE_STABLE_ID)
	private String pipelineStableId;

	@Field(PIPELINE_STABLE_KEY)
	private int pipelineStableKey;

	@Field(PIPELINE_NAME)
	private String pipelineName;

	@Field(ID_ID_ID)
	private String ididid;

	//
	// MP fields
	//

	@Field(MP_ID)
	private List<String> mpId;

	@Field(MP_TERM)
	private List<String> mpTerm;

	@Field(MP_TERM_SYNONYM)
	private List<String> mpTermSynonym;

	@Field(TOP_LEVEL_MP_ID)
	private List<String> topLevelMpId;

	@Field(TOP_LEVEL_MP_TERM)
	private List<String> topLevelMpTerm;

	@Field(TOP_LEVEL_MP_TERM_SYNONYM)
	private List<String> topLevelMpTermSynonym;

	@Field(INTERMEDIATE_MP_ID)
	private List<String> intermediateMpId;

	@Field(INTERMEDIATE_MP_TERM)
	private List<String> intermediateMpTerm;

	@Field(INTERMEDIATE_MP_TERM_SYNONYM)
	private List<String> intermediateMpTermSynonym;

	@Field(INFERRED_MA_ID)
	private List<String> inferredMaId;

	@Field(INFERRED_MA_TERM_SYNONYM)
	private List<String> inferredMaTermSynonym;

	@Field(INFERRED_SELECTED_TOP_LEVEL_MA_ID)
	private List<String> selectedTopLevelMaId;

	@Field(INFERRED_SELECTED_TOP_LEVEL_MA_TERM)
	private List<String> inferredSelectedTopLevelMaTerm;

	@Field(INFERRED_SELECTED_TOP_LEVEL_MA_TERM_SYNONYM)
	private List<String> inferredSelectedToLevelMaTermSynonym;

	@Field(INFERRED_SELECTED_TOP_LEVEL_MA_ID)
	private List<String> inferredSelectedTopLevelMaId;

	@Field(MA_ID)
	private String maId;

	@Field(MA_TERM)
	private String maTerm;

	@Field(EMAP_ID)
	private String emapId;

	public String getEmapId() {
		return emapId;
	}


	public void setEmapId(String emapId) {
		this.emapId = emapId;
	}


	public String getEmapTerm() {
		return emapTerm;
	}


	public List<String> getIncreasedMpTerm() {
		return increasedMpTerm;
	}


	public void setIncreasedMpTerm(List<String> increasedMpTerm) {
		this.increasedMpTerm = increasedMpTerm;
	}
	public void addIncreasedMpTerm(String mpTerm){
		if (this.increasedMpTerm == null){
			this.increasedMpTerm = new ArrayList<>();
		}
		increasedMpTerm.add(mpTerm);
	}

	public List<String> getAbnormalMpTerm() {
		return abnormalMpTerm;
	}


	public void setAbnormalMpTerm(List<String> abnormalMpTerm) {
		this.abnormalMpTerm = abnormalMpTerm;
	}
	public void addAbnormalMpTerm(String mpTerm){
		if (this.abnormalMpTerm == null){
			this.abnormalMpTerm = new ArrayList<>();
		}
		abnormalMpTerm.add(mpTerm);
	}

	public List<String> getDecreasedMpTerm() {
		return decreasedMpTerm;
	}


	public void setDecreasedMpTerm(List<String> decreasedMpTerm) {
		this.decreasedMpTerm = decreasedMpTerm;
	}
	
	public void addDecreasedMpTerm(String mpTerm){
		if (this.decreasedMpTerm == null){
			this.decreasedMpTerm = new ArrayList<>();
		}
		decreasedMpId.add(mpTerm);
	}


	public void setEmapTerm(String emapTerm) {
		this.emapTerm = emapTerm;
	}

	@Field(EMAP_TERM)
	private String emapTerm;




	public List<String> getIncreasedMpId() {
		return increasedMpId;
	}


	public void setIncreasedMpId(List<String> increasedMpId) {
		this.increasedMpId = increasedMpId;
	}
	public void addIncreasedMpId(String mpTerm){
		if (this.increasedMpId == null){
			this.increasedMpId = new ArrayList<>();
		}
		increasedMpId.add(mpTerm);
	}

	public List<String> getAbnormalMpId() {
		return abnormalMpId;
	}


	public void setAbnormalMpId(List<String> abnormalMpId) {
		this.abnormalMpId = abnormalMpId;
	}
	public void addAbnormalMpId(String mpTerm){
		if (this.abnormalMpId == null){
			this.abnormalMpId = new ArrayList<>();
		}
		abnormalMpId.add(mpTerm);
	}

	public List<String> getDecreasedMpId() {
		return decreasedMpId;
	}


	public void setDecreasedMpId(List<String> decreasedMpId) {
		this.decreasedMpId = decreasedMpId;
	}
	public void addDecreasedMpId(String mpTerm){
		if (this.decreasedMpId == null){
			this.decreasedMpId = new ArrayList<>();
		}
		decreasedMpId.add(mpTerm);
	}

	public List<String> getCatgories() {
		return catgories;
	}


	public void setCatgories(List<String> catgories) {
		this.catgories = catgories;
	}


	public void setProcedureStableKey(int procedureStableKey) {
		this.procedureStableKey = procedureStableKey;
	}


	public void setPipelineStableKey(int pipelineStableKey) {
		this.pipelineStableKey = pipelineStableKey;
	}


	public boolean isRequired() {
		return required;
	}


	public void setRequired(boolean required) {
		this.required = required;
	}


	public List<String> getCategories() {
		return catgories;
	}


	public void setCategories(List<String> catgories) {
		this.catgories = catgories;
	}


	public List<String> getMpTerms() {
		return mpTerms;
	}


	public void setMpTerms(List<String> mpTerms) {
		this.mpTerms = mpTerms;
	}


	public String getObservationType() {
		return observationType;
	}


	public void setObservationType(String observationType) {
		this.observationType = observationType;
	}


	public List<String> getMpId() {

		return mpId;
	}


	public void setMpId(List<String> mpId) {

		this.mpId = mpId;
	}


	public List<String> getMpTerm() {

		return mpTerm;
	}


	public void setMpTerm(List<String> mpTerm) {

		this.mpTerm = mpTerm;
	}


	public List<String> getMpTermSynonym() {

		return mpTermSynonym;
	}


	public void setMpTermSynonym(List<String> mpTermSynonym) {

		this.mpTermSynonym = mpTermSynonym;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public List<String> getTopLevelMpId() {

		return topLevelMpId;
	}


	public void setTopLevelMpId(List<String> topLevelMpId) {

		this.topLevelMpId = topLevelMpId;
	}


	public List<String> getTopLevelMpTerm() {

		return topLevelMpTerm;
	}


	public void setTopLevelMpTerm(List<String> topLevelMpTerm) {

		this.topLevelMpTerm = topLevelMpTerm;
	}


	public List<String> getTopLevelMpTermSynonym() {

		return topLevelMpTermSynonym;
	}


	public void setTopLevelMpTermSynonym(List<String> topLevelMpTermSynonym) {

		this.topLevelMpTermSynonym = topLevelMpTermSynonym;
	}


	public List<String> getIntermediateMpId() {

		return intermediateMpId;
	}


	public void setIntermediateMpId(List<String> intermediateMpId) {

		this.intermediateMpId = intermediateMpId;
	}


	public List<String> getIntermediateMpTerm() {

		return intermediateMpTerm;
	}


	public void setIntermediateMpTerm(List<String> intermediateMpTerm) {

		this.intermediateMpTerm = intermediateMpTerm;
	}


	public List<String> getIntermediateMpTermSynonym() {

		return intermediateMpTermSynonym;
	}


	public void setIntermediateMpTermSynonym(List<String> intermediateMpTermSynonym) {

		this.intermediateMpTermSynonym = intermediateMpTermSynonym;
	}

	public List<String> getInferredMaId() {

		return inferredMaId;
	}


	public void setInferredMaId(List<String> inferredMaId) {

		this.inferredMaId = inferredMaId;
	}

	public List<String> getInferredMaTermSynonym() {

		return inferredMaTermSynonym;
	}


	public void setInferredMaTermSynonym(List<String> inferredMaTermSynonym) {

		this.inferredMaTermSynonym = inferredMaTermSynonym;
	}


	public List<String> getSelectedTopLevelMaId() {

		return selectedTopLevelMaId;
	}


	public void setSelectedTopLevelMaId(List<String> selectedTopLevelMaId) {

		this.selectedTopLevelMaId = selectedTopLevelMaId;
	}


	public List<String> getInferredSelectedTopLevelMaTerm() {

		return inferredSelectedTopLevelMaTerm;
	}


	public void setInferredSelectedTopLevelMaTerm(List<String> inferredSelectedTopLevelMaTerm) {

		this.inferredSelectedTopLevelMaTerm = inferredSelectedTopLevelMaTerm;
	}


	public List<String> getInferredSelectedToLevelMaTermSynonym() {

		return inferredSelectedToLevelMaTermSynonym;
	}


	public void setInferredSelectedToLevelMaTermSynonym(List<String> inferredSelectedToLevelMaTermSynonym) {

		this.inferredSelectedToLevelMaTermSynonym = inferredSelectedToLevelMaTermSynonym;
	}

	public String getIdidid() {

		return ididid;
	}


	public void setIdidid(String ididid) {

		this.ididid = ididid;
	}


	public int getParameterStableKey() {

		return parameterStableKey;
	}


	public int getParameterId() {

		return parameterId;
	}


	public void setParameterId(int parameterId) {

		this.parameterId = parameterId;
	}


	public String getParameterStableId() {

		return parameterStableId;
	}


	public void setParameterStableId(String parameterStableId) {

		this.parameterStableId = parameterStableId;
	}


	public String getParameterName() {

		return parameterName;
	}


	public void setParameterName(String parameterName) {

		this.parameterName = parameterName;
	}

	public int getPipelineId() {

		return pipelineId;
	}


	public void setPipelineId(int pipelineId) {

		this.pipelineId = pipelineId;
	}

	public void setParameterStableKey(int paramStableKey) {

		this.parameterStableKey = paramStableKey;

	}


	public void setIdIdId(String ididid) {

		this.ididid = ididid;

	}

	public void addMpId(String mpTermId) {

		if (this.mpId == null) {
			this.mpId = new ArrayList<>();
		}
		this.mpId.add(mpTermId);

	}


	public void addMpTerm(String mpTerm) {

		if (this.mpTerm == null) {
			this.mpTerm = new ArrayList<>();
		}
		this.mpTerm.add(mpTerm);

	}


	public void addMpTermSynonym(List<String> mpTermSynonym) {

		if (this.mpTermSynonym == null) {
			this.mpTermSynonym = new ArrayList<>();
		}
		this.mpTermSynonym.addAll(mpTermSynonym);
		this.mpTermSynonym = new ArrayList<>(new HashSet<>(this.mpTermSynonym));
	}

	public void addTopLevelMpId(List<String> topLevelMpTermId) {
		if (this.topLevelMpId == null) {
			this.topLevelMpId = new ArrayList<>();
		}
		this.topLevelMpId.addAll(topLevelMpTermId);
		this.topLevelMpId = new ArrayList<>(new HashSet<>(this.topLevelMpId));
	}


	public void addTopLevelMpTerm(List<String> topLevelMpTerm) {
		if (this.topLevelMpTerm == null) {
			this.topLevelMpTerm = new ArrayList<>();
		}
		this.topLevelMpTerm.addAll(topLevelMpTerm);
		this.topLevelMpTerm = new ArrayList<>(new HashSet<>(this.topLevelMpTerm));
	}


	public void addTopLevelMpTermSynonym(List<String> topLevelMpTermSynonym) {
		if (this.topLevelMpTermSynonym == null) {
			this.topLevelMpTermSynonym = new ArrayList<>();
		}
		this.topLevelMpTermSynonym.addAll(topLevelMpTermSynonym);
		this.topLevelMpTermSynonym = new ArrayList<>(new HashSet<>(this.topLevelMpTermSynonym));
	}


	public void addIntermediateMpId(List<String> intermediateMpId) {
		if (this.intermediateMpId == null) {
			this.intermediateMpId = new ArrayList<>();
		}
		this.intermediateMpId.addAll(intermediateMpId);
		this.intermediateMpId = new ArrayList<>(new HashSet<>(this.intermediateMpId));
	}


	public void addIntermediateMpTerm(List<String> intermediateMpTerm) {
		if (this.intermediateMpTerm == null) {
			this.intermediateMpTerm = new ArrayList<>();
		}
		this.intermediateMpTerm.addAll(intermediateMpTerm);
		this.intermediateMpTerm = new ArrayList<>(new HashSet<>(this.intermediateMpTerm));
	}


	public void addIntermediateMpTermSynonym(List<String> intermediateMpTermSynonym) {

		if (this.intermediateMpTermSynonym == null) {
			this.intermediateMpTermSynonym = new ArrayList<>();
		}
		this.intermediateMpTermSynonym.addAll(intermediateMpTermSynonym);
		this.intermediateMpTermSynonym = new ArrayList<>(new HashSet<>(this.intermediateMpTermSynonym));
	}


	public void addInferredSelectedTopLevelMaId(List<String> inferredSelectedTopLevelMaId) {

		if (this.inferredSelectedTopLevelMaId == null) {
			this.inferredSelectedTopLevelMaId = new ArrayList<>();
		}
		this.inferredSelectedTopLevelMaId.addAll(inferredSelectedTopLevelMaId);


	}


	public void addInferredSelectedTopLevelMaTerm(List<String> inferredSelectedTopLevelMaTerm) {

		if (this.inferredSelectedTopLevelMaTerm == null) {
			this.inferredSelectedTopLevelMaTerm = new ArrayList<>();
		}
		this.inferredSelectedTopLevelMaTerm.addAll(inferredSelectedTopLevelMaTerm);

	}


	public void addInferredSelectedToLevelMaTermSynonym(List<String> inferredSelectedTopLevelMaTermSynonym) {

		if (this.inferredSelectedToLevelMaTermSynonym== null) {
			this.inferredSelectedToLevelMaTermSynonym = new ArrayList<>();
		}
		this.inferredSelectedToLevelMaTermSynonym.addAll(inferredSelectedTopLevelMaTermSynonym);

	}


	public Integer getProcedureId() {
		return procedureId;
	}


	public void setProcedureId(Integer procedureId) {
		this.procedureId = procedureId;
	}


	public String getProcedureStableId() {
		return procedureStableId;
	}


	public void setProcedureStableId(String procedureStableId) {
		this.procedureStableId = procedureStableId;
	}


	public String getProcedureName() {
		return procedureName;
	}


	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}


	public Integer getProcedureStableKey() {
		return procedureStableKey;
	}


	public void setProcedureStableKey(Integer procedureStableKey) {
		this.procedureStableKey = procedureStableKey;
	}


	public String getPipelineStableId() {
		return pipelineStableId;
	}


	public void setPipelineStableId(String pipelineStableId) {
		this.pipelineStableId = pipelineStableId;
	}


	public Integer getPipelineStableKey() {
		return pipelineStableKey;
	}


	public void setPipelineStableKey(Integer pipelineStableKey) {
		this.pipelineStableKey = pipelineStableKey;
	}


	public String getPipelineName() {
		return pipelineName;
	}


	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}


	public List<String> getInferredSelectedTopLevelMaId() {
		return inferredSelectedTopLevelMaId;
	}


	public void setInferredSelectedTopLevelMaId(List<String> inferredSelectedTopLevelMaId) {
		this.inferredSelectedTopLevelMaId = inferredSelectedTopLevelMaId;
	}


	public String getMaTermId() {
		return maId;
	}


	public void setMaId(String maId) {
		this.maId = maId;
	}


	public String getMaName() {
		return maTerm;
	}


	public void setMaName(String maName) {
		this.maTerm = maName;
	}


	public String getUnit() {
		return unit;
	}


	public void setUnit(String unit) {
		this.unit = unit;
	}


	public boolean isIncrement() {
		return increment;
	}


	public void setIncrement(boolean increment) {
		this.increment = increment;
	}


	public boolean isMetadata() {
		return metadata;
	}


	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}


	public boolean getHasOptions() {
		return hasOptions;
	}


	public void setHasOptions(boolean hasOptions) {
		this.hasOptions = hasOptions;
	}


	public boolean isDerived() {
		return derived;
	}


	public void setDerived(boolean derived) {
		this.derived = derived;
	}


	public boolean isMedia() {
		return media;
	}


	public void setMedia(boolean media) {
		this.media = media;
	}

	public boolean isAnnotate() {
		return annotate;
	}

	public void setAnnotate(boolean annotate) {
		this.annotate = annotate;
	}

	public String getMaTerm() {
		return maTerm;
	}


	public void setMaTerm(String maTerm) {
		this.maTerm = maTerm;
	}


	public String getMaId() {
		return maId;
	}


	@Override
	public String toString() {
		return "PipelineDTO [unit=" + unit + ", increment=" + increment + ", metadata=" + metadata + ", hasOptions="
				+ hasOptions + ", derived=" + derived + ", media=" + media + ", required=" + required + ", description="
				+ description + ", mpTerms=" + mpTerms + ", observationType=" + observationType + ", parameterId="
				+ parameterId + ", parameterStableId=" + parameterStableId + ", parameterName=" + parameterName
				+ ", parameterStableKey=" + parameterStableKey + ", procedureId=" + procedureId + ", procedureStableId="
				+ procedureStableId + ", procedureName=" + procedureName + ", procedureStableKey=" + procedureStableKey
				+ ", pipelineId=" + pipelineId + ", pipelineStableId=" + pipelineStableId + ", pipelineStableKey="
				+ pipelineStableKey + ", pipelineName=" + pipelineName + ", ididid=" + ididid + ", mpId=" + mpId
				+ ", mpTerm=" + mpTerm + ", mpTermSynonym=" + mpTermSynonym + ", topLevelMpId=" + topLevelMpId
				+ ", topLevelMpTerm=" + topLevelMpTerm + ", topLevelMpTermSynonym=" + topLevelMpTermSynonym
				+ ", intermediateMpId=" + intermediateMpId + ", intermediateMpTerm=" + intermediateMpTerm
				+ ", intermediateMpTermSynonym=" + intermediateMpTermSynonym + ", inferredMaId=" + inferredMaId
				+ ", inferredMaTermSynonym=" + inferredMaTermSynonym + ", selectedTopLevelMaId=" + selectedTopLevelMaId
				+ ", inferredSelectedTopLevelMaTerm=" + inferredSelectedTopLevelMaTerm
				+ ", inferredSelectedToLevelMaTermSynonym=" + inferredSelectedToLevelMaTermSynonym
				+ ", inferredSelectedTopLevelMaId=" + inferredSelectedTopLevelMaId + ", maId=" + maId + ", maTerm="
				+ maTerm + "]";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abnormalMpId == null) ? 0 : abnormalMpId.hashCode());
		result = prime * result + ((abnormalMpTerm == null) ? 0 : abnormalMpTerm.hashCode());
		result = prime * result + (annotate ? 1231 : 1237);
		result = prime * result + ((catgories == null) ? 0 : catgories.hashCode());
		result = prime * result + ((decreasedMpId == null) ? 0 : decreasedMpId.hashCode());
		result = prime * result + ((decreasedMpTerm == null) ? 0 : decreasedMpTerm.hashCode());
		result = prime * result + (derived ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((emapId == null) ? 0 : emapId.hashCode());
		result = prime * result + ((emapTerm == null) ? 0 : emapTerm.hashCode());
		result = prime * result + (hasOptions ? 1231 : 1237);
		result = prime * result + ((ididid == null) ? 0 : ididid.hashCode());
		result = prime * result + ((increasedMpId == null) ? 0 : increasedMpId.hashCode());
		result = prime * result + ((increasedMpTerm == null) ? 0 : increasedMpTerm.hashCode());
		result = prime * result + (increment ? 1231 : 1237);
		result = prime * result + ((inferredMaId == null) ? 0 : inferredMaId.hashCode());
		result = prime * result + ((inferredMaTermSynonym == null) ? 0 : inferredMaTermSynonym.hashCode());
		result = prime * result + ((inferredSelectedToLevelMaTermSynonym == null) ? 0
				: inferredSelectedToLevelMaTermSynonym.hashCode());
		result = prime * result
				+ ((inferredSelectedTopLevelMaId == null) ? 0 : inferredSelectedTopLevelMaId.hashCode());
		result = prime * result
				+ ((inferredSelectedTopLevelMaTerm == null) ? 0 : inferredSelectedTopLevelMaTerm.hashCode());
		result = prime * result + ((intermediateMpId == null) ? 0 : intermediateMpId.hashCode());
		result = prime * result + ((intermediateMpTerm == null) ? 0 : intermediateMpTerm.hashCode());
		result = prime * result + ((intermediateMpTermSynonym == null) ? 0 : intermediateMpTermSynonym.hashCode());
		result = prime * result + ((maId == null) ? 0 : maId.hashCode());
		result = prime * result + ((maTerm == null) ? 0 : maTerm.hashCode());
		result = prime * result + (media ? 1231 : 1237);
		result = prime * result + (metadata ? 1231 : 1237);
		result = prime * result + ((mpId == null) ? 0 : mpId.hashCode());
		result = prime * result + ((mpTerm == null) ? 0 : mpTerm.hashCode());
		result = prime * result + ((mpTermSynonym == null) ? 0 : mpTermSynonym.hashCode());
		result = prime * result + ((mpTerms == null) ? 0 : mpTerms.hashCode());
		result = prime * result + ((observationType == null) ? 0 : observationType.hashCode());
		result = prime * result + parameterId;
		result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
		result = prime * result + ((parameterStableId == null) ? 0 : parameterStableId.hashCode());
		result = prime * result + parameterStableKey;
		result = prime * result + pipelineId;
		result = prime * result + ((pipelineName == null) ? 0 : pipelineName.hashCode());
		result = prime * result + ((pipelineStableId == null) ? 0 : pipelineStableId.hashCode());
		result = prime * result + pipelineStableKey;
		result = prime * result + ((procedureId == null) ? 0 : procedureId.hashCode());
		result = prime * result + ((procedureName == null) ? 0 : procedureName.hashCode());
		result = prime * result + ((procedureStableId == null) ? 0 : procedureStableId.hashCode());
		result = prime * result + procedureStableKey;
		result = prime * result + (required ? 1231 : 1237);
		result = prime * result + ((selectedTopLevelMaId == null) ? 0 : selectedTopLevelMaId.hashCode());
		result = prime * result + ((topLevelMpId == null) ? 0 : topLevelMpId.hashCode());
		result = prime * result + ((topLevelMpTerm == null) ? 0 : topLevelMpTerm.hashCode());
		result = prime * result + ((topLevelMpTermSynonym == null) ? 0 : topLevelMpTermSynonym.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImpressDTO other = (ImpressDTO) obj;
		if (abnormalMpId == null) {
			if (other.abnormalMpId != null)
				return false;
		} else if (!abnormalMpId.equals(other.abnormalMpId))
			return false;
		if (abnormalMpTerm == null) {
			if (other.abnormalMpTerm != null)
				return false;
		} else if (!abnormalMpTerm.equals(other.abnormalMpTerm))
			return false;
		if (annotate != other.annotate)
			return false;
		if (catgories == null) {
			if (other.catgories != null)
				return false;
		} else if (!catgories.equals(other.catgories))
			return false;
		if (decreasedMpId == null) {
			if (other.decreasedMpId != null)
				return false;
		} else if (!decreasedMpId.equals(other.decreasedMpId))
			return false;
		if (decreasedMpTerm == null) {
			if (other.decreasedMpTerm != null)
				return false;
		} else if (!decreasedMpTerm.equals(other.decreasedMpTerm))
			return false;
		if (derived != other.derived)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (emapId == null) {
			if (other.emapId != null)
				return false;
		} else if (!emapId.equals(other.emapId))
			return false;
		if (emapTerm == null) {
			if (other.emapTerm != null)
				return false;
		} else if (!emapTerm.equals(other.emapTerm))
			return false;
		if (hasOptions != other.hasOptions)
			return false;
		if (ididid == null) {
			if (other.ididid != null)
				return false;
		} else if (!ididid.equals(other.ididid))
			return false;
		if (increasedMpId == null) {
			if (other.increasedMpId != null)
				return false;
		} else if (!increasedMpId.equals(other.increasedMpId))
			return false;
		if (increasedMpTerm == null) {
			if (other.increasedMpTerm != null)
				return false;
		} else if (!increasedMpTerm.equals(other.increasedMpTerm))
			return false;
		if (increment != other.increment)
			return false;
		if (inferredMaId == null) {
			if (other.inferredMaId != null)
				return false;
		} else if (!inferredMaId.equals(other.inferredMaId))
			return false;
		if (inferredMaTermSynonym == null) {
			if (other.inferredMaTermSynonym != null)
				return false;
		} else if (!inferredMaTermSynonym.equals(other.inferredMaTermSynonym))
			return false;
		if (inferredSelectedToLevelMaTermSynonym == null) {
			if (other.inferredSelectedToLevelMaTermSynonym != null)
				return false;
		} else if (!inferredSelectedToLevelMaTermSynonym.equals(other.inferredSelectedToLevelMaTermSynonym))
			return false;
		if (inferredSelectedTopLevelMaId == null) {
			if (other.inferredSelectedTopLevelMaId != null)
				return false;
		} else if (!inferredSelectedTopLevelMaId.equals(other.inferredSelectedTopLevelMaId))
			return false;
		if (inferredSelectedTopLevelMaTerm == null) {
			if (other.inferredSelectedTopLevelMaTerm != null)
				return false;
		} else if (!inferredSelectedTopLevelMaTerm.equals(other.inferredSelectedTopLevelMaTerm))
			return false;
		if (intermediateMpId == null) {
			if (other.intermediateMpId != null)
				return false;
		} else if (!intermediateMpId.equals(other.intermediateMpId))
			return false;
		if (intermediateMpTerm == null) {
			if (other.intermediateMpTerm != null)
				return false;
		} else if (!intermediateMpTerm.equals(other.intermediateMpTerm))
			return false;
		if (intermediateMpTermSynonym == null) {
			if (other.intermediateMpTermSynonym != null)
				return false;
		} else if (!intermediateMpTermSynonym.equals(other.intermediateMpTermSynonym))
			return false;
		if (maId == null) {
			if (other.maId != null)
				return false;
		} else if (!maId.equals(other.maId))
			return false;
		if (maTerm == null) {
			if (other.maTerm != null)
				return false;
		} else if (!maTerm.equals(other.maTerm))
			return false;
		if (media != other.media)
			return false;
		if (metadata != other.metadata)
			return false;
		if (mpId == null) {
			if (other.mpId != null)
				return false;
		} else if (!mpId.equals(other.mpId))
			return false;
		if (mpTerm == null) {
			if (other.mpTerm != null)
				return false;
		} else if (!mpTerm.equals(other.mpTerm))
			return false;
		if (mpTermSynonym == null) {
			if (other.mpTermSynonym != null)
				return false;
		} else if (!mpTermSynonym.equals(other.mpTermSynonym))
			return false;
		if (mpTerms == null) {
			if (other.mpTerms != null)
				return false;
		} else if (!mpTerms.equals(other.mpTerms))
			return false;
		if (observationType == null) {
			if (other.observationType != null)
				return false;
		} else if (!observationType.equals(other.observationType))
			return false;
		if (parameterId != other.parameterId)
			return false;
		if (parameterName == null) {
			if (other.parameterName != null)
				return false;
		} else if (!parameterName.equals(other.parameterName))
			return false;
		if (parameterStableId == null) {
			if (other.parameterStableId != null)
				return false;
		} else if (!parameterStableId.equals(other.parameterStableId))
			return false;
		if (parameterStableKey != other.parameterStableKey)
			return false;
		if (pipelineId != other.pipelineId)
			return false;
		if (pipelineName == null) {
			if (other.pipelineName != null)
				return false;
		} else if (!pipelineName.equals(other.pipelineName))
			return false;
		if (pipelineStableId == null) {
			if (other.pipelineStableId != null)
				return false;
		} else if (!pipelineStableId.equals(other.pipelineStableId))
			return false;
		if (pipelineStableKey != other.pipelineStableKey)
			return false;
		if (procedureId == null) {
			if (other.procedureId != null)
				return false;
		} else if (!procedureId.equals(other.procedureId))
			return false;
		if (procedureName == null) {
			if (other.procedureName != null)
				return false;
		} else if (!procedureName.equals(other.procedureName))
			return false;
		if (procedureStableId == null) {
			if (other.procedureStableId != null)
				return false;
		} else if (!procedureStableId.equals(other.procedureStableId))
			return false;
		if (procedureStableKey != other.procedureStableKey)
			return false;
		if (required != other.required)
			return false;
		if (selectedTopLevelMaId == null) {
			if (other.selectedTopLevelMaId != null)
				return false;
		} else if (!selectedTopLevelMaId.equals(other.selectedTopLevelMaId))
			return false;
		if (topLevelMpId == null) {
			if (other.topLevelMpId != null)
				return false;
		} else if (!topLevelMpId.equals(other.topLevelMpId))
			return false;
		if (topLevelMpTerm == null) {
			if (other.topLevelMpTerm != null)
				return false;
		} else if (!topLevelMpTerm.equals(other.topLevelMpTerm))
			return false;
		if (topLevelMpTermSynonym == null) {
			if (other.topLevelMpTermSynonym != null)
				return false;
		} else if (!topLevelMpTermSynonym.equals(other.topLevelMpTermSynonym))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}


	public ImpressDTO(){

	}

	/**
	 * @author tudose
	 * @return sort by name but IMPC objects always first.
	 */
	public static Comparator<ImpressDTO> getComparatorByProcedureNameImpcFirst()	{
		Comparator<ImpressDTO> comp = new Comparator<ImpressDTO>(){
	    @Override
	    public int compare(ImpressDTO param1, ImpressDTO param2)
	    {
	    	if (isImpc(param1.getProcedureStableId()) && !isImpc(param2.getProcedureStableId())){
				return -1;
			}
			if (isImpc(param2.getProcedureStableId()) && !isImpc(param1.getProcedureStableId())){
				return 1;
			}
			return param1.getProcedureName().compareTo(param2.getProcedureName());
	    }
		private boolean isImpc(String param){
			return param.startsWith("IMPC");
		}

		};
		return comp;
	}
}
