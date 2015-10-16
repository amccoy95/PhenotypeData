/**
 * Copyright © 2014 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.mousephenotype.cda.seleniumtests.tests;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mousephenotype.cda.db.dao.PhenotypePipelineDAO;
import org.mousephenotype.cda.seleniumtests.support.GenePage;
import org.mousephenotype.cda.seleniumtests.support.PageStatus;
import org.mousephenotype.cda.seleniumtests.support.SeleniumWrapper;
import org.mousephenotype.cda.seleniumtests.support.TestUtils;
import org.mousephenotype.cda.solr.service.GeneService;
import org.mousephenotype.cda.solr.service.dto.GeneDTO;
import org.mousephenotype.cda.utilities.CommonUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mrelac
 *
 * Selenium test for impc images coverage ensuring each page works as expected.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("file:${user.home}/configfiles/${profile}/application.properties")
@SpringApplicationConfiguration(classes = TestConfig.class)
public class ImpcImagesTest {

    private CommonUtils commonUtils = new CommonUtils();
    private WebDriver driver;
    protected TestUtils testUtils = new TestUtils();
    private WebDriverWait wait;

    private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private final int TIMEOUT_IN_SECONDS = 120;         // Increased timeout from 4 to 120 secs as some of the graphs take a long time to load.
    private final int THREAD_WAIT_IN_MILLISECONDS = 20;

    private int timeoutInSeconds = TIMEOUT_IN_SECONDS;
    private int threadWaitInMilliseconds = THREAD_WAIT_IN_MILLISECONDS;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected GeneService geneService;

    @Autowired
    protected PhenotypePipelineDAO phenotypePipelineDAO;

    @Autowired
    protected SeleniumWrapper wrapper;

    @NotNull
    @Value("${baseUrl}")
    protected String baseUrl;


    @PostConstruct
    public void initialise() throws Exception {
        driver = wrapper.getDriver();
    }

    @Before
    public void setup() {
        if (commonUtils.tryParseInt(System.getProperty("TIMEOUT_IN_SECONDS")) != null)
            timeoutInSeconds = commonUtils.tryParseInt(System.getProperty("TIMEOUT_IN_SECONDS"));
        if (commonUtils.tryParseInt(System.getProperty("THREAD_WAIT_IN_MILLISECONDS")) != null)
            threadWaitInMilliseconds = commonUtils.tryParseInt(System.getProperty("THREAD_WAIT_IN_MILLISECONDS"));

        testUtils.printTestEnvironment(driver, wrapper.getSeleniumUrl());
        wait = new WebDriverWait(driver, timeoutInSeconds);

        driver.navigate().refresh();
        commonUtils.sleep(threadWaitInMilliseconds);
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


    // PRIVATE METHODS


    private void geneIdsTestEngine(String testName, List<String> geneIds)
            throws SolrServerException {
        PageStatus status = new PageStatus();
        DateFormat dateFormat = new SimpleDateFormat(TestUtils.DATE_FORMAT);

geneIds = testUtils.removeKnownBadGeneIds(geneIds);

        String target = "";
        List<String> successList = new ArrayList();
        String message;
        Date start = new Date();

        System.out.println(dateFormat.format(start) + ": " + testName
                + " started. Expecting to process " + geneIds.size()
                + " of a total of " + geneIds.size() + " records.");

        // Loop through all genes, testing each one for valid page load.
        int i = 0;
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        for (String geneId : geneIds) {

            target = baseUrl + "/genes/" + geneId;
            logger.debug("gene[" + i + "] URL: " + target);

            try {
                driver.get(target);
                wait.until(ExpectedConditions.presenceOfElementLocated(By
                        .cssSelector("span#enu")));
                GenePage genePage = new GenePage(driver, wait, target, geneId, phenotypePipelineDAO, baseUrl);
                boolean hasImpcImages = genePage.hasImpcImages();
                if ( ! hasImpcImages) {
                    status.addError("No IMPC Images found for " + target);
                    continue;
                }

                List<String> parameters = genePage.getAssociatedImpcImageSections();
                if (parameters.isEmpty()) {
                    status.addError("Parameter list is empty!");
                    continue;
                }

            } catch (NoSuchElementException | TimeoutException te) {
                message = "Expected page for MGI_ACCESSION_ID " + geneId + "("
                        + target + ") but found none.";
                status.addError(message);
                commonUtils.sleep(threadWaitInMilliseconds);
                continue;
            } catch (Exception e) {
                message = "EXCEPTION processing target URL " + target + ": "
                        + e.getLocalizedMessage();
                status.addError(message);
                commonUtils.sleep(threadWaitInMilliseconds);
                continue;
            }

            message = "SUCCESS: MGI_ACCESSION_ID " + geneId + ". URL: "
                    + target;
            successList.add(message);

            commonUtils.sleep(threadWaitInMilliseconds);
            i ++;
        }

        testUtils.printEpilogue(testName, start, status, successList.size(), i, geneIds.size());
    }


    // TESTS


    @Test
//@Ignore
    public void testImpcImagesOnGenePage() throws Exception {

        String testName = "testImpcImagesOnGenePage";
        ArrayList<String> genes = new ArrayList<>();
		// genes.add("Akt2"); should fail on Akt2 as no impc_images

        genes.add("Ccdc120");
        genes.add("Cenpo");
        genes.add("Cwc27");
        genes.add("Eya4");
        genes.add("Htr1b");
        genes.add("Lrp1");
        genes.add("Osm");
        genes.add("Ppp2r2b");
        genes.add("Prkab1");
        genes.add("Rhbdl1");
        genes.add("Rxfp2");
        genes.add("Snrnp200");
        genes.add("Tpgs2");
        genes.add("Wee1");

        genes.add("Abcb11");
        genes.add("Baz1a");
        genes.add("C3");
        genes.add("Ddx41");
        genes.add("Dnajb7");
        genes.add("Idh1");
        genes.add("Ovgp1");
        genes.add("Palb2");
        genes.add("Pipox");
        genes.add("Pkd2l2");
        genes.add("Plekhm1");
        genes.add("Stk16");
        genes.add("Vps13d");
        String geneString = genes.toString();
//		System.out.println(geneString);
        String orQuery = geneString.replace(",", " OR ");
        System.out.println(orQuery);
        List<String> geneIds = new ArrayList<>();

        for (String gene : genes) {
            GeneDTO geneDto = geneService.getGeneByGeneSymbol(gene);
            logger.debug("geneDto=" + geneDto.getMgiAccessionId());
            geneIds.add(geneDto.getMgiAccessionId());
        }
        geneIdsTestEngine(testName, geneIds);

    }

    @Test
//@Ignore
    public void testImpcImagesOnaSpecificGenePage() throws Exception {

        String testName = "testImpcImagesOnGenePage";
        ArrayList<String> genes = new ArrayList<>();
        // genes.add("Akt2"); should fail on Akt2 as no impc_images
        genes.add("Baz1a");
        String geneString = genes.toString();
        System.out.println(geneString);
        String orQuery = geneString.replace(",", " OR ");
        System.out.println(orQuery);
        List<String> geneIds = new ArrayList<>();
		// genes.add("Wee1");
        //
        // GeneDTO geneDto = geneService.getGeneByGeneSymbol(gene);
        // System.out.println("geneDto=" + geneDto.getMgiAccessionId());
        // geneIds.add(geneDto.getMgiAccessionId());
        // }

    }
}