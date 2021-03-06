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
 * Representation of an allele in the database.
 *
 * @author Gautier Koscielny (EMBL-EBI) <koscieln@ebi.ac.uk>
 * @since February 2012
 */

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "allele")
public class Allele {

	@EmbeddedId
	@AttributeOverrides({
	@AttributeOverride(name="accession",
					   column=@Column(name="acc")),
	@AttributeOverride(name="databaseId",
	   column=@Column(name="db_id"))
	})
	DatasourceEntityId id;

	@OneToOne
	@JoinColumns({
		@JoinColumn(name = "biotype_acc"),
		@JoinColumn(name = "biotype_db_id"),
	})
	private OntologyTerm biotype;

	@OneToOne
	@JoinColumns({
	@JoinColumn(name = "gf_acc"),
	@JoinColumn(name = "gf_db_id"),
	})
	private GenomicFeature gene;

	@Column(name = "name")
	private String name;

	@Column(name = "symbol")
	private String symbol;

	@ElementCollection(fetch= FetchType.EAGER)//JW made eager for the indexing of images and rest service - will this cause problems elsewhere?
	@Fetch(value = FetchMode.SELECT)
	@CollectionTable(name="synonym",
	joinColumns= {@JoinColumn(name="acc"),@JoinColumn(name="db_id")}
			)
	private List<Synonym> synonyms;

	public Allele() {
		super();
	}

	/**
	 * @return the id
	 */
	public DatasourceEntityId getId() {
		return id;
	}

	public OntologyTerm getBiotype() {
		return biotype;
	}

	public void setBiotype(OntologyTerm biotype) {
		this.biotype = biotype;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(DatasourceEntityId id) {
		this.id = id;
	}

	/**
	 * @return the gene
	 */
	public GenomicFeature getGene() {
		return gene;
	}

	/**
	 * @param gene the gene to set
	 */
	public void setGene(GenomicFeature gene) {
		this.gene = gene;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public List<Synonym> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<Synonym> synonyms) {
		this.synonyms = synonyms;
	}

//	@Override
//	public String toString() {
//		return "Allele [id=" + id + ", gene=" + gene + ", name=" + name
//				+ ", symbol=" + symbol + "]";
//	}


	@Override
	public String toString() {
		return "Allele{" +
				"symbol='" + symbol + '\'' +
				", id=" + id +
				", name='" + name + '\'' +
				", biotype=" + biotype +
				", gene=" + gene +
				'}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gene == null) ? 0 : gene.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Allele other = (Allele) obj;
		if (gene == null) {
			if (other.gene != null)
				return false;
		} else if (!gene.equals(other.gene))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}


}
