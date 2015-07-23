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
 * Parent data access manager implementation.
 *
 * @author Gautier Koscielny (EMBL-EBI) <koscieln@ebi.ac.uk>
 * @since February 2012
 */

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

/*
* Implementation of the HibernateDAO interface
*/

@Repository
@Transactional
public class HibernateDAOImpl implements HibernateDAO {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

	/**
	 * The session factory used to query the database
	 */
	@Autowired
	protected SessionFactory sessionFactory;

	/**
	 * Method to get a jdbc connection.
	 *
	 * @return a jdbc connection.
	 */
	public Connection getConnection() {
		Session session = getSession();

		SessionImplementor sessionImplementor = (SessionImplementor) session;
		Connection connection = null;

		try {
			connection = sessionImplementor.getJdbcConnectionAccess().obtainConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

	/**
	 * Method to get a session from the session factory.
     *
	 * @return a hibernate session.
	 */
	public Session getSession() {
		Session sess = null;
		try {
			sess = sessionFactory.getCurrentSession();
		} catch (org.hibernate.HibernateException he) {
			sess = sessionFactory.openSession();
		}
		return sess;

	}

	/**
	 * @return Returns the sessionFactory.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("rawtypes")
	public Collection executeNativeQuery(String sql) throws SQLException {

		LinkedList<Object> results = new LinkedList<Object>();

		Connection connection = getConnection();
		Statement stmt = connection.createStatement();
		ResultSet rs = null;
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
		  results.add(rs.getString(1));
		}

		rs.close();

		return results;
	}

	@Transactional(readOnly = true)
	public int optimizeTable(String tableName) {
		Query query = getCurrentSession().createSQLQuery(
				"optimize table " + tableName);
		return query.executeUpdate();
	}


	/**
	 * Returns the session associated with the ongoing reward transaction.
	 * @return the transactional session
	 */
	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public void flushAndClearSession() {
		getCurrentSession().flush();
		getCurrentSession().clear();
	}

	protected void finalize() {

		getCurrentSession().flush();
		getCurrentSession().clear();
		getCurrentSession().close();

	}



}
