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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.exception.JDBCConnectionException;
import org.mousephenotype.cda.solr.service.ObservationService;
import org.mousephenotype.cda.solr.service.SolrIndex;
import org.mousephenotype.cda.solr.service.StatisticalResultService;
import org.mousephenotype.cda.solr.service.dto.StatisticalResultDTO;
import org.mousephenotype.cda.solr.web.dto.AllelePageDTO;
import org.mousephenotype.cda.solr.web.dto.ExperimentsDataTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.ebi.phenotype.chart.Constants;
import uk.ac.ebi.phenotype.chart.PhenomeChartProvider;
import uk.ac.ebi.phenotype.error.GenomicFeatureNotFoundException;


@Controller
public class ExperimentsController {


	@Autowired
	SolrIndex solrIndex;

	@Autowired
	private StatisticalResultService srService;

	@Autowired
	private ObservationService observationService;

	private PhenomeChartProvider phenomeChartProvider = new PhenomeChartProvider();

	/**
	 * Runs when the request missing an accession ID. This redirects to the
	 * search page which defaults to showing all genes in the list
	 */
	@RequestMapping("/experimentsFrag")
	public String getAlleles(
			@RequestParam(required = true, value = "geneAccession") String geneAccession,
			@RequestParam(required = false, value = "alleleSymbol") List<String> alleleSymbol,
			@RequestParam(required = false, value = "phenotypingCenter") List<String> phenotypingCenter,
			@RequestParam(required = false, value = "pipelineName") List<String> pipelineName,
			@RequestParam(required = false, value = "procedureStableId") List<String> procedureStableId,
			@RequestParam(required = false, value = "mpTermId") List<String> mpTermId,
			@RequestParam(required = false, value = "resource") ArrayList<String> resource,
			Model model,
			HttpServletRequest request,
			RedirectAttributes attributes)
	throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, GenomicFeatureNotFoundException, IOException, SolrServerException {

		AllelePageDTO allelePageDTO = observationService.getAllelesInfo(geneAccession);
		Map<String, List<ExperimentsDataTableRow>> experimentRows = new HashMap<>();
		int rows = 0;
		String graphBaseUrl = request.getAttribute("mappedHostname").toString() + request.getAttribute("baseUrl").toString();;
		
		experimentRows.putAll(srService.getPvaluesByAlleleAndPhenotypingCenterAndPipeline(geneAccession, alleleSymbol, phenotypingCenter, pipelineName, procedureStableId, resource, mpTermId, graphBaseUrl));
		for ( List<ExperimentsDataTableRow> list : experimentRows.values()){
			rows += list.size();
		}

		String chart = phenomeChartProvider.generatePvaluesOverviewChart(geneAccession, experimentRows, Constants.SIGNIFICANT_P_VALUE, allelePageDTO.getParametersByProcedure());

		model.addAttribute("chart", chart);
		model.addAttribute("rows", rows);
		model.addAttribute("experimentRows", experimentRows);
		model.addAttribute("allelePageDTO", allelePageDTO);

		return "experimentsFrag";
	}
	
	@RequestMapping("/experiments")
	public String getBasicInfo(
			@RequestParam(required = true, value = "geneAccession") String geneAccession,
			@RequestParam(required = false, value = "alleleSymbol") List<String> alleleSymbol,
			@RequestParam(required = false, value = "phenotypingCenter") List<String> phenotypingCenter,
			@RequestParam(required = false, value = "pipelineName") List<String> pipelineName,
			@RequestParam(required = false, value = "procedureStableId") List<String> procedureStableId,
			@RequestParam(required = false, value = "mpTermId") List<String> mpTermId,
			@RequestParam(required = false, value = "resource") ArrayList<String> resource,
			Model model,
			HttpServletRequest request,
			RedirectAttributes attributes)
	throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, GenomicFeatureNotFoundException, IOException, SolrServerException {

		AllelePageDTO allelePageDTO = observationService.getAllelesInfo(geneAccession);
		Map<String, List<ExperimentsDataTableRow>> experimentRows = new HashMap<>();
		int rows = 0;
		String graphBaseUrl = "/";
		
		experimentRows.putAll(srService.getPvaluesByAlleleAndPhenotypingCenterAndPipeline(geneAccession, alleleSymbol, phenotypingCenter, pipelineName, procedureStableId, resource, mpTermId, graphBaseUrl));
		for ( List<ExperimentsDataTableRow> list : experimentRows.values()){
			rows += list.size();
		}

		String chart = phenomeChartProvider.generatePvaluesOverviewChart(geneAccession, experimentRows, Constants.SIGNIFICANT_P_VALUE, allelePageDTO.getParametersByProcedure());

		model.addAttribute("chart", chart);
		model.addAttribute("rows", rows);
		model.addAttribute("experimentRows", experimentRows);
		model.addAttribute("allelePageDTO", allelePageDTO);

		return "experiments";
	}
	
	/**
	 * Error handler for gene not found
	 *
	 * @param exception
	 * @return redirect to error page
	 *
	 */
	@ExceptionHandler(GenomicFeatureNotFoundException.class)
	public ModelAndView handleGenomicFeatureNotFoundException(GenomicFeatureNotFoundException exception) {
        ModelAndView mv = new ModelAndView("identifierError");
        mv.addObject("errorMessage",exception.getMessage());
        mv.addObject("acc",exception.getAcc());
        mv.addObject("type","MGI gene");
        mv.addObject("exampleURI", "/experiments/alleles/MGI:4436678?phenotyping_center=HMGU&pipeline_stable_id=ESLIM_001");
        return mv;
    }

	@ExceptionHandler(JDBCConnectionException.class)
	public ModelAndView handleJDBCConnectionException(JDBCConnectionException exception) {
        ModelAndView mv = new ModelAndView("uncaughtException");
        System.out.println(ExceptionUtils.getFullStackTrace(exception));
        mv.addObject("errorMessage", "An error occurred connecting to the database");
        return mv;
    }

	@ExceptionHandler(Exception.class)
	public ModelAndView handleGeneralException(Exception exception) {
        ModelAndView mv = new ModelAndView("uncaughtException");
        System.out.println(ExceptionUtils.getFullStackTrace(exception));
        return mv;
    }


}
