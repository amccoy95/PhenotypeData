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
package org.mousephenotype.cda.db.pojo;

/**
 * Representation of a genomic feature cross-reference in the database
 *
 * @author Gautier Koscielny (EMBL-EBI) <koscieln@ebi.ac.uk>
 * @since February 2013
 *
 */

import javax.persistence.Embeddable;


@Embeddable
public class Xref extends DatasourceEntityId {

	private Long   id;
	private String xrefAccession;
	private Long   xrefDatabaseId;

	public Xref() {
		super();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the xrefAccession
	 */
	public String getXrefAccession() {
		return xrefAccession;
	}

	/**
	 * @param xrefAccession the xrefAccession to set
	 */
	public void setXrefAccession(String xrefAccession) {
		this.xrefAccession = xrefAccession;
	}

	/**
	 * @return the xrefDatabaseId
	 */
	public Long getXrefDatabaseId() {
		return xrefDatabaseId;
	}

	/**
	 * @param xrefDatabaseId the xrefDatabaseId to set
	 */
	public void setXrefDatabaseId(Long xrefDatabaseId) {
		this.xrefDatabaseId = xrefDatabaseId;
	}

	@Override
	public String toString() {
		return "Xref{" +
				"id=" + id +
				", xrefAccession='" + xrefAccession + '\'' +
				", xrefDatabaseId=" + xrefDatabaseId +
				'}';
	}
}