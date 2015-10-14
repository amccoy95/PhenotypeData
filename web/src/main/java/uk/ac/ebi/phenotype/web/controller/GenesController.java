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
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hibernate.HibernateException;
import org.hibernate.exception.JDBCConnectionException;
import org.mousephenotype.cda.enumerations.ZygosityType;
import org.mousephenotype.cda.solr.generic.util.HttpProxy;
import org.mousephenotype.cda.solr.generic.util.PhenotypeCallSummarySolr;
import org.mousephenotype.cda.solr.generic.util.PhenotypeFacetResult;
import org.mousephenotype.cda.solr.repositories.image.ImagesSolrDao;
import org.mousephenotype.cda.solr.service.ExpressionService;
import org.mousephenotype.cda.solr.service.GeneService;
import org.mousephenotype.cda.solr.service.ImageService;
import org.mousephenotype.cda.solr.service.ObservationService;
import org.mousephenotype.cda.solr.service.PostQcService;
import org.mousephenotype.cda.solr.service.PreQcService;
import org.mousephenotype.cda.solr.service.SolrIndex;
import org.mousephenotype.cda.solr.service.dto.GeneDTO;
import org.mousephenotype.cda.solr.web.dto.DataTableRow;
import org.mousephenotype.cda.solr.web.dto.GenePageTableRow;
import org.mousephenotype.cda.solr.web.dto.ImageSummary;
import org.mousephenotype.cda.solr.web.dto.PhenotypeCallSummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import uk.ac.ebi.phenotype.error.GenomicFeatureNotFoundException;
import uk.ac.ebi.phenotype.generic.util.RegisterInterestDrupalSolr;
import uk.ac.ebi.phenotype.generic.util.SolrIndex2;
import uk.ac.ebi.phenotype.ontology.PhenotypeSummaryBySex;
import uk.ac.ebi.phenotype.ontology.PhenotypeSummaryDAO;
import uk.ac.ebi.phenotype.ontology.PhenotypeSummaryType;
import uk.ac.ebi.phenotype.service.UniprotDTO;
import uk.ac.ebi.phenotype.service.UniprotService;
import uk.ac.sanger.phenodigm2.dao.PhenoDigmWebDao;
import uk.ac.sanger.phenodigm2.model.Gene;
import uk.ac.sanger.phenodigm2.model.GeneIdentifier;
import uk.ac.sanger.phenodigm2.web.AssociationSummary;
import uk.ac.sanger.phenodigm2.web.DiseaseAssociationSummary;

@Controller
public class GenesController {

	private final Logger log = LoggerFactory.getLogger(GenesController.class);
	private static final int numberOfImagesToDisplay = 5;

	@Autowired
	private PhenotypeSummaryDAO phenSummary;

	@Autowired
	private ImagesSolrDao imagesSolrDao;

	@Autowired
	private PhenotypeCallSummarySolr phenoDAO;

	@Autowired
	ObservationService observationService;

	@Autowired
	SolrIndex solrIndex;

	@Autowired
    SolrIndex2 solrIndex2;

	@Autowired
	ImageService imageService;

	@Autowired
	ExpressionService expressionService;

	@Autowired
	private GeneService geneService;

	@Autowired
	private PreQcService preqcService;
	

	@Autowired
	private PostQcService postqcService;
	
	
	@Autowired
	private UniprotService uniprotService;

	@Resource(name = "globalConfiguration")
	private Map<String, String> config;

	/**
	 * Runs when the request missing an accession ID. This redirects to the
	 * search page which defaults to showing all genes in the list
	 */
	@RequestMapping("/genes")
	public String rootForward() {
		return "redirect:/search";
	}


	/**
	 * Prints out the request object
	 */
	@RequestMapping("/genes/print-request")
	public ResponseEntity<String> printRequest(HttpServletRequest request) {

		Enumeration<String> s = request.getHeaderNames();

		while (s.hasMoreElements()) {
			String header = (String) s.nextElement();
			Enumeration<String> headers = request.getHeaders(header);

			while (headers.hasMoreElements()) {
				String actualHeader = (String) headers.nextElement();
			}
		}

		HttpHeaders resp = new HttpHeaders();
		resp.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<String>(request.toString(), resp, HttpStatus.CREATED);
	}


