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
 * This selenium test walks through all phenotype pages, compiling a list of phenotype pages with:
 * <ul>
 * <li>only phenotype table (and no graphs)</li>
 * <li>only images (and no phenotype table)</li>
 * <li>both a phenotype table and one or more images</li>
 * <li>no phenotype table and no images</li>
 * </ul>
 */

package org.mousephenotype.cda.seleniumtests.tests;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mousephenotype.cda.seleniumtests.support.PageStatus;
import org.mousephenotype.cda.seleniumtests.support.SeleniumWrapper;
import org.mousephenotype.cda.seleniumtests.support.TestUtils;
import org.mousephenotype.cda.solr.service.MpService;
import org.mousephenotype.cda.utilities.CommonUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
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
 * Selenium test for phenotype page statistics.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("file:${user.home}/configfiles/${profile}/application.properties")
@SpringApplicationConfiguration(classes = TestConfig.class)
public class PhenotypePageStatistics {

    private CommonUtils commonUtils = new CommonUtils();
    private WebDriver driver;
    protected TestUtils testUtils = new TestUtils();
    private WebDriverWait wait;

    private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private final int TIMEOUT_IN_SECONDS = 120;         // Increased timeout from 4 to 120 secs as some of the graphs take a long time to load.
    private final int THREAD_WAIT_IN_MILLISECONDS = 20;

    private int timeoutInSeconds = TIMEOUT_IN_SECONDS;
    private int threadWaitInMilliseconds = THREAD_WAIT_IN_MILLISECONDS;

    private final String NO_PHENOTYPE_ASSOCIATIONS = "Phenotype associations to genes and alleles will be available once data has completed quality control.";
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Environment env;

    @Autowired
    protected MpService mpService;

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

    /**
     * Walks the phenotype core collecting the count of: [phenotype] table only,
     * image(s) only, both, and none.
     *
     * @throws SolrServerException
     */
    @Test
    public void testCollectTableAndImageStatistics() throws SolrServerException {
        PageStatus status = new PageStatus();
        String testName = "testCollectTableAndImageStatistics";
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        List<String> phenotypeIds = new ArrayList(mpService.getAllPhenotypes());
        String target = "";
        List<String> errorList = new ArrayList();
        List<String> successList = new ArrayList();
        List<String> exceptionList = new ArrayList();

        List<String> phenotypeTableOnly = new ArrayList();
        List<String> imagesOnly = new ArrayList();
        List<String> both = new ArrayList();
        String message;
        Date start = new Date();
        Date stop;

        int pagesWithPhenotypeTableCount = 0;
        int pagesWithImageCount = 0;
        int pagesWithBoth = 0;
        List<String> urlsWithNeitherPhenotypeTableNorImage = new ArrayList();

        int targetCount = testUtils.getTargetCount(env, testName, phenotypeIds, 10);
        System.out.println(dateFormat.format(start) + ": " + testName + " started. Expecting to process " + targetCount + " of a total of " + phenotypeIds.size() + " records.");

        // Loop through first targetCount phenotype MGI links, testing each one for valid page load.
        int i = 0;
        for (String phenotypeId : phenotypeIds) {
            if (i >= targetCount) {
                break;
            }
            i++;

            boolean found = false;
//if (i == 1) phenotypeId = "MP:0001304";

            target = baseUrl + "/phenotypes/" + phenotypeId;
            logger.debug("phenotype[" + i + "] URL: " + target);

            try {
                driver.get(target);
                (new WebDriverWait(driver, timeoutInSeconds))
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1#top")));
                found = true;
            } catch (NoSuchElementException | TimeoutException te) {
                message = "Expected page for MP_TERM_ID " + phenotypeId + "(" + target + ") but found none.";
                status.addError(message);
                continue;
            }
            try {
                boolean hasPhenotypeTable = false;
                boolean hasImage = false;

                // Are there any phenotype associations?
                List<WebElement> elementList = driver.findElements(By.cssSelector("div.alert"));

                hasPhenotypeTable = ! testUtils.contains(elementList, NO_PHENOTYPE_ASSOCIATIONS);

                // Are there any images?
                elementList = driver.findElements(By.cssSelector("h2#section"));
                if (testUtils.contains(elementList, "Images")) {
                    List<WebElement> imagesAccordion = driver.findElements(By.cssSelector("div.accordion-body ul li"));
                    if (imagesAccordion.isEmpty()) {
                        message = "ERROR: Found Image tag but there were no image links";
                        status.addError(message);
                    } else {
                        hasImage = true;
                    }
                }

                if (hasPhenotypeTable && hasImage) {
                    pagesWithBoth++;
                    both.add(driver.getCurrentUrl());
                } else if (hasPhenotypeTable) {
                    pagesWithPhenotypeTableCount++;
                    phenotypeTableOnly.add(driver.getCurrentUrl());
                } else if (hasImage) {
                    pagesWithImageCount++;
                    imagesOnly.add(driver.getCurrentUrl());
                } else {
                    urlsWithNeitherPhenotypeTableNorImage.add(driver.getCurrentUrl());
                }
            } catch (Exception e) {
                message = "EXCEPTION processing target URL " + target + ": " + e.getLocalizedMessage();
                status.addError(message);
            }

            if (found) {
                message = "SUCCESS: MGI link OK for " + phenotypeId + ". Target URL: " + target;
                successList.add(message);
            } else {
                message = "h1 with id 'top' not found.";
                status.addError(message);
            }

            commonUtils.sleep(threadWaitInMilliseconds);
        }

        System.out.println("\nPhenotype pages with tables but no images: " + pagesWithPhenotypeTableCount);
        for (String s : phenotypeTableOnly) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("Phenotype pages with images but no tables: " + pagesWithImageCount);
        for (String s : imagesOnly) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("Phenotype pages with both tables and images: " + pagesWithBoth);
        for (String s : both) {
            System.out.println(s);
        }
        System.out.println();

        if ( ! urlsWithNeitherPhenotypeTableNorImage.isEmpty()) {
            System.out.println("WARNING: The following " + urlsWithNeitherPhenotypeTableNorImage.size() + " results had neither phenotype table nor images:");
            System.out.println("WARNING: Phenotype pages with neither phenotype table nor images: " + urlsWithNeitherPhenotypeTableNorImage.size());
            for (String s : urlsWithNeitherPhenotypeTableNorImage) {
                System.out.println("\t" + s);
            }
        }

        testUtils.printEpilogue(testName, start, status, successList.size(), targetCount, phenotypeIds.size());
    }

}
