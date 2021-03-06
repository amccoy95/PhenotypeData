package org.mousephenotype.cda.reports.support;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.mousephenotype.cda.db.repositories.*;
import org.mousephenotype.cda.db.utilities.SqlUtils;
import org.mousephenotype.cda.solr.repositories.image.ImagesSolrDao;
import org.mousephenotype.cda.solr.repositories.image.ImagesSolrJ;
import org.mousephenotype.cda.solr.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

/**
 * ReportType bean configuration
 */

@Configuration
@EnableJpaRepositories(basePackages = {"org.mousephenotype.cda.db.repositories"})
public class ReportsConfig {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${internal_solr_url}")
	private String internalSolrUrl;


	private BiologicalModelRepository       biologicalModelRepository;
	private DatasourceRepository            datasourceRepository;
	private GenesSecondaryProjectRepository genesSecondaryProjectRepository;
	private ImpressService                  impressService;
	private OrganisationRepository          organisationRepository;
	private ParameterRepository				parameterRepository;
	private PipelineRepository              pipelineRepository;

	@Inject
	public ReportsConfig(
			@NotNull BiologicalModelRepository biologicalModelRepository,
			@NotNull DatasourceRepository datasourceRepository,
			@NotNull GenesSecondaryProjectRepository genesSecondaryProjectRepository,
			@NotNull ImpressService impressService,
			@NotNull OrganisationRepository organisationRepository,
			@NotNull ParameterRepository parameterRepository, @NotNull PipelineRepository pipelineRepository)
	{
		this.biologicalModelRepository = biologicalModelRepository;
		this.datasourceRepository = datasourceRepository;
		this.genesSecondaryProjectRepository = genesSecondaryProjectRepository;
		this.impressService = impressService;
		this.organisationRepository = organisationRepository;
		this.parameterRepository = parameterRepository;
		this.pipelineRepository = pipelineRepository;
	}
	

	////////////////////////////////
	// DataSources and JdbcTemplates
	////////////////////////////////

	// komp2
	@Value("${datasource.komp2.jdbc-url}")
	private String komp2Url;
	@Value("${datasource.komp2.username}")
	private String komp2Uername;
	@Value("${datasource.komp2.password}")
	private String komp2Password;
    @Bean
    @Primary
    public DataSource komp2DataSource() {
        return SqlUtils.getConfiguredDatasource(komp2Url, komp2Uername, komp2Password);
    }
    
    
    // cdabase
	@Value("${datasource.cdabase.compare.current.jdbc-url}")
	private String urlCdabaseCurrent;
	@Value("${datasource.cdabase.compare.current.username}")
	private String usernameCdabaseCurrent;
	@Value("${datasource.cdabase.compare.current.password}")
	private String passwordCdabaseCurrent;
	@Bean
	public DataSource cdabaseCurrent() {
		return SqlUtils.getConfiguredDatasource(urlCdabaseCurrent, usernameCdabaseCurrent, passwordCdabaseCurrent);
	}
	@Value("${datasource.cdabase.compare.previous.jdbc-url}")
	private String urlCdabasePrevious;
	@Value("${datasource.cdabase.compare.previous.username}")
	private String usernameCdabasePrevious;
	@Value("${datasource.cdabase.compare.previous.password}")
	private String passwordCdabasePrevious;
	@Bean
	public DataSource cdabasePrevious() {
		return SqlUtils.getConfiguredDatasource(urlCdabasePrevious, usernameCdabasePrevious, passwordCdabasePrevious);
	}
	@Bean(name = "jdbcCdabasePrevious")
	public JdbcTemplate jdbcCdabasePrevious() {
		return new JdbcTemplate(cdabasePrevious());
	}
	@Bean(name = "jdbcCdabaseCurrent")
	public JdbcTemplate jdbcCdabaseCurrent() {
		return new JdbcTemplate(cdabaseCurrent());
	}


