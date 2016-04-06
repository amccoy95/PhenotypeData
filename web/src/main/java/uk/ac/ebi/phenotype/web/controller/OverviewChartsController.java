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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrServerException;
import org.mousephenotype.cda.constants.OverviewChartsConstants;
import org.mousephenotype.cda.db.dao.PhenotypePipelineDAO;
import org.mousephenotype.cda.db.impress.Utilities;
import org.mousephenotype.cda.db.pojo.DiscreteTimePoint;
import org.mousephenotype.cda.db.pojo.Parameter;
import org.mousephenotype.cda.enumerations.ObservationType;
import org.mousephenotype.cda.solr.service.ObservationService;
import org.mousephenotype.cda.solr.service.PostQcService;
import org.mousephenotype.cda.solr.service.StatisticalResultService;
import org.mousephenotype.cda.solr.web.dto.CategoricalSet;
import org.mousephenotype.cda.solr.web.dto.StackedBarsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.ebi.phenotype.chart.CategoricalChartAndTableProvider;
import uk.ac.ebi.phenotype.chart.ChartData;
import uk.ac.ebi.phenotype.chart.TimeSeriesChartAndTableProvider;
import uk.ac.ebi.phenotype.chart.UnidimensionalChartAndTableProvider;


@Controller
public class OverviewChartsController {

	@Autowired
	private PhenotypePipelineDAO pipelineDao;

	@Autowired
	ObservationService os;

	@Autowired
	PostQcService gpService;


	@Autowired
	StatisticalResultService srs;

	@Autowired
	Utilities impressUtilities;

	public OverviewChartsController(){

	}

	@RequestMapping(value="/overviewCharts/{phenotype_id}", method=RequestMethod.GET)
	public String getGraph(
		@PathVariable String phenotype_id,
		@RequestParam(required = true, value = "parameter_id") String parameterId,
		@RequestParam(required = false, value = "center") String center,
		@RequestParam(required = false, value = "sex") String sex,
		@RequestParam(required = false, value = "all_centers") String allCenters,
		Model model,
		HttpServletRequest request,
		RedirectAttributes attributes) throws SolrServerException, IOException, URISyntaxException, SQLException{

			String[] centerArray = (center != null) ? center.split(",") : null;
			String[] sexArray = (sex != null) ? sex.split(",") : null;
			String[] allCentersArray = (allCenters != null) ? allCenters.split(",") : null;

			String[] centers = (centerArray != null) ? centerArray : allCentersArray;

			model.addAttribute("chart", getDataOverviewChart(phenotype_id, model, parameterId, centers, sexArray));
			return "overviewChart";
	}

	public ChartData getDataOverviewChart(String mpId, Model model, String parameter, String[] center, String[] sex)
	throws SolrServerException, IOException, URISyntaxException, SQLException{

		CategoricalChartAndTableProvider cctp = new CategoricalChartAndTableProvider();
		TimeSeriesChartAndTableProvider tstp = new TimeSeriesChartAndTableProvider();
		UnidimensionalChartAndTableProvider uctp = new UnidimensionalChartAndTableProvider();
		Parameter p = pipelineDao.getParameterByStableId(parameter);
		ChartData chartRes = null;
		List<String> genes = null;
		String[] centerToFilter = center;


		// Assuming that different versions of a procedure will keep the same name.
		String procedureName = p.getProcedures().iterator().next().getName();

		if (p != null){

			genes = gpService.getGenesAssocByParamAndMp(parameter, mpId);

			if (centerToFilter == null) { // first time we load the page.
				// We need to know centers for the controls, otherwise we show all controls
				Set <String> tempCenters = os.getCenters(p, genes, OverviewChartsConstants.B6N_STRAINS, "experimental");
				centerToFilter = tempCenters.toArray(new String[0]);
			}

			if( impressUtilities.checkType(p).equals(ObservationType.categorical) ){
				CategoricalSet controlSet = os.getCategories(p, null , "control", OverviewChartsConstants.B6N_STRAINS, centerToFilter, sex);
				controlSet.setName("Control");
				System.out.println("CONTROL SET " + controlSet);
				CategoricalSet mutantSet = os.getCategories(p, null, "experimental", OverviewChartsConstants.B6N_STRAINS, centerToFilter, sex);
				mutantSet.setName("Mutant");
				System.out.println("MUTANT SET " + mutantSet);
				List<ChartData> chart = cctp.doCategoricalDataOverview(controlSet, mutantSet, model, p, procedureName);
				if (chart.size() > 0){
					chartRes = chart.get(0);
				}
			}

			else if ( impressUtilities.checkType(p).equals(ObservationType.time_series) ){
				Map<String, List<DiscreteTimePoint>> data = new HashMap<String, List<DiscreteTimePoint>>();
				data.put("Control", os.getTimeSeriesControlData(parameter, OverviewChartsConstants.B6N_STRAINS, centerToFilter, sex));
				data.putAll(os.getTimeSeriesMutantData(parameter, genes, OverviewChartsConstants.B6N_STRAINS, centerToFilter, sex));
				ChartData chart = tstp.doTimeSeriesOverviewData(data, p);
				chart.setId(parameter);
				chartRes = chart;
			}

			else if ( impressUtilities.checkType(p).equals(ObservationType.unidimensional) ){
				StackedBarsData data = srs.getUnidimensionalData(p, genes, OverviewChartsConstants.B6N_STRAINS, "experimental", centerToFilter, sex);
				chartRes = uctp.getStackedHistogram(data, p, procedureName);
			}

			if (chartRes != null && center == null && sex == null){ // we don't do a filtering
				// we want to offer all filter values, not to eliminate males if we filtered on males
				// plus we don't want to do another SolR call each time to get the same data
				Set<String> centerFitlers =	os.getCenters(p, genes, OverviewChartsConstants.B6N_STRAINS, "experimental");
				model.addAttribute("centerFilters", centerFitlers);
			}
		}

		return chartRes;
	}

}
