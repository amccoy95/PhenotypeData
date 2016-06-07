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
package org.mousephenotype.cda.indexers;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.mousephenotype.cda.db.dao.MpOntologyDAO;
import org.mousephenotype.cda.indexers.beans.OntologyTermHelper;
import org.mousephenotype.cda.indexers.exceptions.IndexerException;
import org.mousephenotype.cda.solr.service.dto.GenotypePhenotypeDTO;
import org.mousephenotype.cda.utilities.CommonUtils;
import org.mousephenotype.cda.utilities.RunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Populate the MGI-Phenotype core - currently only for internal EBI consumption
 */
@EnableAutoConfiguration
public class MGIPhenotypeIndexer extends AbstractIndexer implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("komp2DataSource")
    DataSource komp2DataSource;

	@Autowired
	@Qualifier("ontodbDataSource")
	DataSource ontodbDataSource;

	@Autowired
    @Qualifier("mgiPhenotypeIndexing")
    SolrServer mgiPhenotypeIndexing;

    @Autowired
    MpOntologyDAO mpOntologyService;

	public MGIPhenotypeIndexer() {
    }

    @Override
    public RunStatus validateBuild() throws IndexerException {
        return super.validateBuild(mgiPhenotypeIndexing);
    }

    @Override
    public void initialise(String[] args) throws IndexerException {

        super.initialise(args);

        printConfiguration();
    }

    public static void main(String[] args) throws IndexerException {
        SpringApplication.run(MGIPhenotypeIndexer.class, args);
    }


    @Override
    public RunStatus run() throws IndexerException, SQLException, IOException, SolrServerException{
        try {
            run("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run(String... strings) throws Exception {


        int count = 0;
        RunStatus runStatus = new RunStatus();
        long start = System.currentTimeMillis();

        try {
            count = populateMgiPhenotypeSolrCore(runStatus);

        } catch (SQLException | IOException | SolrServerException ex) {
            throw new IndexerException(ex);
        }

	    CommonUtils commonUtils = new CommonUtils();
        logger.info(" Added {} total beans in {}", count, commonUtils.msToHms(System.currentTimeMillis() - start));

    }



    // Returns document count.
    public int populateMgiPhenotypeSolrCore(RunStatus runStatus) throws SQLException, IOException, SolrServerException {

	    Connection connection = komp2DataSource.getConnection();

	    int count = 1;

        mgiPhenotypeIndexing.deleteByQuery("*:*");

        String query="SELECT DISTINCT CONCAT_WS(\"-\", bm.id, gf.acc, bmp.phenotype_acc) as id, bm.zygosity, org.short_name AS project_name, " +
	        "org.name as project_fullname, gf.acc AS marker_accession_id, gf.symbol as marker_symbol, " +
	        "  bma.allele_acc AS allele_accession_id, al.name AS allele_name, al.symbol AS allele_symbol, " +
	        "CONCAT_WS(\"-\", bm.id, gf.acc, bmp.phenotype_acc) external_id, bmp.phenotype_acc AS ontology_term_id, ot.name AS ontology_term_name " +
	        "FROM biological_model_phenotype bmp INNER JOIN biological_model bm ON bmp.biological_model_id = bm.id " +
	        "  INNER JOIN biological_model_allele bma ON bma.biological_model_id = bm.id " +
	        "  INNER JOIN biological_model_genomic_feature bmgf ON bmgf.biological_model_id = bm.id " +
	        "  INNER JOIN ontology_term ot ON ot.acc = bmp.phenotype_acc " +
	        "  INNER JOIN genomic_feature gf ON bmgf.gf_acc = gf.acc " +
	        "  INNER JOIN allele al ON bma.allele_acc = al.acc " +
	        "  INNER JOIN external_db org ON org.id=bm.db_id ";


        try (PreparedStatement p = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            p.setFetchSize(Integer.MIN_VALUE);

            ResultSet r = p.executeQuery();

            while (r.next()) {
                GenotypePhenotypeDTO doc = new GenotypePhenotypeDTO();

                doc.setId(count);
                doc.setZygosity(r.getString("zygosity"));
                doc.setProjectName(r.getString("project_name"));
                doc.setProjectFullname(r.getString("project_fullname"));

                doc.setMarkerAccessionId(r.getString("marker_accession_id"));
                doc.setMarkerSymbol(r.getString("marker_symbol"));

                doc.setAlleleAccessionId(r.getString("allele_accession_id"));
                doc.setAlleleName(r.getString("allele_name"));
                doc.setAlleleSymbol(r.getString("allele_symbol"));


	            //doc.setStrainAccessionId(r.getString("strain_accession_id"));
                //doc.setStrainName(r.getString("strain_name"));

                doc.setExternalId(r.getString("external_id"));


	            doc.setAssertionType("manual");
	            doc.setAssertionTypeId("ECO:0000218");

                // MP association
                if ( r.getString("ontology_term_id").startsWith("MP:") ) {
                    // some hard-coded stuff
                    doc.setOntologyDbId(5);

                    String mpId = r.getString("ontology_term_id");
                    doc.setMpTermId(mpId);
                    doc.setMpTermName(r.getString("ontology_term_name"));

                    OntologyTermHelper beanlist = new OntologyTermHelper(mpOntologyService, mpId);

                    doc.setTopLevelMpTermId(beanlist.getTopLevels().getIds());
                    doc.setTopLevelMpTermName(beanlist.getTopLevels().getNames());
                    doc.setTopLevelMpTermSynonym(beanlist.getTopLevels().getSynonyms());
                    doc.setTopLevelMpTermDefinition(beanlist.getTopLevels().getDefinitions());

                    doc.setIntermediateMpTermId(beanlist.getIntermediates().getIds());
                    doc.setIntermediateMpTermName(beanlist.getIntermediates().getNames());
                    doc.setIntermediateMpTermSynonym(beanlist.getIntermediates().getSynonyms());
                    doc.setIntermediateMpTermDefinition(beanlist.getIntermediates().getDefinitions());
                }
                // MPATH association
                else if ( r.getString("ontology_term_id").startsWith("MPATH:") ){
                    // some hard-coded stuff
                    doc.setOntologyDbId(24);

                    doc.setMpathTermId(r.getString("ontology_term_id"));
                    doc.setMpathTermName(r.getString("ontology_term_name"));
                }

                // EMAP association
                else if ( r.getString("ontology_term_id").startsWith("EMAP:") ){
                    // some hard-coded stuff
                    doc.setOntologyDbId(14);

                    doc.setMpathTermId(r.getString("ontology_term_id"));
                    doc.setMpathTermName(r.getString("ontology_term_name"));
                }
                else {
                    runStatus.addError(" Found unknown ontology term: " + r.getString("ontology_term_id"));
                }

	            // Always postnatal
                String developmentalStageAcc = "EFO:0002948";
                String developmentalStageName = "postnatal";

                doc.setLifeStageAcc(developmentalStageAcc);
                doc.setLifeStageName(developmentalStageName);

                documentCount++;
                mgiPhenotypeIndexing.addBean(doc, 30000);
                count ++;

	            if (count % 100000 == 0) {
		            logger.info(" Added " + count + " beans");
	            }
            }

            // Final commit to save the rest of the docs
            mgiPhenotypeIndexing.commit();

        } catch (Exception e) {
            runStatus.addError(" Big error " + e.getMessage());
        }

        return count;
    }

}
