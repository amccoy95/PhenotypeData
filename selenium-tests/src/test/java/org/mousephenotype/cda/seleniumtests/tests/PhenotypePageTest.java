 /**
 * Copyright © 2011-2014 EMBL - European Bioinformatics Institute
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
 */

package org.mousephenotype.cda.seleniumtests.tests;

 import org.apache.commons.lang3.StringUtils;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.junit.*;
 import org.junit.runner.RunWith;
 import org.mousephenotype.cda.db.dao.PhenotypePipelineDAO;
 import org.mousephenotype.cda.seleniumtests.support.PhenotypePage;
 import org.mousephenotype.cda.seleniumtests.support.PhenotypeProcedure;
 import org.mousephenotype.cda.seleniumtests.support.TestUtils;
 import org.mousephenotype.cda.solr.service.MpService;
 import org.mousephenotype.cda.solr.service.PostQcService;
 import org.mousephenotype.cda.utilities.CommonUtils;
 import org.mousephenotype.cda.utilities.RunStatus;
 import org.openqa.selenium.*;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.test.SpringApplicationConfiguration;
 import org.springframework.core.env.Environment;
 import org.springframework.test.context.TestPropertySource;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

 import javax.validation.constraints.NotNull;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;

 /**
  *
  * @author mrelac
  *
  * Selenium test for phenotype page coverage ensuring each page works as expected.
  */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("file:${user.home}/configfiles/${profile}/test.properties")
@SpringApplicationConfiguration(classes = TestConfig.class)
public class PhenotypePageTest {

    private CommonUtils commonUtils = new CommonUtils();
    protected TestUtils testUtils = new TestUtils();
    private WebDriverWait wait;

    private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private final int TIMEOUT_IN_SECONDS = 120;         // Increased timeout from 4 to 120 secs as some of the graphs take a long time to load.
    private final int THREAD_WAIT_IN_MILLISECONDS = 20;

    private int timeoutInSeconds = TIMEOUT_IN_SECONDS;
    private int threadWaitInMilliseconds = THREAD_WAIT_IN_MILLISECONDS;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

     @NotNull
     @Value("${baseUrl}")
     protected String baseUrl;

     @Autowired
     WebDriver driver;

    @Autowired
    Environment env;

    @Autowired
    @Qualifier("postqcService")
    protected PostQcService genotypePhenotypeService;

    @Autowired
    protected MpService mpService;

    @Autowired
    protected PhenotypePipelineDAO phenotypePipelineDAO;

     @Value("${seleniumUrl}")
     protected String seleniumUrl;


    @Before
    public void setup() {
        if (commonUtils.tryParseInt(System.getProperty("TIMEOUT_IN_SECONDS")) != null)
            timeoutInSeconds = 60;                                            // Use 1 minute rather than the default 4 seconds, as some of the pages take a long time to load.
        if (commonUtils.tryParseInt(System.getProperty("THREAD_WAIT_IN_MILLISECONDS")) != null)
            threadWaitInMilliseconds = commonUtils.tryParseInt(System.getProperty("THREAD_WAIT_IN_MILLISECONDS"));

        wait = new WebDriverWait(driver, timeoutInSeconds);

        try { Thread.sleep(threadWaitInMilliseconds); } catch (Exception e) { }
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Checks the MGI links for the first MAX_MGI_LINK_CHECK_COUNT phenotype ids
     * Fetches all gene IDs (MARKER_ACCESSION_ID) with phenotype associations
     * from the genotype-phenotype core and tests to make sure there is an MGI
     * link for each.
     *
     * <p><em>Limit the number of test iterations by adding an entry to
     * testIterations.properties with this test's name as the lvalue and the
     * number of iterations as the rvalue. -1 means run all iterations.</em></p>
     *
     * @throws SolrServerException
     */
    @Test
//@Ignore
    public void testMGI_MPLinksAreValid() throws SolrServerException {
        RunStatus status = new RunStatus();
        String testName = "testMGI_MPLinksAreValid";
        List<String> phenotypeIds = new ArrayList(genotypePhenotypeService.getAllPhenotypesWithGeneAssociations());
        String target = "";
        String message;
        Date start = new Date();

        int targetCount = testUtils.getTargetCount(env, testName, phenotypeIds, 10);
        testUtils.logTestStartup(logger, this.getClass(), testName, targetCount, phenotypeIds.size());

        // Loop through first targetCount phenotype MGI links, testing each one for valid page load.
        int i = 0;
        for (String phenotypeId : phenotypeIds) {
            if (i >= targetCount) {
                break;
            }

            i++;

            WebElement phenotypeLink;

            target = baseUrl + "/phenotypes/" + phenotypeId;
            logger.debug("phenotype[" + i + "] URL: " + target);

            try {
                driver.get(target);
                phenotypeLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.inner a").linkText(phenotypeId)));
            } catch (NoSuchElementException | TimeoutException te) {
                message = "Expected page for MP_TERM_ID " + phenotypeId + "(" + target + ") but found none.";
                status.addError(message);
                commonUtils.sleep(threadWaitInMilliseconds);
                continue;
            }
            try {
                phenotypeLink.click();
                WebElement mgiMpIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='templateBodyInsert']/table/tbody/tr[1]/td[1]")));
                if ( ! mgiMpIdElement.getText().contains("MP term:")) {
                    message = "Expected valid MGI MP page for " + phenotypeId + "(" + target + ").";
                    status.addError(message);
                }

            } catch (Exception e) {
                message = "EXCEPTION processing target URL " + target + ": " + e.getLocalizedMessage();
                status.addError(message);
            }
        }

        testUtils.printEpilogue(testName, start, status, targetCount, phenotypeIds.size());
    }

