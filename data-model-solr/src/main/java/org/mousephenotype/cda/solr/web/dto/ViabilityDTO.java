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
package org.mousephenotype.cda.solr.web.dto;

import java.util.HashMap;
import java.util.Map;

import org.mousephenotype.cda.solr.service.dto.ObservationDTO;

public class ViabilityDTO {
	public final static String totalPups="IMPC_VIA_003_001";
	public final static String totalPupsWt="IMPC_VIA_004_001";
	public final static String totalPupsHom="IMPC_VIA_006_001";
	public final static String totalPupsHet="IMPC_VIA_005_001";
	public final static String totalMalePups="IMPC_VIA_010_001";// Total Male Pups value=96.0
	public final static String totalFemalePups="IMPC_VIA_014_001";// Total Female Pups value=105.0
	public final static String totalMaleHom="IMPC_VIA_009_001";// Total Male Homozygous value=2.0
	public final static String totalFemaleHet="IMPC_VIA_012_001";// Total Female Heterozygous value=56.0
	public final static String totalMaleHet="IMPC_VIA_008_001";// Total Male Heterozygous value=48.0
	public final static String totalFemaleWt="IMPC_VIA_011_001";// Total Female WT value=46.0
	public final static String totalMaleWt="IMPC_VIA_007_001";// Total Male WT value=46.0
	public final static String totalFemaleHom="IMPC_VIA_013_001";// Total Female Homozygous value=3.0

	Map<String, ObservationDTO> paramStableIdToObservation= new HashMap<>();


	public Map<String, ObservationDTO> getParamStableIdToObservation() {

		return paramStableIdToObservation;
	}



	public void setParamStableIdToObservation(Map<String, ObservationDTO> paramStableIdToObservation) {

		this.paramStableIdToObservation = paramStableIdToObservation;
	}


	private String totalChart = "";
	private String maleChart = "";
	private String femaleChart = "";
	String category = "";// should get set to e.g. Homozygous - Viable




	public String getCategory() {

		return category;
	}


	public void setCategory(String category) {

		this.category = category;
	}


	public String getTotalChart() {

		return totalChart;
	}


	public String getMaleChart() {

		return maleChart;
	}


	public String getFemaleChart() {

		return femaleChart;
	}


	public void setTotalChart(String totalChart) {

		this.totalChart = totalChart;
	}


	public void setMaleChart(String maleChart) {

		this.maleChart = maleChart;
	}


	public void setFemaleChart(String femaleChart) {

		this.femaleChart = femaleChart;
	}

}
