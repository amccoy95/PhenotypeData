/*******************************************************************************
 *  Copyright © 2013 - 2015 EMBL - European Bioinformatics Institute
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied. See the License for the specific
 *  language governing permissions and limitations under the
 *  License.
 ******************************************************************************/

package uk.ac.ebi.phenotype.web.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.SolrServerException;
import org.mousephenotype.cda.solr.service.MpService;
import org.mousephenotype.cda.solr.service.OntologyBean;
import org.mousephenotype.cda.solr.service.PostQcService;
import org.mousephenotype.cda.solr.service.PreQcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.ac.ebi.phenotype.chart.PieChartCreator;


/**
 * @author tudose
 * @since Feb 2015
 */

@Controller
public class ReportsController {

	@Autowired
	private PostQcService postqcService;

	@Autowired
	private MpService mpService;

	@Autowired
	private PreQcService preqcService;

	
	@RequestMapping(value = "/reports", method = RequestMethod.GET)
	public String defaultAction(HttpSession session, HttpServletRequest request, HttpServletResponse response) 
	throws IOException {

		return "reports";
	}


	@RequestMapping(value = "/reports/ebidcc", method = RequestMethod.GET)
	public String ebiDccReport(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model) 
	throws IOException, URISyntaxException, SolrServerException {
		
		int exact = 0;
		int differentPvalue = 0;
		int moreGeneralCallDcc = 0;
		int moreGeneralCallEbi = 0;
		List<CallDTO> ebiList = getCalls(postqcService.getTabbedCallSummary());
		List<CallDTO> dccList = getCalls(preqcService.getTabbedCallSummary());

		List<CallDTO> noMatch = new ArrayList<>();
		
		for (CallDTO ebi: ebiList){
			boolean someMatch = false; 
			for (CallDTO dcc: dccList){
				if (ebi.getKey().equalsIgnoreCase(dcc.getKey())){
					exact ++;
					someMatch = true;
					break;
				} else if (ebi.getKeyNoPval().equalsIgnoreCase(dcc.getKeyNoPval())){
					differentPvalue ++;
					someMatch = true;
					break;
				} else if (ebi.getKeyNoPvalNoMp().equalsIgnoreCase(dcc.getKeyNoPvalNoMp()) && ebi.mpParentIds.contains(dcc.mpTermId)){
					moreGeneralCallDcc ++;
					someMatch = true;
					break;
				} else if (dcc.getKeyNoPvalNoMp().equalsIgnoreCase(ebi.getKeyNoPvalNoMp()) && dcc.mpParentIds.contains(ebi.mpTermId)){
					moreGeneralCallEbi ++;
					someMatch = true;
					break;
				}
			}
			if (!someMatch){
				noMatch.add(ebi);
			}
		}
		
		Map<String, Integer> grouped = new HashMap<>();
		for (CallDTO call: noMatch){
			if (grouped.containsKey(call.parameterStableId)){
				grouped.put(call.parameterStableId, grouped.get(call.parameterStableId) + 1);
			} else {
				grouped.put(call.parameterStableId, 1);
			}
		}

		for (String param: grouped.keySet()){
			System.out.println(param + "\t" + grouped.get(param));
		}
		
		Map<String, Integer> labelToNumber = new HashMap<String, Integer>();
		labelToNumber.put("Exact match", exact);
		labelToNumber.put("Same MP, different p-value", differentPvalue);
		labelToNumber.put("More general call at the DCC", moreGeneralCallDcc);
		labelToNumber.put("More general call at the EBI", moreGeneralCallEbi);
		labelToNumber.put("No match", ebiList.size()-differentPvalue-moreGeneralCallDcc-exact-moreGeneralCallEbi);
		
		
		model.addAttribute("chart", PieChartCreator.getPieChart(labelToNumber, "chartDiv", "How do EBI calls match the ones at the DCC?", "", null));
		
		model.addAttribute("exact", exact);
		model.addAttribute("differentPvalue", differentPvalue);
		model.addAttribute("moreGeneralCallDcc", moreGeneralCallDcc);
		model.addAttribute("moreGeneralCallEbi", moreGeneralCallEbi);
		model.addAttribute("total", ebiList.size());
		
		System.out.println("Exact " + exact + " differentPvalue "+ differentPvalue + " moreGeneralCallDcc " + moreGeneralCallDcc);
		
		return "ebidccCompare";
	}

	private List<CallDTO> getCalls(String content) 
	throws SolrServerException{
		
		String[] list = content.split("\\r\\n|\\n|\\r");
		List<CallDTO> res = new ArrayList<>();
		// Start from 1 to skip header column
		for (int i = 1; i< list.length; i++){
			res.add(new CallDTO(list[i].replace("\"", "")));
		}
		System.out.println("SIZE Of RES " + res.size());
		return res;
		
	}
	
	
	
	private class CallDTO{
		
		String mpTermId;
		String markerSymbol;
		String colonyId;
		String phenotypingCenter;
		String sex;
		String zygosity;
		String parameterStableId;
		Double pValue;
		List<String> mpParentIds; 
		
		public CallDTO(String line) 
		throws SolrServerException{
			
			String[] split = line.split(",");
			mpTermId= split[0];
			markerSymbol = split[1];
			colonyId = split[2];
			phenotypingCenter = split[3];
			sex = split[4];
			zygosity = split[5];
			parameterStableId = split[6];
			pValue = new Double(split[7]);
			mpParentIds = new ArrayList<>();
			
			for (OntologyBean parent: mpService.getParents(mpTermId)){
				mpParentIds.add(parent.getId());
			}
			
		}
		
		public String getKey(){
			return mpTermId +  markerSymbol + colonyId + phenotypingCenter + sex + zygosity + parameterStableId + pValue;
		}
		
		public String getKeyNoPval(){
			return mpTermId +  markerSymbol + colonyId + phenotypingCenter + sex + zygosity + parameterStableId;
		}
		
		public String getKeyNoPvalNoMp(){
			return markerSymbol + colonyId + phenotypingCenter + sex + zygosity + parameterStableId;
		}

		@Override
		public String toString() {
			return "CallDTO [mpTermId=" + mpTermId + ", markerSymbol=" + markerSymbol + ", colonyId=" + colonyId
					+ ", phenotypingCenter=" + phenotypingCenter + ", sex=" + sex + ", zygosity=" + zygosity
					+ ", parameterStableId=" + parameterStableId + ", pValue=" + pValue + ", mpParentIds=" + mpParentIds
					+ "]";
		}
			
	}
	
}

