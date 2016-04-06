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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrServerException;
import org.mousephenotype.cda.db.dao.GenomicFeatureDAO;
import org.mousephenotype.cda.solr.service.GeneService;
import org.mousephenotype.cda.solr.service.MpService;
import org.mousephenotype.cda.solr.service.PostQcService;
import org.mousephenotype.cda.solr.service.StatisticalResultService;
import org.mousephenotype.cda.solr.service.dto.BasicBean;
import org.mousephenotype.cda.solr.web.dto.GeneRowForHeatMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 *
 * @author tudose
 *
 */

@Service("threeI")
public class SecondaryProject3iImpl implements SecondaryProjectService {


	@Autowired
	StatisticalResultService srs;

	@Autowired
	GeneService gs;

	@Autowired
	private GenomicFeatureDAO genesDao;

	@Autowired
	@Qualifier("postqcService")
	private PostQcService gps;


	@Autowired
	private MpService mpService;

	public Set<String> getAccessionsBySecondaryProjectId(String projectId)
	throws SQLException {

		if (projectId.equalsIgnoreCase(SecondaryProjectService.SecondaryProjectIds.threeI.name())){
			return srs.getAccessionsByResourceName("3i");
		}
		return null;
	}


	@Override
	public List<GeneRowForHeatMap> getGeneRowsForHeatMap(HttpServletRequest request)
	throws SolrServerException {

		return  srs.getSecondaryProjectMapForResource("3i");
	}


	@Override
	public List<BasicBean> getXAxisForHeatMap() {

		List<BasicBean> mp = new ArrayList<>();
		try {
			Set<BasicBean> topLevelPhenotypes = mpService.getAllTopLevelPhenotypesAsBasicBeans();
			mp.addAll(topLevelPhenotypes);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return mp;
	}

}
