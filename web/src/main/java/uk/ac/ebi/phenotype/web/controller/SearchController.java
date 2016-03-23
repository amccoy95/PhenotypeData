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
package uk.ac.ebi.phenotype.web.controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.mousephenotype.cda.solr.generic.util.Tools;
import org.mousephenotype.cda.solr.service.SolrIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.phenotype.util.SearchConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;


@Controller
public class SearchController {

	//LinkedList<FileMeta> files = new LinkedList<FileMeta>();
    //FileMeta fileMeta = null;

	/**
	 * redirect calls to the base url or /search path to the search page with the version 2 URL path
	 *
	 * @return
	 */
	@RequestMapping("/index.html")
	public String rootForward() {
		return "redirect:/search";
	}


	private String internalSolrUrl;

	@Autowired
	private SolrIndex solrIndex;

	@Autowired
	private SearchConfig searchConfig;

	@Autowired
	private DataTableController dataTableController;


	@Autowired
	private QueryBrokerController queryBrokerController;
	/**
	 * search page
	 *
	 */

	@RequestMapping("/search")
	public String searchResult2(
			HttpServletRequest request,
			Model model) throws IOException, URISyntaxException {

		System.out.println("path: /search");

		return processSearch("gene", "*", null, null, null, false, request, model);
	}

	@RequestMapping("/search/{dataType}")
	public String searchResult(
			@PathVariable ()String dataType,
			@RequestParam(value = "kw", required = false, defaultValue = "*") String query,
			@RequestParam(value = "fq", required = false) String fqStr,
			@RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
			@RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
			@RequestParam(value = "showImgView", required = false) boolean showImgView,
			HttpServletRequest request,
			Model model) throws IOException, URISyntaxException {

		System.out.println("path: /search/datatype");

		return processSearch(dataType, query, fqStr, iDisplayStart, iDisplayLength, showImgView, request, model);
	}

	private String processSearch(String dataType, String query, String fqStr, Integer iDisplayStart, Integer iDisplayLength, boolean showImgView, HttpServletRequest request, Model model) throws IOException, URISyntaxException {
		iDisplayStart =  iDisplayStart == null ? 0 : iDisplayStart;
		request.setAttribute("iDisplayStart", iDisplayStart);
		iDisplayLength = iDisplayLength == null ? 10 : iDisplayLength;
		request.setAttribute("iDisplayLength", iDisplayLength);

		String debug = request.getParameter("debug");

		String paramString = request.getQueryString();
		//System.out.println("***** paramstr: " + paramString);

		JSONObject jr = fetchAllFacetCounts(dataType, query, fqStr, request, model);
		model.addAttribute("facetCount", jr);

		model.addAttribute("searchQuery", query.replaceAll("\\\\",""));
		model.addAttribute("dataType", dataType); // lowercase: core name
		model.addAttribute("dataTypeParams", paramString);
		JSONObject json = fetchSearchResultJson(query, dataType, iDisplayStart, iDisplayLength, showImgView, fqStr, model, request);

		model.addAttribute("jsonStr", convert2DataTableJson(request, json, query, fqStr, iDisplayStart, iDisplayLength, showImgView, dataType));
		return "search";
	}


	public String convert2DataTableJson(HttpServletRequest request, JSONObject json, String query, String fqStr, Integer iDisplayStart, Integer iDisplayLength, Boolean showImgView, String dataType) throws IOException, URISyntaxException {

		String mode = dataType + "Grid";
		String solrCoreName = dataType;
		Boolean legacyOnly = false;
		String evidRank = "";
		String solrParamStr = composeSolrParamStr(query, fqStr, dataType);
		String content = dataTableController.fetchDataTableJson(request, json, mode, query, fqStr, iDisplayStart, iDisplayLength, solrParamStr, showImgView, solrCoreName, legacyOnly, evidRank);
		//System.out.println("CONTENT: " + content);

		return content;
	}

	public JSONObject fetchSearchResultJson(String query, String dataType, Integer iDisplayStart, Integer iDisplayLength, Boolean showImgView, String fqStr, Model model, HttpServletRequest request) throws IOException, URISyntaxException {

		// facet filter on the left panel of search page

		String breadcrumLabel = searchConfig.getBreadcrumLabel(dataType);
		model.addAttribute("dataTypeLabel", breadcrumLabel);
		model.addAttribute("gridHeaderListStr", StringUtils.join(searchConfig.getGridHeaders(dataType), ","));

		// results on the right panel of search page
		internalSolrUrl = request.getAttribute("internalSolrUrl").toString();

		String solrParamStr = composeSolrParamStr(query, fqStr, dataType);
		String url = internalSolrUrl + "/" + dataType + "/select?" + solrParamStr;
		System.out.println("URL: " + url);
		//JSONObject json = solrIndex.getResults(url);

		String mode = dataType + "Grid";
		JSONObject json = solrIndex.getQueryJson(query, dataType, solrParamStr, mode, iDisplayStart, iDisplayLength, showImgView);
		//System.out.println("JSON: " + json.toString());
		return json;
	}