	// cda
	@Value("${datasource.cda.compare.current.jdbc-url}")
	private String urlCdaCurrent;
	@Value("${datasource.cda.compare.current.username}")
	private String usernameCdaCurrent;
	@Value("${datasource.cda.compare.current.password}")
	private String passwordCdaCurrent;
	@Bean
	public DataSource cdaCurrent() {
		return SqlUtils.getConfiguredDatasource(urlCdaCurrent, usernameCdaCurrent, passwordCdaCurrent);
	}
	@Value("${datasource.cda.compare.previous.jdbc-url}")
	private String urlCdaPrevious;
	@Value("${datasource.cda.compare.previous.username}")
	private String usernameCdaPrevious;
	@Value("${datasource.cda.compare.previous.password}")
	private String passwordCdaPrevious;
	@Bean
	public DataSource cdaPrevious() {
		return SqlUtils.getConfiguredDatasource(urlCdaPrevious, usernameCdaPrevious, passwordCdaPrevious);
	}
	@Bean(name = "jdbcCdaPrevious")
	public JdbcTemplate jdbcCdaPrevious() {
		return new JdbcTemplate(cdaPrevious());
	}
	@Bean(name = "jdbcCdaCurrent")
	public JdbcTemplate jdbcCdaCurrent() {
		return new JdbcTemplate(cdaCurrent());
	}


	// dcc
	@Value("${datasource.dcc.compare.current.jdbc-url}")
	private String urlDccCurrent;
	@Value("${datasource.dcc.compare.current.username}")
	private String usernameDccCurrent;
	@Value("${datasource.dcc.compare.current.password}")
	private String passwordDccCurrent;
	@Bean
	public DataSource dccCurrent() {
		return SqlUtils.getConfiguredDatasource(urlDccCurrent, usernameDccCurrent, passwordDccCurrent);
	}
	@Value("${datasource.dcc.compare.previous.jdbc-url}")
	private String urlDccPrevious;
	@Value("${datasource.dcc.compare.previous.username}")
	private String usernameDccPrevious;
	@Value("${datasource.dcc.compare.previous.password}")
	private String passwordDccPrevious;
	@Bean
	public DataSource dccPrevious() {
		return SqlUtils.getConfiguredDatasource(urlDccPrevious, usernameDccPrevious, passwordDccPrevious);
	}
	@Bean(name = "jdbcDccPrevious")
	public JdbcTemplate jdbcDccPrevious() {
		return new JdbcTemplate(dccPrevious());
	}
	@Bean(name = "jdbcDccCurrent")
	public JdbcTemplate jdbcDccCurrent() {
		return new JdbcTemplate(dccCurrent());
	}


	/////////////////////////
	// Read only solr servers
	/////////////////////////

