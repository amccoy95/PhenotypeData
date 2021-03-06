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
 *
 * Instance of a biological model
 *
 * @author Gautier Koscielny (EMBL-EBI) <koscieln@ebi.ac.uk>
 * @since February 2012
 * @see BiologicalModel
 */

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;


@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@Table(name = "biological_sample")
public class BiologicalSample implements Serializable {

	public BiologicalSample() {

	}

	public BiologicalSample(
		  String stableId
		, long dbId
		, String group
		, OntologyTerm sampleType
		, Organisation phenotypingCenter
		, Organisation productionCenter
		, BiologicalModel biologicalModel)
	{
		this.stableId = stableId;
		this.datasource = new Datasource();
		this.datasource.setId(dbId);
		this.group = group;
		this.type = sampleType;
		this.organisation = phenotypingCenter;
		this.productionCenter = productionCenter;
		this.biologicalModel = biologicalModel;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * MySQL auto increment
	 * GenerationType.AUTO won't work
	 */
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id")
	protected Long id;

	@Column(name = "external_id")
	protected String stableId;

	@OneToOne
	@JoinColumn(name = "db_id")
	protected Datasource datasource;

	@Column(name = "sample_group")
	protected String group;

	@OneToOne
	@JoinColumns({
	@JoinColumn(name = "sample_type_acc"),
	@JoinColumn(name = "sample_type_db_id"),
	})
	protected OntologyTerm type;

	@OneToOne
	@JoinColumn(name = "organisation_id")
	protected Organisation organisation;

	@OneToOne
	@JoinColumn(name = "production_center_id")
	protected Organisation productionCenter;

	@OneToOne
	@JoinColumn(name = "project_id")
	protected Project project;

	//a association table is used to store the link between the 2 entities
	// will implement this later!
	//@OneToOne
	//@JoinColumn(name = "biological_model_id")
	//private BiologicalModel biologicalModel;
	/**
	 * bi-directional
	 */

	// nullable = false' causes hibernate to do an INNER JOIN on biological_sample_id
	// rather than an OUTER JOIN (OUTER is the default)
	// https://stackoverflow.com/questions/15181632/why-should-i-specify-column-nullable-false
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinTable(name="biological_model_sample",
        joinColumns = @JoinColumn(name="biological_sample_id"),
        inverseJoinColumns = @JoinColumn(name="biological_model_id",
		nullable = false)
    )
	protected BiologicalModel biologicalModel;

	/**
	 * @return the biologicalModel
	 */
	public BiologicalModel getBiologicalModel() {
		return biologicalModel;
	}

	/**
	 * @param biologicalModel the biologicalModel to set
	 */
	public void setBiologicalModel(BiologicalModel biologicalModel) {
		this.biologicalModel = biologicalModel;
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
	 * @return the stableId
	 */
	public String getStableId() {
		return stableId;
	}

	/**
	 * @param stableId the stableId to set
	 */
	public void setStableId(String stableId) {
		this.stableId = stableId;
	}

	/**
	 * @return the datasource
	 */
	public Datasource getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the type
	 */
	public OntologyTerm getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(OntologyTerm type) {
		this.type = type;
	}


	/**
	 * @return the organisation
	 */
	public Organisation getOrganisation() {
		return organisation;
	}

	/**
	 * @param organisation the organisation to set
	 */
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public Organisation getProductionCenter() {
		return productionCenter;
	}

	public void setProductionCenter(Organisation productionCenter) {
		this.productionCenter = productionCenter;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public String toString() {
		return "BiologicalSample{" +
				"id=" + id +
				", stableId='" + stableId + '\'' +
				", datasource=" + datasource +
				", group='" + group + '\'' +
				", type=" + type +
				", organisation=" + organisation +
				", productionCenter=" + productionCenter +
				", project=" + project +
				", biologicalModel=" + biologicalModel +
				'}';
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiologicalSample that = (BiologicalSample) o;
        return stableId.equals(that.stableId) &&
                group.equals(that.group) &&
                organisation.equals(that.organisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId, group, organisation);
    }
}