    /**
     * Fetches all phenotype IDs from the genotype-phenotype core and
     * tests to make sure there is a valid phenotype page for each.
     *
     * <p><em>Limit the number of test iterations by adding an entry to
     * testIterations.properties with this test's name as the lvalue and the
     * number of iterations as the rvalue. -1 means run all iterations.</em></p>
     *
     * @throws SolrServerException
     */
    @Test
//@Ignore
    public void testPageForEveryMPTermId() throws SolrServerException {
        String testName = "testPageForEveryMPTermId";
        List<String> phenotypeIds = new ArrayList(mpService.getAllPhenotypes());
        phenotypeIdsTestEngine(testName, phenotypeIds);
    }

    /**
     * Fetches all top-level phenotype IDs from the genotype-phenotype core and
     * tests to make sure there is a valid phenotype page for each.
     *
     * <p><em>Limit the number of test iterations by adding an entry to
     * testIterations.properties with this test's name as the lvalue and the
     * number of iterations as the rvalue. -1 means run all iterations.</em></p>
     *
     * @throws SolrServerException
     */
    @Test
//@Ignore
    public void testPageForEveryTopLevelMPTermId() throws SolrServerException {
        String testName = "testPageForEveryTopLevelMPTermId";
        List<String> phenotypeIds = new ArrayList(genotypePhenotypeService.getAllTopLevelPhenotypes());

        phenotypeIdsTestEngine(testName, phenotypeIds);
    }

    /**
     * Fetches all intermediate-level phenotype IDs from the genotype-phenotype
     * core and tests to make sure there is a valid phenotype page for each.
     *
     * <p><em>Limit the number of test iterations by adding an entry to
     * testIterations.properties with this test's name as the lvalue and the
     * number of iterations as the rvalue. -1 means run all iterations.</em></p>
     *
     * @throws SolrServerException
     */
    @Test
//@Ignore
    public void testPageForEveryIntermediateLevelMPTermId() throws SolrServerException {
        String testName = "testPageForEveryIntermediateLevelMPTermId";
        List<String> phenotypeIds = new ArrayList(genotypePhenotypeService.getAllIntermediateLevelPhenotypes());

        phenotypeIdsTestEngine(testName, phenotypeIds);
    }