	public String composeSolrParamStr(String query, String fqStr, String dataType){
		if (query.matches("MGI:\\d+")) {
			searchConfig.setQf("mgi_accession_id");
		} else if (query.matches("MP:\\d+")) {
			searchConfig.setQf("mp_id");
		} else if (query.matches("MA:\\d+")) {
			searchConfig.setQf("ma_id");
		}

		String qfStr = searchConfig.getQfSolrStr();
		String defTypeStr = searchConfig.getDefTypeSolrStr();
		String facetStr = searchConfig.getFacetFieldsSolrStr(dataType);
		String flStr = searchConfig.getFieldListSolrStr(dataType);
		String bqStr = searchConfig.getBqStr(dataType, query);
		String sortStr = searchConfig.getSortingStr(dataType);

		String solrParamStr = "wt=json&q=" + query + qfStr + defTypeStr + flStr + facetStr + bqStr + sortStr;

		if (dataType.equals("ma")) {
			fqStr = fqStr == null ? "selected_top_level_ma_term:*" : fqStr;
		}
		if (dataType.equals("mp")) {
			fqStr = fqStr == null ? "top_level_mp_term:*" : fqStr;
		}

		if (fqStr != null) {
			solrParamStr += "&fq=" + fqStr;
		}
		System.out.println("PARAMS*****: " + solrParamStr);
		return solrParamStr;
	}

	public JSONObject fetchAllFacetCounts(String dataType, String query, String fqStr, HttpServletRequest request, Model model) throws IOException, URISyntaxException {
		internalSolrUrl = request.getAttribute("internalSolrUrl").toString();

		JSONObject qryBrokerJson = new JSONObject();
		String qStr = "q=" + query;

		String fqStrOri = fqStr;
		String defaultFq = searchConfig.getFqStr(dataType, fqStr);
		fqStr = fqStr == null ? defaultFq : defaultFq + " AND " + fqStr;

		Map<String, String> coreFq = new HashMap<>();

		List<String> cores = Arrays.asList(new String[]{"gene", "mp", "disease", "ma", "impc_images", "images"});
		for( int i=0; i<cores.size(); i++ ){
			String thisCore = cores.get(i);
			if ( dataType.equals(thisCore) ){
				coreFq.put(dataType, "&fq="+fqStr);
			}
			else {
				coreFq.put(thisCore, "&fq=" + searchConfig.getFqStr(thisCore, fqStrOri));
			}
		}

		String qfDefTypeWt = "&qf=auto_suggest&defType=edismax&wt=json";

		qryBrokerJson.put("gene", qStr + coreFq.get("gene") + qfDefTypeWt);
		qryBrokerJson.put("mp", qStr + coreFq.get("mp") + qfDefTypeWt);
		qryBrokerJson.put("disease", qStr + coreFq.get("disease") + qfDefTypeWt);
		qryBrokerJson.put("ma", qStr + coreFq.get("ma") + qfDefTypeWt);
		qryBrokerJson.put("images", 	qStr + coreFq.get("images") + qfDefTypeWt);
		qryBrokerJson.put("impc_images", qStr + coreFq.get("impc_images") + qfDefTypeWt);

//		System.out.println("gene: " + qStr + coreFq.get("gene") + qfDefTypeWt);
//		System.out.println("mp: " + qStr + coreFq.get("mp") + qfDefTypeWt);
//		System.out.println("disease: " + qStr + coreFq.get("disease") + qfDefTypeWt);
//		System.out.println("ma: " + qStr + coreFq.get("ma") + qfDefTypeWt);
//		System.out.println("impc_images: " + qStr + coreFq.get("impc_images") + qfDefTypeWt);
//		System.out.println("images: " + qStr + coreFq.get("images") + qfDefTypeWt);

		String subfacet = null;
		return queryBrokerController.createJsonResponse(subfacet, qryBrokerJson, request);

	}



	@RequestMapping(value="/batchquery2", method=RequestMethod.GET)
	public @ResponseBody String fetchDataFields(
			@RequestParam(value = "core", required = false) String core,
			HttpServletRequest request,
			Model model) {

		return Tools.fetchOutputFieldsCheckBoxesHtml(core);

	}

	@RequestMapping(value="/batchQuery", method=RequestMethod.GET)
	public String loadBatchQueryPage(
			@RequestParam(value = "core", required = false) String core,
			HttpServletRequest request,
			Model model) {

		String outputFieldsHtml = Tools.fetchOutputFieldsCheckBoxesHtml(core);
		model.addAttribute("outputFields", outputFieldsHtml);

		return "batchQuery";
	}


}
