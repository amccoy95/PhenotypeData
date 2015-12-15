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
package org.mousephenotype.cda.solr.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mousephenotype.cda.db.dao.PhenotypePipelineDAO;
import org.mousephenotype.cda.solr.service.dto.BasicBean;
import org.mousephenotype.cda.solr.service.dto.HpDTO;
import org.mousephenotype.cda.solr.service.dto.MaDTO;
import org.mousephenotype.cda.solr.service.dto.MpDTO;
import org.mousephenotype.cda.solr.web.dto.SimpleOntoTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class MaService extends BasicService implements WebStatus{


	@Autowired
	@Qualifier("maCore")
    private HttpSolrServer solr;

	@Autowired
	@Qualifier("phenotypePipelineDAOImpl")
	private PhenotypePipelineDAO pipelineDao;

	public MaService() {
	}



	/**
	 * Return an MA term
	 *
	 * @return single MA term from the ma core.
	 * @throws SolrServerException
	 */
	public MaDTO getMaTerm(String id) throws SolrServerException {

		SolrQuery solrQuery = new SolrQuery()
			.setQuery(MaDTO.MA_ID + ":\"" + id + "\"")
			.setRows(1);

		QueryResponse rsp = solr.query(solrQuery);
		List<MaDTO> mas = rsp.getBeans(MaDTO.class);

		if (rsp.getResults().getNumFound() > 0) {
			return mas.get(0);
		}

		return null;
	}

    /**
     * Return all MA terms from the ma core.
     *
     * @return all MAs from the ma core.
     * @throws SolrServerException
     */
    public List<MaDTO> getAllMaTerms() throws SolrServerException {

    	System.out.println("SOLR: " + solr.getBaseURL());
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(MaDTO.MA_ID + ":*");
       // solrQuery.setFields(MpDTO.MA_ID);
        solrQuery.setRows(1000000);
        QueryResponse rsp;
        rsp = solr.query(solrQuery);
        List<MaDTO> mas = rsp.getBeans(MaDTO.class);

        return mas;
    }

    public Set<BasicBean> getAllTopLevelPhenotypesAsBasicBeans() throws SolrServerException{

		SolrQuery solrQuery = new SolrQuery();
		solrQuery.addFacetField("top_level_mp_term_id");
		solrQuery.setRows(0);
		QueryResponse rsp = solr.query(solrQuery);
		System.out.println("solr query in basicbean="+solrQuery);

		HashSet<BasicBean> allTopLevelPhenotypes = new LinkedHashSet<BasicBean>();
		for (FacetField ff:rsp.getFacetFields()){
			for(Count count: ff.getValues()){
				String mpArray[]=count.getName().split("___");
				BasicBean bean=new BasicBean();
				bean.setName(mpArray[0]);
				bean.setId(mpArray[1]);
				allTopLevelPhenotypes.add(bean);
			}

		}
		return allTopLevelPhenotypes;
	}

    public ArrayList<String> getChildrenFor(String mpId) throws SolrServerException{

    	SolrQuery solrQuery = new SolrQuery();
    	solrQuery.setQuery(MpDTO.MP_ID + ":\"" + mpId + "\"");
    	solrQuery.setFields(MpDTO.CHILD_MP_ID);
		QueryResponse rsp = solr.query(solrQuery);
		SolrDocumentList res = rsp.getResults();

//		System.out.println("Solr URL to getChildrenFor: " + solr.getBaseURL() + "/select?" + solrQuery);
		ArrayList<String> children = new ArrayList<String>();

        for (SolrDocument doc : res) {
        	if (doc.containsKey(MpDTO.CHILD_MP_ID)){
        		for (Object child: doc.getFieldValues(MpDTO.CHILD_MP_ID)){
        			children.add((String)child);
        		}
        	}
        }
        return children;
    }

    
    @Override
	public long getWebStatus() throws SolrServerException {
		SolrQuery query = new SolrQuery();

		query.setQuery("*:*").setRows(0);

		//System.out.println("SOLR URL WAS " + solr.getBaseURL() + "/select?" + query);

		QueryResponse response = solr.query(query);
		return response.getResults().getNumFound();
	}
	@Override
	public String getServiceName(){
		return "MA Service";
	}
    
	
}