	// allele
	@Bean(name = "alleleCore")
	public HttpSolrClient alleleCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/allele").build();
	}

	// allele2
	@Bean(name = "allele2Core")
	public HttpSolrClient allele2Core() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/allele2").build();
	}

	// anatomy
	@Bean(name = "anatomyCore")
	HttpSolrClient anatomyCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/anatomy").build();
	}

	// autosuggest
	@Bean(name = "autosuggestCore")
	HttpSolrClient autosuggestCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/autosuggest").build();
	}

	// experiment
	@Bean(name = "experimentCore")
	HttpSolrClient experimentCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/experiment").build();
	}

	// gene
	@Bean(name = "geneCore")
	HttpSolrClient geneCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/gene").build();
	}

	// genotype-phenotype
	@Bean(name = "genotypePhenotypeCore")
	HttpSolrClient genotypePhenotypeCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/genotype-phenotype").build();
	}

	// images
	@Bean(name = "sangerImagesCore")
	HttpSolrClient imagesCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/images").build();
	}

	// impc_images
	@Bean(name = "impcImagesCore")
	HttpSolrClient impcImagesCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/impc_images").build();
	}

	// mgi-phenotype
	@Bean(name = "mgiPhenotypeCore")
	HttpSolrClient mgiPhenotypeCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/mgi-phenotype").build();
	}

	// mp
	@Bean(name = "mpCore")
	HttpSolrClient mpCore() { return new HttpSolrClient.Builder(internalSolrUrl + "/mp").build(); }

	// phenodigm
	@Bean(name = "phenodigmCore")
	public HttpSolrClient phenodigmCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/phenodigm").build();
	}

	// pipeline
	@Bean(name = "pipelineCore")
	HttpSolrClient pipelineCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/pipeline").build();
	}

	// product
	@Bean(name = "productCore")
	HttpSolrClient productCore() { return new HttpSolrClient.Builder(internalSolrUrl + "/product").build(); }

	// statistical-result
	@Bean(name = "statisticalResultCore")
	HttpSolrClient statisticalResultCore() {
		return new HttpSolrClient.Builder(internalSolrUrl + "/statistical-result").build();
	}


	///////
	// DAOs
	///////

	@Bean
	public ImagesSolrDao imagesSolrDao( HttpSolrClient sangerImagesCore) {
    	return new ImagesSolrJ(sangerImagesCore);
	}


	///////////
	// SERVICES
	///////////

	@Bean
	public AnatomyService anatomyService() {
    	return new AnatomyService(anatomyCore());
	}

	@Bean
	public ExperimentService experimentService() {
		return new ExperimentService();
	}

	@Bean
	public GeneService geneService() {
    	return new GeneService(geneCore());
	}

	@Bean
	public ImageService imageService() {
		return new ImageService(impcImagesCore());
	}

	@Bean
	public ImpressService impressService() {
    	return new ImpressService(pipelineCore());
	}

	@Bean
	public MpService mpService() {
    	return new MpService(mpCore());
	}

	@Bean
	public ObservationService observationService() {
		return new ObservationService(experimentCore());
	}

	@Bean
	public PhenotypeCenterProcedureCompletenessService phenotypeCenterProcedureCompletenessService() {
    	return new PhenotypeCenterProcedureCompletenessService(phenotypeCenterService(), impressService());
	}

	@Bean
	public PhenotypeCenterProcedureCompletenessAllService phenotypeCenterProcedureCompletenessAllService() {
    	return new PhenotypeCenterProcedureCompletenessAllService(phenotypeCenterAllService(), statisticalResultCore());
	}

	@Bean PhenotypeCenterAllService phenotypeCenterAllService() {
    	return new PhenotypeCenterAllService(statisticalResultCore(), mpCore());
	}

	@Bean
	public PhenotypeCenterService phenotypeCenterService() {
    	return new PhenotypeCenterService(experimentCore());
	}

	@Bean
	public GenotypePhenotypeService genotypePhenotypeService() {
    	return new GenotypePhenotypeService(impressService, genotypePhenotypeCore(), genesSecondaryProjectRepository);
	}

	@Bean StatisticalResultService statisticalResultService() {
    	return new StatisticalResultService(
    			impressService(),
				genotypePhenotypeCore(),
				genesSecondaryProjectRepository,
				biologicalModelRepository,
				datasourceRepository,
				organisationRepository,
				parameterRepository,
				pipelineRepository,
				statisticalResultCore());
	}


	/////////
	//	Other
	/////////

//	@Primary
//	@Bean(name = "sessionFactoryHibernate")
//	public SessionFactory sessionFactory() {
//
//		LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(komp2DataSource());
//		sessionBuilder.scanPackages("org.mousephenotype.cda.db.entity");
//		sessionBuilder.scanPackages("org.mousephenotype.cda.db.pojo");
//
//		return sessionBuilder.buildSessionFactory();
//	}

	@Bean
	public SolrClient solrClient() { return new HttpSolrClient.Builder(internalSolrUrl).build(); }

	@Bean
	public SolrOperations solrTemplate() { return new SolrTemplate(solrClient()); }
}
