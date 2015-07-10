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
package uk.ac.ebi.phenotype.web.dao;

import org.apache.log4j.Logger;
import org.mousephenotype.cda.db.pojo.StatisticalResult;
import org.mousephenotype.cda.enumerations.ObservationType;
import org.mousephenotype.cda.solr.generic.util.PhenotypeFacetResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.mousephenotype.cda.solr.service.PostQcService;
import org.mousephenotype.cda.solr.service.PreQcService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


public class PhenotypeCallSummarySolrImpl implements
		PhenotypeCallSummarySolr {

	@Autowired
	@Qualifier("postqcService")
	PostQcService genotypePhenotypeService;

	@Autowired
	@Qualifier("preqcService")
	PreQcService preqcService;

	private static final Logger log = Logger
			.getLogger(PhenotypeCallSummarySolrImpl.class);

	@Override
	public PhenotypeFacetResult getPhenotypeCallByGeneAccession(String accId)
			throws IOException, URISyntaxException {
		return this.getPhenotypeCallByGeneAccessionAndFilter(accId, "");
	}

	@Override
	public PhenotypeFacetResult getPhenotypeCallByMPAccession(
			String phenotype_id) throws IOException, URISyntaxException {
		return this.getPhenotypeCallByMPAccessionAndFilter(phenotype_id, "");

	}

	@Override
	public PhenotypeFacetResult getPhenotypeCallByMPAccessionAndFilter(
			String phenotype_id, String queryString) throws IOException, URISyntaxException {
		// http://wwwdev.ebi.ac.uk/mi/solr/genotype-phenotype/select/?q=mp_term_id:MP:0010025&rows=100&version=2.2&start=0&indent=on&defType=edismax&wt=json&facet=true&facet.field=resource_fullname&facet.field=top_level_mp_term_name&
		return genotypePhenotypeService.getMPCallByMPAccessionAndFilter(phenotype_id, queryString);
	}

	@Override
	public PhenotypeFacetResult getPreQcPhenotypeCallByMPAccessionAndFilter(
			String phenotype_id, String queryString) throws IOException, URISyntaxException {
		// http://wwwdev.ebi.ac.uk/mi/solr/genotype-phenotype/select/?q=mp_term_id:MP:0010025&rows=100&version=2.2&start=0&indent=on&defType=edismax&wt=json&facet=true&facet.field=resource_fullname&facet.field=top_level_mp_term_name&
		return preqcService.getMPCallByMPAccessionAndFilter(phenotype_id, queryString);
	}

	@Override
	public PhenotypeFacetResult getPhenotypeCallByGeneAccessionAndFilter(
			String accId, String filterString) throws IOException, URISyntaxException {
		return genotypePhenotypeService.getMPByGeneAccessionAndFilter(accId, filterString);
	}

	@Override
	public PhenotypeFacetResult getPreQcPhenotypeCallByGeneAccessionAndFilter(
			String accId, String filterString) throws IOException, URISyntaxException {
		return preqcService.getMPByGeneAccessionAndFilter(accId, filterString);
	}

	@Override
	public List<? extends StatisticalResult> getStatisticalResultFor(
                String accession, String parameterStableId, ObservationType observationType, String strainAccession, String alleleAccession)
			throws IOException, URISyntaxException {
		return genotypePhenotypeService.getStatsResultFor(accession, parameterStableId, observationType, strainAccession, alleleAccession);
	}



}
