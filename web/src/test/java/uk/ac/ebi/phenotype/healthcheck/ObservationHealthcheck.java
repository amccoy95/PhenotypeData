/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
/**
 * Copyright © 2014 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This test class is intended to run healthchecks against the observation table.
 */

package uk.ac.ebi.phenotype.healthcheck;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mousephenotype.cda.db.pojo.ObservationMissingNotMissingCount;
import org.mousephenotype.cda.db.pojo.ObservationMissingOntologyTerm;
import org.mousephenotype.cda.db.repositories.ObservationMissingNotMissingCountRepository;
import org.mousephenotype.cda.db.repositories.ObservationMissingOntologyTermRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.phenotype.web.TestConfig;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Mouseinformatics fetches an xml file nightly that contains all of the
 * phenotyping data from YYY. The first step to QC-ing the data is to transform
 * that file into a set of import statistics that are loaded into the
 * 'observations' table. The important fields are:
 * <ul><li><code>missing</code> - if 1, the record identified by xxx is missing;
 * if 0, the data is not missing</li>
 * <li><code>parameter_status</code> - a controlled vocabulary term describing
 * the reason the data is missing, whose value comes from the <code>ontology_term<code> table</li>
 * <li><code>parameter_status_message</code> - a free-text field further describing
 * the reason the data is missing</li></ul>
 * If <code>missing</code> equals 0, no data is missing. The <code>parameter_status</code> and <code>parameter_status_message</code>
 *      fields should both be null or empty. A warning should be issued if they are not.
 * If <code>missing<code> equals 1, data is missing. The <code>parameter_status</code> should not be null.
 *      It should contain a term matching one of the values from the <code>ontology_term</code> table.
 *      This is the only data this field should contain. A warning should be issued if there is no such term.
 *      The <code>parameter_status_message</code> field may be null, empty, or not empty.
 *
 * @author mrelac
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
public class ObservationHealthcheck {

    private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private ObservationMissingOntologyTermRepository observationMissingOntologyTermRepository;
    private ObservationMissingNotMissingCountRepository observationMissingNotMissingCountRepository;


    /**
     * When <code>missing</code> is zero, <code>parameter_status</code> and
     * <code>parameter_status_message</code> should both be null/empty. Issue
     * a warning if they are not, and display some useful debugging information.
     * @throws SQLException
     */
    @Test
    public void ObservationIsNotMissingAndParameterStatusAndParameterStatusMessageAreEmpty() {
        String testName = "testObservationIsNotMissingAndParameterStatusAndParameterStatusMessageAreEmpty";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date start = new Date();

        System.out.println(dateFormat.format(start) + ": " + testName + " started.");

        List<ObservationMissingNotMissingCount> data = observationMissingNotMissingCountRepository.getObservationIsNotMissingAndParameterStatusAndParameterStatusMessageAreEmpty();
        if ( ! data.isEmpty()) {
            String format = "%10s %10s %15s %20s %-50s %-100s\n";
            System.out.println("WARNING: there are observations that are missing but that have no parameter_status or parameter_status_message values:");
            System.out.printf(format, "missing", "count", "organisation_id", "observation_type", "parameter_status", "parameter_status_message");
            data
                    .stream()
                    .map(d -> System.out.printf(format, d.getMissing(), d.getCount(), d.getOrganisationId(), d.getObservationType(), d.getParameterStatus(), d.getParameterStatusMessage()));
            fail("There were parameter status values for not-missing data");
        } else {
            System.out.println("SUCCESS: " + testName);
        }
    }

    /**
     *
     * When <code>missing</code> is one, <code>parameter_status</code> should
     * contain a controlled vocabulary message, taken from the <code>ontology_term</code>
     * table, describing the reason the data is missing. This field must not be
     * null/empty. Issue a warning if it is. The <code>parameter_status_message</code>
     * may or may not be empty.
     * @throws SQLException
     */
    @Test
    public void testObservationIsMissingAndParameterStatusIsNotEmpty() {
        String testName = "testObservationIsMissingAndParameterStatusIsNotEmpty";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date start = new Date();

        System.out.println(dateFormat.format(start) + ": " + testName + " started.");

        List<ObservationMissingNotMissingCount> data = observationMissingNotMissingCountRepository.getObservationIsMissingAndParameterStatusIsNotEmpty();
        if ( ! data.isEmpty()) {
            String format = "%10s %10s %15s %20s %-50s %-100s\n";
            System.out.println("WARNING: there are observations that are not missing but that have non-empty parameter_status values:");
            System.out.printf(format, "missing", "count", "organisation_id", "observation_type", "parameter_status", "parameter_status_message");
            data
                    .stream()
                    .map(d -> System.out.printf(format, d.getMissing(), d.getCount(), d.getOrganisationId(), d.getObservationType(), d.getParameterStatus(), d.getParameterStatusMessage()));
            }
        else {
            System.out.println("SUCCESS: " + testName);
        }
    }

    /**
     * This test fetches the list of observation.parameter_status that is not in
     * IMPC ontology_term.acc and prints out the information necessary to resolve
     * the missing terms.
     * @throws SQLException
     */
    @Test
    public void testMissingParameterStatusFromOntologyTerm() {
        String testName = "testMissingParameterStatusFromOntologyTerm";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date start = new Date();

        System.out.println(dateFormat.format(start) + ": " + testName + " started.");

        List<ObservationMissingOntologyTerm> data = observationMissingOntologyTermRepository.getParameterStatusMissingFromOntologyTerms();
        if ( ! data.isEmpty()) {
            String format = "%50s %10s %15s %20s\n";
            System.out.println("WARNING: there are ontology.parameter_status terms that do not exist in ontology_term.acc:");
            System.out.printf(format, "parameter_status", "acc", "organisation_id", "observation_type");
            data
                    .stream()
                    .map(d -> System.out.printf(format, d.getParameterStatus(), d.getAcc(), d.getOrganisationId(), d.getObservationType()));
        }
        else {
            System.out.println("SUCCESS: " + testName);
        }
    }
}