    /**
     * Tests that a sensible page is returned for an invalid phenotype id.
     *
     * @throws SolrServerException
     */
//@Ignore
    @Test
    public void testInvalidMpTermId() throws SolrServerException {
        RunStatus status = new RunStatus();
        String testName = "testInvalidMpTermId";
        String target = "";
        String message;
        Date start = new Date();
        String phenotypeId = "junkBadPhenotype";
        final String EXPECTED_ERROR_MESSAGE = "Oops! junkBadPhenotype is not a valid mammalian phenotype identifier.";

        testUtils.logTestStartup(logger, this.getClass(), testName, 1, 1);

        boolean found = false;
        target = baseUrl + "/phenotypes/" + phenotypeId;

        try {
            driver.get(target);
            List<WebElement> phenotypeLinks = (new WebDriverWait(driver, timeoutInSeconds))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.node h1")));
            if (phenotypeLinks == null) {
                message = "Expected error page for MP_TERM_ID " + phenotypeId + "(" + target + ") but found none.";
                status.addError(message);
            }
            for (WebElement div : phenotypeLinks) {
                if (div.getText().equals(EXPECTED_ERROR_MESSAGE)) {
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            message = "Timeout: Expected error page for MP_TERM_ID " + phenotypeId + "(" + target + ") but found none.";
            status.addError(message);
        }

        if (found && ( ! status.hasErrors())) {
            status.successCount++;
        } else {
            message = "Expected error page for MP_TERM_ID " + phenotypeId + "(" + target + ") but found none.";
            status.addError(message);
        }

        testUtils.printEpilogue(testName, start, status, 1, 1);
    }

     // Test the top section: Definition, synonyms, mapped hp terms, procedures, mpId.
//@Ignore
    @Test
    public void testTopSection() throws SolrServerException {
        String testName = "testTopSection";
        Date start = new Date();
        RunStatus masterStatus = new RunStatus();
        String[] targets = new String[] {
                  baseUrl + "/phenotypes/MP:0000172"                // 0 synonyms, 0 mapped hp terms, 0 procedures
                , baseUrl + "/phenotypes/MP:0000022"                // 0 synonyms, 1 mapped hp term,  1 procedure
                , baseUrl + "/phenotypes/MP:0000120"                // 1 synonym,  0 mapped hp terms, 0 procedures
                , baseUrl + "/phenotypes/MP:0000013"                // 1 synonym,  1 mapped hp term,  2 procedures
                , baseUrl + "/phenotypes/MP:0000023"                // 4 synonyms, 3 mapped hp terms, 0 procedures
                , baseUrl + "/phenotypes/MP:0005202"                // 4 synonyms, 1 mapped hp term,  1 procedure
        };

        String[] definitions = new String[] {
                  "increased or decreased number of cells that make up the core cavities of bones when compared to controls"
                , "any anomaly in the characteristic surface outline or contour of the external ear"
                , "perturbations in the normal patterned arrangement of the teeth or alignment of the jaw, resulting in the incorrect position of biting or chewing surfaces of the upper and lower teeth"
                , "alterations in the normal placement of body fat"
                , "anomaly in the space between or the placement of the outer ears"
                , "mild impairment of consciousness resulting in reduced alertness and awareness and/or sluggish behavior or inactivity; can be due to generalized brain dysfunction"
        };
        String[][] synonyms = new String[] [] {
                  null
                , null
                , new String[] { "misaligned teeth" }
                , new String[] { "abnormal fat distribution" }
                , new String[] { "abnormal ear distance/ position", "abnormal pinnae position", "abnormal pinna position", "abnormal position of pinna" }
                , new String[] { "listlessness", "torpidity", "torpor", "languor" }
        };
        String[][] mappedHpTerms = new String[] [] {
                  null
                , new String[] { "Abnormality of the pinna" }
                , null
                , new String[] { "Abnormality of adipose tissue" }
                , new String[] { "Posteriorly rotated ears", "Protruding ear", "Low-set ears" }
                , new String[] { "Lethargy" }
        };
        PhenotypeProcedure[][] procedures = new PhenotypeProcedure[][] {
                  { null }
                , { new PhenotypeProcedure("Dysmorphology (ESLIM, v1)", "/impress/impress/displaySOP/1") }
                , { null }
                , { new PhenotypeProcedure("Dysmorphology (ESLIM, v1)", "/impress/impress/displaySOP/1"),
                    new PhenotypeProcedure("Dysmorphology (M-G-P, v1)", "/impress/impress/displaySOP/48") }
                , { null }
                , { new PhenotypeProcedure("Combined SHIRPA and Dysmorphology (IMPC, v3)", "/impress/impress/displaySOP/186"),
                    new PhenotypeProcedure("Combined SHIRPA and Dysmorphology (IMPC, v1)", "/impress/impress/displaySOP/82"),
                    new PhenotypeProcedure("Combined SHIRPA and Dysmorphology (IMPC, v2)", "/impress/impress/displaySOP/155"),}
        };

        testUtils.logTestStartup(logger, this.getClass(), testName, targets.length, targets.length);

        for (int i = 0; i < targets.length; i++) {
            String target = targets[i];
            System.out.println("phenotype[" + i + "] URL: " + target);
            RunStatus status = new RunStatus();

            try {
                List<String> synonymList = (synonyms[i] == null ? null : Arrays.asList(synonyms[i]));
                List<PhenotypeProcedure> procedureList = (procedures[i] == null ? null : Arrays.asList(procedures[i]));
                List<String> mappedHpTermList = (mappedHpTerms[i] == null ? null : Arrays.asList(mappedHpTerms[i]));
                status = testTopSectionEngine(target, definitions[i], synonymList, mappedHpTermList, procedureList);

            } catch (Exception e) {
                status.addError("EXCEPTION: " + e.getLocalizedMessage() + "\nURL: " + target);
            }

            if ( ! status.hasErrors()) {
                status.successCount++;
            } else {
                System.out.println(status.toStringErrorMessages());
            }

            masterStatus.add(status);
        }

        testUtils.printEpilogue(testName, start, masterStatus, definitions.length, definitions.length);
    }

    // PRIVATE METHODS

     /**
      * Engine to test the top part (first section) of the phenotype page.
      *
      * @param target url of mp id to test
      * @param expectedDefinition Expected definition text. (matches using 'contains')
      * @param expectedSynonyms list of expected synonyms. May be null or empty.
      * @param expectedMappedHpTerms list of expected mapped hp terms. May be null or empty.
      * @param expectedProcedures list of expected procedure groups (internal class in <code>PhenotypePage</code>). May
      *                           be null or empty.
      * @return status
      */
     private RunStatus testTopSectionEngine(String target, String expectedDefinition, List<String> expectedSynonyms, List<String> expectedMappedHpTerms, List<PhenotypeProcedure> expectedProcedures) {
         RunStatus status = new RunStatus();

         try {
             PhenotypePage phenotypePage = new PhenotypePage(driver, wait, target, phenotypePipelineDAO, baseUrl);

             // Definition
             String definition = phenotypePage.getDefinition();
             if ( ! definition.contains(expectedDefinition)) {
                 status.addError("Expected definition '" + expectedDefinition + "'. but found '" + definition + ".");
             }

             // Synonyms
             if ((expectedSynonyms != null) && ( ! expectedSynonyms.isEmpty())) {
                 List<String> synonyms = phenotypePage.getSynonyms();
                 synonyms.removeAll(expectedSynonyms);
                 if ( ! synonyms.isEmpty()) {
                    status.addError("Unexpected synonyms: '" + StringUtils.join(synonyms, ", ") + "'");
                 }
             }

            // Mapped HP terms
             if ((expectedMappedHpTerms != null) && ( ! expectedMappedHpTerms.isEmpty())) {
                 List<String> mappedHpTerms = phenotypePage.getMappedHpTerms();
                 mappedHpTerms.removeAll(expectedMappedHpTerms);
                 if ( ! mappedHpTerms.isEmpty()) {
                    status.addError("Unexpected mappedHpTerms: '" + StringUtils.join(mappedHpTerms, ", ") + "'");
                 }
             }

             // Procedures
             if ((expectedProcedures != null) && ( ! expectedProcedures.isEmpty())) {
                 List<PhenotypeProcedure> procedures = phenotypePage.getProcedures();
                  procedures.removeAll(expectedProcedures);
                  if ( ! procedures.isEmpty()) {
                     status.addError("Unexpected procedures: '" + StringUtils.join(procedures, ", ") + "'");
                  }
              }

         } catch (Exception e) {
             status.addError("EXCEPTION: " + e.getLocalizedMessage() + "\nURL: " + target);
         }
         return status;
     }


    private void phenotypeIdsTestEngine(String testName, List<String> phenotypeIds) throws SolrServerException {
        RunStatus status = new RunStatus();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String target;
        List<String> errorList = new ArrayList();
        List<String> successList = new ArrayList();
        List<String> exceptionList = new ArrayList();
        Date start = new Date();
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);

        int targetCount = testUtils.getTargetCount(env, testName, phenotypeIds, 10);
        testUtils.logTestStartup(logger, this.getClass(), testName, targetCount, phenotypeIds.size());

        // Loop through all phenotypes, testing each one for valid page load.
        int i = 0;
        for (String phenotypeId : phenotypeIds) {
            int errorCount = 0;
            if (i >= targetCount) {
                break;
            }

            WebElement mpLinkElement = null;
            target = baseUrl + "/phenotypes/" + phenotypeId;
            System.out.println("phenotype[" + i + "] URL: " + target);

            try {
                PhenotypePage phenotypePage = new PhenotypePage(driver, wait, target, phenotypePipelineDAO, baseUrl);
                if (phenotypePage.hasPhenotypesTable()) {
                    mpLinkElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.inner a").linkText(phenotypeId)));
                    RunStatus localStatus = phenotypePage.validate();
                    if (localStatus.hasErrors()) {
                        status.add(localStatus);
                        errorCount++;
                    }
                } else {
                    // Genes that are Phenotype Started but not yet Complete have a placeholder and note that they will be available soon.
                    // Thus, it is not an error if the PhenotypesTable doesn't exist.
                    System.out.println("\tNo PhenotypesTable. Skipping this page ...");
                    continue;
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION processing target URL " + target + ": " + e.getLocalizedMessage());
                errorCount++;
            }

            if (mpLinkElement == null) {
                System.out.println("Expected page for MP_TERM_ID " + phenotypeId + "(" + target + ") but found none.");
                errorCount++;
            }

            if ((errorCount == 0) && ( ! status.hasErrors())) {
                successList.add("SUCCESS: MP_TERM_ID " + phenotypeId + ". URL: " + target);
            } else {
                errorList.add("FAIL: MP_TERM_ID " + phenotypeId + ". URL: " + target);
            }

            i++;
            commonUtils.sleep(100);
        }

        testUtils.printEpilogue(testName, start, status, successList.size(), targetCount, phenotypeIds.size());
    }

}