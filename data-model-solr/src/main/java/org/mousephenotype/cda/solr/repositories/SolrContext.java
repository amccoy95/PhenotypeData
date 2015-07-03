/*
 * Copyright 2012 - 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mousephenotype.cda.solr.repositories;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrServerFactory;
import org.springframework.data.solr.server.support.MulticoreSolrServerFactory;

/**
 * @author Christoph Strobl
 */
@Configuration
@EnableSolrRepositories(basePackages = { "org.mousephenotype.cda.solr.repositories" }, multicoreSupport=true)
public class SolrContext {

	@Bean
	public SolrServer solrServer(@Value("${solr.host}") String solrHost) {
		return new HttpSolrServer(solrHost);
	}

	@Bean
	public SolrServerFactory solrServerFactory(SolrServer solrServer) {
		return new MulticoreSolrServerFactory(solrServer);
	}

	@Bean
	public SolrTemplate solrTemplate(SolrServerFactory solrServerFactory) {
		return new SolrTemplate(solrServerFactory);
	}

}
