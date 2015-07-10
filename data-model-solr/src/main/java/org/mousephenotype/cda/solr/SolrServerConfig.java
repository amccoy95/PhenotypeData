package org.mousephenotype.cda.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.mousephenotype.cda.db.dao.PhenotypePipelineDAO;
import org.mousephenotype.cda.solr.service.PhenotypeCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

import javax.validation.constraints.NotNull;


/**
 * Solr server bean configuration
 */

@Configuration
@ComponentScan("org.mousephenotype.cda.solr")
@EnableSolrRepositories(basePackages = {"org.mousephenotype.cda.solr.repositories"}, multicoreSupport = true)
public class SolrServerConfig {

	@NotNull
	@Value("${solr.host}")
	private String solrBaseUrl;

	@Autowired
	PhenotypePipelineDAO ppDao;

	@Bean
	public SolrServer solrServer(@Value("${solr.host}") String solrHost) {
		return new HttpSolrServer(solrHost);
	}


	//Allele
	@Bean(name = "alleleCore")
	public HttpSolrServer getAlleleCore() {
		return new HttpSolrServer(solrBaseUrl + "/allele");
	}


	//Autosuggest
	@Bean(name = "autosuggestCore")
	HttpSolrServer getAutosuggestCore() {
		return new HttpSolrServer(solrBaseUrl + "/autosuggest");
	}


	//Disease
	@Bean(name = "diseaseCore")
	HttpSolrServer getDiseaseCore() {
		return new HttpSolrServer(solrBaseUrl + "/disease");
	}


	//Gene
	@Bean(name = "geneCore")
	HttpSolrServer getGeneCore() {
		return new HttpSolrServer(solrBaseUrl + "/gene");
	}


	//GenotypePhenotype
	@Bean(name = "genotypePhenotypeCore")
	HttpSolrServer getGenotypePhenotypeCore() {
		return new HttpSolrServer(solrBaseUrl + "/genotype-phenotype");
	}


	//ImpcImages
	@Bean(name = "impcImagesCore")
	HttpSolrServer getImpcImagesCore() {
		return new HttpSolrServer(solrBaseUrl + "/impc_images");
	}


	//MA
	@Bean(name = "maCore")
	HttpSolrServer getMaCore() {
		return new HttpSolrServer(solrBaseUrl + "/ma");
	}


	//MP
	@Bean(name = "mpCore")
	HttpSolrServer getMpCore() {
		return new HttpSolrServer(solrBaseUrl + "/mp");
	}


	//Observation
	@Bean(name = "experimentCore")
	HttpSolrServer getExperimentCore() {
		return new HttpSolrServer(solrBaseUrl + "/experiment");
	}


	//Pipeline
	@Bean(name = "pipelineCore")
	HttpSolrServer getPipelineCore() {
		return new HttpSolrServer(solrBaseUrl + "/pipeline");
	}


	//Preqc
	@Bean(name = "preQcCore")
	HttpSolrServer getPreQcCore() {
		return new HttpSolrServer(solrBaseUrl + "/preqc");
	}


	//SangerImages
	@Bean(name = "sangerImagesCore")
	HttpSolrServer getImagesCore() {
		return new HttpSolrServer(solrBaseUrl + "/images");
	}


	//StatisticalResult
	@Bean(name = "statisticalResultCore")
	HttpSolrServer getStatisticalResultCore() {
		return new HttpSolrServer(solrBaseUrl + "/statistical-result");
	}


	@Bean(name = "phenotypeCenterService")
	PhenotypeCenterService phenotypeCenterService() {
		return new PhenotypeCenterService(solrBaseUrl + "/experiment", ppDao);
	}


	@Bean(name = "preQcPhenotypeCenterService")
	PhenotypeCenterService preQcPhenotypeCenterService() {
		return new PhenotypeCenterService(solrBaseUrl + "/preqc", ppDao);
	}

}
