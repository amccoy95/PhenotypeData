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
package org.mousephenotype.cda.db.dao;

/**
 *
 * External data source access manager implementation.
 *
 * @author Gautier Koscielny (EMBL-EBI) <koscieln@ebi.ac.uk>
 * @since February 2012
 */

import org.hibernate.SessionFactory;
import org.mousephenotype.cda.db.pojo.Datasource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class DatasourceDAOImpl extends HibernateDAOImpl implements DatasourceDAO {

	public DatasourceDAOImpl() {
	}


	/**
	 * Creates a new Hibernate coordinate system data access manager.
	 * @param sessionFactory the Hibernate session factory
	 */
	public DatasourceDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Datasource> getAllDatasources() {
		return getCurrentSession().createQuery("from Datasource").list();
	}

	@Transactional(readOnly = true)
	public Datasource getDatasourceByShortName(String shortName) {
		return (Datasource) getCurrentSession()
				.createQuery("from Datasource as d where d.shortName = :shortname")
				.setParameter("shortname", shortName)
				.uniqueResult();
	}

	@Transactional(readOnly = true)
	public Datasource getDatasourceById(Integer externalDbId){
		return (Datasource) getCurrentSession()
				.createQuery("from Datasource as d where d.id = :externaldbid")
				.setParameter("externaldbid", externalDbId)
				.uniqueResult();
	}

}
