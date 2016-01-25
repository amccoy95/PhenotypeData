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

import java.util.ArrayList;
import java.util.List;

import org.mousephenotype.cda.enumerations.ObservationType;

/**
 * @since 2015/07/28
 * @author tudose
 *
 */
public class ParameterDTO  extends ImpressBaseDTO{

	boolean increment;
	boolean metadata;
	boolean options;
	boolean derived;
	boolean required;
	boolean media;	
	boolean annotate;	
	
	String dataType;
	String parameterType;
	String maId;
	String maName;
	String unit;

	String abnormalMpId;
	String increasedMpId;
	String decreasedMpId;

	ObservationType observationType;
	
	List<String> procedureStableIds;
	List<String> categories = new ArrayList<>();
	List<String> mpIds = new ArrayList<>();
	
	private String emapId;
	public String getEmapId() {
		return emapId;
	}
	public void setEmapId(String emapId) {
		this.emapId = emapId;
	}
	public String getEmapName() {
		return emapName;
	}
	public void setEmapName(String emapName) {
		this.emapName = emapName;
	}
	private String emapName;
	
	
	public boolean isAnnotate() {
		return annotate;
	}
	public void setAnnotate(boolean annotate) {
		this.annotate = annotate;
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
	public boolean isOptions() {
		return options;
	}
	public void setOptions(boolean options) {
		this.options = options;
	}
	public boolean isDerived() {
		return derived;
	}
	public void setDerived(boolean derived) {
		this.derived = derived;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isMedia() {
		return media;
	}
	public void setMedia(boolean media) {
		this.media = media;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getParameterType() {
		return parameterType;
	}
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}
	public String getMaId() {
		return maId;
	}
	public void setMaId(String maId) {
		this.maId = maId;
	}
	public String getMaName() {
		return maName;
	}
	public void setMaName(String maName) {
		this.maName = maName;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public ObservationType getObservationType() {
		return observationType;
	}
	public void setObservationType(ObservationType observationType) {
		this.observationType = observationType;
	}
	public List<String> getProcedureStableIds() {
		return procedureStableIds;
	}
	public void setProcedureStableIds(List<String> procedureStableIds) {
		this.procedureStableIds = procedureStableIds;
	}
	public void addProcedureStableId(String procedure) {
		if (this.procedureStableIds == null){
			this.procedureStableIds = new ArrayList<>();
		}
		this.procedureStableIds.add(procedure);
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public void addCategories(String category) {
		if (this.categories == null){
			this.categories = new ArrayList<>();
		}
		this.categories.add(category);
	}
	public List<String> getMpIds() {
		return mpIds;
	}
	public void setMpIds(List<String> mpIds) {
		this.mpIds = mpIds;
	}
	public void addMpIds(String mpId) {
		if (this.mpIds == null){
			this.mpIds = new ArrayList<>();
		}
		this.mpIds.add(mpId);
	}
	
	public String getAbnormalMpId() {
		return abnormalMpId;
	}
	public void setAbnormalMpId(String abnormalMpId) {
		this.abnormalMpId = abnormalMpId;
	}
	public String getIncreasedMpId() {
		return increasedMpId;
	}
	public void setIncreasedMpId(String increasedMpId) {
		this.increasedMpId = increasedMpId;
	}
	public String getDecreasedMpId() {
		return decreasedMpId;
	}
	public void setDecreasedMpId(String decreasedMpId) {
		this.decreasedMpId = decreasedMpId;
	}
	@Override
	public String toString() {
		return "ParameterDTO [id=" + getId() + ", stableKey=" + getStableKey() + ", stableId=" + getStableId() + ", name=" + getName()  
				+ "increment=" + increment + ", metadata=" + metadata + ", options=" + options + ", derived="
				+ derived + ", required=" + required + ", media=" + media + ", dataType=" + dataType
				+ ", parameterType=" + parameterType + ", maId=" + maId + ", maName=" + maName + ", unit=" + unit
				+ ", observationType=" + observationType + ", procedureStableIds=" + procedureStableIds
				+ ", categories=" + categories + ", mpIds=" + mpIds + "]";
	}
	
	
	
	
	
}
