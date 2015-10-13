/*******************************************************************************
 * Copyright 2015 EMBL - European Bioinformatics Institute
 * <p>
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
 * Secondarr project DAO
 *
 * @author Gautier Koscielny (EMBL-EBI) <koscieln@ebi.ac.uk>
 * @since February 2012
 */

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;


@Repository
@Transactional
public class SecondaryProjectDAOImpl extends HibernateDAOImpl implements SecondaryProjectDAO {

	public SecondaryProjectDAOImpl() {

	}


	/**
	 * Creates a new Hibernate sequence region data access manager.
	 * @param sessionFactory the Hibernate session factory
	 */
	public SecondaryProjectDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	@Transactional(readOnly = true)
	@Override
	public Set<String> getAccessionsBySecondaryProjectId(String projectId)
		throws SQLException {
		Set<String> accessions = new TreeSet<>();

		String query = "select * from genes_secondary_project where secondary_project_id="
			+ "\"" + projectId + "\"";// +" limit 10";

		try (PreparedStatement statement = getConnection().prepareStatement(
			query)) {

			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String result = resultSet.getString(1);
				accessions.add(result);
			}
		}
		// accessions.add("MGI:104874");//just for testing as no others seem to
		// have mice produced so far for idg
		// accessions.add("MGI:2683087");
		return accessions;
	}

}
