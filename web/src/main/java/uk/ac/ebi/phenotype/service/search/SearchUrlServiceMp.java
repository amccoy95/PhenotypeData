/** *****************************************************************************
 * Copyright 2017 QMUL - Queen Mary University of London
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
 ****************************************************************************** */
package uk.ac.ebi.phenotype.service.search;

import org.apache.solr.client.solrj.SolrClient;
import org.mousephenotype.cda.solr.SolrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Support for solr search queries for core "mp"
 *
 */
@Service
public class SearchUrlServiceMp extends SearchUrlService {

    @Autowired
    @Qualifier("mpCore")
    private SolrClient solr;

    @Override
    public String qf() {
        return "mixSynQf";
    }

    @Override
    public String bq(String q) {
        if (q.equals("*:*") || q.equals("*")) {
            return "mp_term:\"male infertility\" ^100"
                    + " mp_term:\"female infertility\" ^100"
                    + " mp_term:infertility ^90";
        } else {
            return "mp_term:(" + q + ")^1000"
                    + " mp_term_synonym:(" + q + ")^500"
                    + " mp_definition:(" + q + ")^100";
        }
    }

    @Override
    public List<String> fieldList() {
        return Arrays.asList("mp_id", "mp_term", "mixSynQf", "mp_definition");
    }

    @Override
    public List<String> facetFields() {
        return Arrays.asList("top_level_mp_term_inclusive");
    }

    @Override
    public String facetSort() {
        return "index";
    }

    @Override
    public String facetMinCount() { return "0"; }

    @Override
    public List<String> gridHeaders() {
        return Arrays.asList("Phenotype", "Definition", "Ontology<br/>Tree");
    }

    @Override
    public String breadcrumb() {
        return "Phenotypes";
    }

    @Override
    public String sort() {
        return "mp_term asc";
    }

    @Override
    public String solrUrl() {
        return SolrUtils.getBaseURL(solr);
    }

}