	@RequestMapping("/genes/{acc}")
	public String genes(@PathVariable String acc, @RequestParam(value = "heatmap", required = false, defaultValue = "false") Boolean showHeatmap, Model model, HttpServletRequest request, RedirectAttributes attributes)
	throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, GenomicFeatureNotFoundException, IOException, SQLException, SolrServerException {

		String debug = request.getParameter("debug");
		log.info("#### genesAllele2: debug: " + debug);
		boolean d = debug != null && debug.equals("true");
		if (d) {
			model.addAttribute("debug", "true");
		}

		processGeneRequest(acc, model, request);

		return "genes";
	}


	private void processGeneRequest(String acc, Model model, HttpServletRequest request)
	throws GenomicFeatureNotFoundException, URISyntaxException, IOException, SQLException, SolrServerException {

		GeneDTO gene = geneService.getGeneById(acc);
		
		if (gene == null) {
			log.warn("Gene object from solr for " + acc + " can't be found.");
			throw new GenomicFeatureNotFoundException("Gene " + acc + " can't be found.", acc);
		}

	
		/**
		 * Phenotype Summary
		 */

		HashMap<ZygosityType, PhenotypeSummaryBySex> phenotypeSummaryObjects = null;
		HashMap<String, String> mpGroupsSignificant = new HashMap<> (); // <group, linktToAllData>
		HashMap<String, String> mpGroupsNotSignificant = new HashMap<> ();
		
		String prodStatusIcons = "Neither production nor phenotyping status available ";
		// Get list of tripels of pipeline, allele acc, phenotyping center
		// to link to an experiment page will all data
		try {
			
			phenotypeSummaryObjects = phenSummary.getSummaryObjectsByZygosity(acc);
			mpGroupsSignificant = getGroups(true, phenotypeSummaryObjects);
			mpGroupsNotSignificant = getGroups(false, phenotypeSummaryObjects);
			
			for (String str : mpGroupsSignificant.keySet()){
				if (mpGroupsNotSignificant.keySet().contains(str)){
					mpGroupsNotSignificant.remove(str);
				}
			}

			// add number of top level terms
			int total = 0;
			for (ZygosityType zyg : phenotypeSummaryObjects.keySet()) {
				total += phenotypeSummaryObjects.get(zyg).getTotalPhenotypesNumber();
			}
			model.addAttribute("summaryNumber", total);
			
			List<Map<String, String>> dataMapList = observationService.getDistinctPipelineAlleleCenterListByGeneAccession(acc);
			model.addAttribute("dataMapList", dataMapList);

			boolean hasPreQc = (preqcService.getPhenotypes(acc).size() > 0);
			model.addAttribute("hasPreQcData", hasPreQc);

			String genePageUrl =  request.getAttribute("mappedHostname").toString() + request.getAttribute("baseUrl").toString();
			Map<String, String> prod = geneService.getProductionStatus(acc, genePageUrl );
			prodStatusIcons = (prod.get("productionIcons").equalsIgnoreCase("") || prod.get("phenotypingIcons").equalsIgnoreCase("")) 
					? prodStatusIcons : prod.get("productionIcons") + prod.get("phenotypingIcons").equalsIgnoreCase("");
			
			model.addAttribute("orderPossible", prod.get("orderPossible"));
			
			
		} catch (SolrServerException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// GWAS Gene to IMPC gene mapping
		// commented out for now as we are going to use biosolr stuff to do this
		/*
		List<GwasDTO> gwasMappings = gwasDao.getGwasMappingRows("mgi_gene_symbol", gene.getMarkerSymbol().toUpperCase());

		System.out.println("GeneController FOUND " + gwasMappings.size() + " phenotype to gwas trait mappings");
		if ( gwasMappings.size() > 0 ){
			model.addAttribute("gwasPhenoMapping", gwasMappings.get(0).getPhenoMappingCategory());
		}
		*/
		
		// code for assessing if the person is logged in and if so have they
		// registered interest in this gene or not?
		RegisterInterestDrupalSolr registerInterest = new RegisterInterestDrupalSolr(config.get("drupalBaseUrl"), request);
		Map<String, String> regInt = registerInterest.registerInterestState(acc, request, registerInterest);

		model.addAttribute("registerInterestButtonString", regInt.get("registerInterestButtonString"));
		model.addAttribute("registerButtonAnchor", regInt.get("registerButtonAnchor"));
		model.addAttribute("registerButtonId", regInt.get("registerButtonId"));

		try {
			getExperimentalImages(acc, model);
			getExpressionImages(acc, model);
			getImpcImages(acc, model);
			getImpcExpressionImages(acc, model);

		} catch (SolrServerException e1) {
			e1.printStackTrace();
			log.info("images solr not available");
			model.addAttribute("imageErrors", "Something is wrong Images are not being returned when normally they would");
		}

		// ES Cell and IKMC Allele check (Gautier)
		String solrCoreName = "allele";
		String mode = "ikmcAlleleGrid";
		int countIKMCAlleles = 0;
		boolean ikmcError = false;

		try {
			countIKMCAlleles = solrIndex.getNumFound("allele_name:" + gene.getMarkerSymbol(), solrCoreName, mode, "");
		} catch (Exception e) {
			model.addAttribute("countIKMCAllelesError", Boolean.TRUE);
			e.printStackTrace();
		}

		processPhenotypes(acc, model, "", request);
		
		model.addAttribute("phenotypeSummaryObjects", phenotypeSummaryObjects);
		model.addAttribute("prodStatusIcons", prodStatusIcons);
		model.addAttribute("gene", gene);
		model.addAttribute("request", request);
		model.addAttribute("acc", acc);
		model.addAttribute("isLive", new Boolean((String) request.getAttribute("liveSite")));
		model.addAttribute("phenotypeStarted", geneService.checkPhenotypeStarted(acc));
		model.addAttribute("attemptRegistered", geneService.checkAttemptRegistered(acc));
		model.addAttribute("significantTopLevelMpGroups", mpGroupsSignificant);
		model.addAttribute("notsignificantTopLevelMpGroups", mpGroupsNotSignificant);
		// add in the disease predictions from phenodigm
		processDisease(acc, model);

		model.addAttribute("countIKMCAlleles", countIKMCAlleles);
		log.debug("CHECK IKMC allele error : " + ikmcError);
		log.debug("CHECK IKMC allele found : " + countIKMCAlleles);
	}

	/**
	 * @author ilinca
	 * @since 2015/10/09
	 * @param significant
	 * @param phenotypeSummaryObjects
	 * @return
	 */
	public HashMap<String, String> getGroups (boolean significant, HashMap<ZygosityType, PhenotypeSummaryBySex> phenotypeSummaryObjects){
		
		HashMap<String, String> mpGroups = new HashMap<>();
		
		for ( PhenotypeSummaryBySex summary : phenotypeSummaryObjects.values()){
			for (PhenotypeSummaryType phen : summary.getBothPhenotypes(significant)){
				mpGroups.put(phen.getGroup(), phen.getTopLevelIds());
			}
			for (PhenotypeSummaryType phen : summary.getMalePhenotypes(significant)){
				mpGroups.put(phen.getGroup(), phen.getTopLevelIds());
			}
			for (PhenotypeSummaryType phen : summary.getFemalePhenotypes(significant)){
				mpGroups.put(phen.getGroup(), phen.getTopLevelIds());
			}				
		}
		
		return mpGroups;
	}

	/**
	 * @throws IOException
	 * @throws SolrServerException 
	 */
	@RequestMapping("/genesPhenoFrag/{acc}")
	public String genesPhenoFrag(@PathVariable String acc, Model model, HttpServletRequest request, RedirectAttributes attributes)
	throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, GenomicFeatureNotFoundException, IOException, SolrServerException {

		// Pass on any query string after the 
		String queryString = request.getQueryString();
		processPhenotypes(acc, model, queryString, request);

		return "PhenoFrag";
	}


	/**
	 * @author tudose
	 * @throws Exception 
	 * @since 2015/10/02
	 */
	@RequestMapping("/geneSummary/{acc}")
	public String geneSummary(@PathVariable String acc, Model model, HttpServletRequest request, RedirectAttributes attributes)
	throws Exception {


		GeneDTO gene = geneService.getGeneById(acc);

		UniprotDTO uniprotData = uniprotService.getUniprotData(gene);

		HashMap<ZygosityType, PhenotypeSummaryBySex> phenotypeSummaryObjects = phenSummary.getSummaryObjectsByZygosity(acc);
		HashMap<String, String> mpGroupsSignificant = getGroups(true, phenotypeSummaryObjects);	
		HashMap<String, String> mpGroupsNotSignificant = getGroups(false, phenotypeSummaryObjects);	
		
		for (String str : mpGroupsSignificant.keySet()){
			if (mpGroupsNotSignificant.keySet().contains(str)){
				mpGroupsNotSignificant.remove(str);
			}
		}
		
		Set<String> viabilityCalls = observationService.getViabilityForGene(acc);
		Set<String> allelesWithData = postqcService.getAllGenotypePhenotypes(acc);
		Map<String, String> alleleCassette = (allelesWithData.size() > 0 && allelesWithData != null) ? solrIndex2.getAlleleImage(allelesWithData) : null;
		String genePageUrl =  request.getAttribute("mappedHostname").toString() + request.getAttribute("baseUrl").toString();
		Map<String, String> prod = geneService.getProductionStatus(acc, genePageUrl );
		String prodStatusIcons = (prod.get("productionIcons").equalsIgnoreCase("")) ? "" : prod.get("productionIcons");
		List<ImageSummary> imageSummary = imageService.getImageSummary(acc);
		
		JSONObject pfamJson = (gene.getUniprotAccs() != null && gene.getUniprotAccs().size() > 1) ?
				getResults("http://pfam.xfam.org/protein/" + gene.getUniprotAccs().get(0) + "/graphic").getJSONObject(0) : null;
		
		// Adds "orthologousDiseaseAssociations", "phenotypicDiseaseAssociations" to the model
		processDisease(acc, model);
		model.addAttribute("significantTopLevelMpGroups", mpGroupsSignificant);
		model.addAttribute("notsignificantTopLevelMpGroups", mpGroupsNotSignificant);
		model.addAttribute("viabilityCalls", viabilityCalls);
		model.addAttribute("phenotypeSummaryObjects", phenotypeSummaryObjects);
		model.addAttribute("gene", gene);
		model.addAttribute("alleleCassette", alleleCassette);
		model.addAttribute("imageSummary", imageSummary);
		model.addAttribute("prodStatusIcons", prodStatusIcons);
		model.addAttribute("pfamJson", pfamJson);
		model.addAttribute("uniprotData", uniprotData);
		
		System.out.println("In geneSummary Controller" + imageSummary.size());
		
		return "geneSummary";
	}

	@RequestMapping("/pFam/{acc}")
	public String pfam(@PathVariable String acc, Model model, HttpServletRequest request, RedirectAttributes attributes) 
	throws IOException, URISyntaxException{

		
		JSONObject pfamJson = getResults("http://pfam.xfam.org/protein/O60090/graphic").getJSONObject(0);
		System.out.println("PFAM JSON " + pfamJson);
		model.addAttribute("pfamJson", pfamJson);
		return "pfamDomain";
	}

	
	public JSONArray getResults(String url) throws IOException,
	URISyntaxException {
		
		HttpProxy proxy = new HttpProxy();
		
		try {
			String content = proxy.getContent(new URL(url));
			return (JSONArray) JSONSerializer.toJSON(content);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Map<String, Map<String, Integer>> sortPhenFacets(Map<String, Map<String, Integer>> phenFacets) {

		Map<String, Map<String, Integer>> sortPhenFacets = phenFacets;
		for (String key : phenFacets.keySet()) {
			sortPhenFacets.put(key, new TreeMap<String, Integer>(phenFacets.get(key)));
		}
		return sortPhenFacets;
	}


	private void processPhenotypes(String acc, Model model, String queryString, HttpServletRequest request)
	throws IOException, URISyntaxException, SolrServerException {

		if (queryString == null) {
			queryString = "";
		}
		
		List<PhenotypeCallSummaryDTO> phenotypeList = new ArrayList<PhenotypeCallSummaryDTO>();
		PhenotypeFacetResult phenoResult = null;
		PhenotypeFacetResult preQcResult = new PhenotypeFacetResult();

		try {

			phenoResult = phenoDAO.getPhenotypeCallByGeneAccessionAndFilter(acc, queryString);
			preQcResult = phenoDAO.getPreQcPhenotypeCallByGeneAccessionAndFilter(acc, queryString);

			phenotypeList = phenoResult.getPhenotypeCallSummaries();
			phenotypeList.addAll(preQcResult.getPhenotypeCallSummaries());

			Map<String, Map<String, Integer>> phenoFacets = phenoResult.getFacetResults();
			Map<String, Map<String, Integer>> preQcFacets = preQcResult.getFacetResults();

			for (String key : preQcFacets.keySet()){
				if (preQcFacets.get(key).keySet().size() > 0){
					for (String key2: preQcFacets.get(key).keySet()){
						phenoFacets.get(key).put(key2, preQcFacets.get(key).get(key2));
					}
				}
			}

			// sort facets 
			model.addAttribute("phenoFacets", sortPhenFacets(phenoFacets));

		} catch (HibernateException | JSONException e) {
			log.error("ERROR GETTING PHENOTYPE LIST");
			e.printStackTrace();
			phenotypeList = new ArrayList<PhenotypeCallSummaryDTO>();
		}

		// This is a map because we need to support lookups
		Map<Integer, DataTableRow> phenotypes = new HashMap<>();

		for (PhenotypeCallSummaryDTO pcs : phenotypeList) {
			
			DataTableRow pr = new GenePageTableRow(pcs, request.getAttribute("baseUrl").toString(), config);
			// Collapse rows on sex			
			if (phenotypes.containsKey(pr.hashCode())) {

				pr = phenotypes.get(pr.hashCode());
				TreeSet<String> sexes = new TreeSet<String>();
				for (String s : pr.getSexes()) {
					sexes.add(s);
				}
				sexes.add(pcs.getSex().toString());
				pr.setSexes(new ArrayList<String>(sexes));
				
			}

			phenotypes.put(pr.hashCode(), pr);
		}
		
		ArrayList<GenePageTableRow> l = new ArrayList(phenotypes.values());
		Collections.sort(l);
		model.addAttribute("phenotypes", l);

	}


	/**
	 * Get the first 5 wholemount expression images if available
	 *
	 * @param acc
	 *            the gene to get the images for
	 * @param model
	 *            the model to add the images to
	 * @throws SolrServerException
	 */
	private void getExpressionImages(String acc, Model model)
	throws SolrServerException {

		QueryResponse solrExpressionR = imagesSolrDao.getExpressionFacetForGeneAccession(acc);
		if (solrExpressionR == null) {
			log.error("no response from solr data source for acc=" + acc);
			return;
		}

		List<FacetField> expressionfacets = solrExpressionR.getFacetFields();
		if (expressionfacets == null) {
			log.error("no expression facets from solr data source for acc=" + acc);
			return;
		}

		Map<String, SolrDocumentList> facetToDocs = new HashMap<String, SolrDocumentList>();

		for (FacetField facet : expressionfacets) {
			if (facet.getValueCount() != 0) {
				for (Count value : facet.getValues()) {
					QueryResponse response = imagesSolrDao.getDocsForGeneWithFacetField(acc, "selected_top_level_ma_term", value.getName(), "expName:\"Wholemount Expression\"", 0, numberOfImagesToDisplay);
					if (response != null) {
						facetToDocs.put(value.getName(), response.getResults());
					}
				}
			}
			model.addAttribute("expressionFacets", expressionfacets.get(0).getValues());
			model.addAttribute("expFacetToDocs", facetToDocs);
		}
	}


	/**
	 * Get the first 5 images for aall but the wholemount expression images if
	 * available
	 *
	 * @param acc
	 *            the gene to get the images for
	 * @param model
	 *            the model to add the images to
	 * @throws SolrServerException
	 */
	private void getExperimentalImages(String acc, Model model)
	throws SolrServerException {

		QueryResponse solrR = imagesSolrDao.getExperimentalFacetForGeneAccession(acc);
		if (solrR == null) {
			log.error("no response from solr data source for acc=" + acc);
			return;
		}

		List<FacetField> facets = solrR.getFacetFields();
		if (facets == null) {
			log.error("no facets from solr data source for acc=" + acc);
			return;
		}

		Map<String, SolrDocumentList> facetToDocs = new HashMap<String, SolrDocumentList>();
		List<Count> filteredCounts = new ArrayList<Count>();

		for (FacetField facet : facets) {
			if (facet.getValueCount() != 0) {

				// get rid of wholemount expression facet
				for (Count count : facets.get(0).getValues()) {
					if (!count.getName().equals("Wholemount Expression")) {
						filteredCounts.add(count);
					}
				}

				for (Count count : facet.getValues()) {
					if (!count.getName().equals("Wholemount Expression")) {

						// get 5 images if available for this experiment type
						QueryResponse response = imagesSolrDao.getDocsForGeneWithFacetField(acc, "expName", count.getName(), "", 0, numberOfImagesToDisplay);
						if (response != null) {
							facetToDocs.put(count.getName(), response.getResults());
						}
					}
				}
			}

			model.addAttribute("solrFacets", filteredCounts);
			model.addAttribute("facetToDocs", facetToDocs);
		}
	}


	/**
	 * Get the first 5 images for impc experimental images if available
	 *
	 * @param acc
	 *            the gene to get the images for
	 * @param model
	 *            the model to add the images to
	 * @throws SolrServerException
	 */
	private void getImpcImages(String acc, Model model)
	throws SolrServerException {

		imageService.getImpcImagesForGenePage(acc, model, 0, 5, false);
		//imageService.getControlAndExperimentalImpcImages(acc, model, null, null, 0, 1, "Adult Lac Z");

	}


	/**
	 * Get the first 5 wholemount expression images if available
	 *
	 * @param acc
	 *            the gene to get the images for
	 * @param model
	 *            the model to add the images to
	 * @throws SolrServerException
	 * @throws SQLException 
	 */
	private void getImpcExpressionImages(String acc, Model model)
	throws SolrServerException, SQLException {
		boolean overview=true;
		boolean expressionOverview=true;
		expressionService.getLacImageDataForGene(acc, null, overview, expressionOverview, model);
		expressionService.getExpressionDataForGene(acc, model);
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
		mv.addObject("errorMessage", exception.getMessage());
		mv.addObject("acc", exception.getAcc());
		mv.addObject("type", "MGI gene");
		mv.addObject("exampleURI", "/genes/MGI:104874");
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


	/**
	 * Display an identifier error page
	 */
	@RequestMapping("/identifierError")
	public String identifierError(@PathVariable String acc, Model model, HttpServletRequest request, RedirectAttributes attributes) {

		return "identifierError";
	}


	/**
	 * @throws IOException
	 */
	@RequestMapping("/genomeBrowser/{acc}")
	public String genomeBrowser(@PathVariable String acc, Model model, HttpServletRequest request, RedirectAttributes attributes)
	throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, GenomicFeatureNotFoundException, IOException, SolrServerException{

		GeneDTO gene=geneService.getGeneById(acc);
		model.addAttribute("geneDTO",gene);
		if (gene == null) {
			log.warn("Gene object from solr for " + acc + " can't be found.");
			throw new GenomicFeatureNotFoundException("Gene " + acc + " can't be found.", acc);
		}
		
		List<String> ensemblIds = new ArrayList<String>();
		List<String> vegaIds = new ArrayList<String>();
		List<String> ncbiIds = new ArrayList<String>();
		List<String> ccdsIds = new ArrayList<String>();

		if(gene.getEnsemblGeneIds()!=null){
			ensemblIds=gene.getEnsemblGeneIds();
		}
		if(gene.getVegaIds()!=null){
			vegaIds=gene.getVegaIds();
		}
		if(gene.getCcdsIds()!=null){
			ccdsIds=gene.getCcdsIds();
		}
		if(gene.getNcbiIds()!=null){
			ncbiIds=gene.getNcbiIds();
		}

		model.addAttribute("ensemblIds", ensemblIds);
		model.addAttribute("vegaIds", vegaIds);
		model.addAttribute("ncbiIds", ncbiIds);
		model.addAttribute("ccdsIds", ccdsIds);

		model.addAttribute("gene", gene);
		return "genomeBrowser";
	}


	@RequestMapping("/genesAllele2/{acc}")
	public String genesAllele2(@PathVariable String acc, Model model, HttpServletRequest request, RedirectAttributes attributes)
	throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, GenomicFeatureNotFoundException, IOException, Exception {

		List<Map<String, Object>> constructs2 = solrIndex2.getGeneProductInfo(acc);
		Map<String, Object> creProducts = null;

		if (constructs2 != null) {
			creProducts = constructs2.get(constructs2.size() - 1);
			constructs2.remove(constructs2.size() - 1);
		}

		model.addAttribute("alleleProducts2", constructs2);
		model.addAttribute("alleleProductsCre2", creProducts);

		String debug = request.getParameter("debug");
		boolean d = debug != null && debug.equals("true");
		if (d) {
			model.addAttribute("debug", "true");
		}

		return "genesAllele2";
	}

    @Autowired
	private PhenoDigmWebDao phenoDigmDao;
	private final double rawScoreCutoff = 1.97;


	/**
	 * Adds disease-related info to the model from Phenodigm.
	 *
	 * @param acc
	 * @param model
	 */
	private void processDisease(String acc, Model model) {

		String mgiId = acc;
		log.info("Adding disease info to gene page {}", mgiId);
		model.addAttribute("mgiId", mgiId);
		GeneIdentifier geneIdentifier = new GeneIdentifier(mgiId, mgiId);

		Gene gene = null;
		try {
			gene = phenoDigmDao.getGene(geneIdentifier);
		} catch (RuntimeException e) {
			log.error("Error retrieving disease data for {}", geneIdentifier);
		}

		log.info("Found Gene: " + gene);
		if (gene != null) {
			model.addAttribute("geneIdentifier", gene.getOrthologGeneId());
			model.addAttribute("humanOrtholog", gene.getHumanGeneId());
			log.info("Found gene: {} {}", gene.getOrthologGeneId().getCompoundIdentifier(), gene.getOrthologGeneId().getGeneSymbol());
		} else {
			model.addAttribute("geneIdentifier", geneIdentifier);
			log.info("No human ortholog found for gene: {}", geneIdentifier);
		}

		List<DiseaseAssociationSummary> diseaseAssociationSummarys = new ArrayList<>();
		try {
			log.info("{} - getting disease-gene associations using cutoff {}", geneIdentifier, rawScoreCutoff);
			diseaseAssociationSummarys = phenoDigmDao.getGeneToDiseaseAssociationSummaries(geneIdentifier, rawScoreCutoff);
			log.info("{} - received {} disease-gene associations", geneIdentifier, diseaseAssociationSummarys.size());
		} catch (RuntimeException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
			log.error("Error retrieving disease data for {}", geneIdentifier);
		}

		List<DiseaseAssociationSummary> orthologousDiseaseAssociations = new ArrayList<>();
		List<DiseaseAssociationSummary> phenotypicDiseaseAssociations = new ArrayList<>();

		// add the known association summaries to a dedicated list for the top
		// panel
		for (DiseaseAssociationSummary diseaseAssociationSummary : diseaseAssociationSummarys) {
			AssociationSummary associationSummary = diseaseAssociationSummary.getAssociationSummary();
			if (associationSummary.isAssociatedInHuman()) {
				orthologousDiseaseAssociations.add(diseaseAssociationSummary);
			} else {
				phenotypicDiseaseAssociations.add(diseaseAssociationSummary);
			}
		}
		model.addAttribute("orthologousDiseaseAssociations", orthologousDiseaseAssociations);
		model.addAttribute("phenotypicDiseaseAssociations", phenotypicDiseaseAssociations);

		log.info("Added {} disease associations for gene {} to model", diseaseAssociationSummarys.size(), mgiId);
	}



}
