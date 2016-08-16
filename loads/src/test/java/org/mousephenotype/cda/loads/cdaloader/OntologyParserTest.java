package org.mousephenotype.cda.loads.cdaloader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mousephenotype.cda.db.pojo.OntologyTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TestConfigLoaders.class} )
@TestPropertySource(locations = {"file:${user.home}/configfiles/${profile:dev}/test.properties"})
public class OntologyParserTest {

    private OntologyParser ontologyParser;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    @Value("${owlpath}")
    protected String owlpath;

    @Before
    public void setUp() throws Exception {

    }
    
    @Ignore
    @Test
    public void testOwlOntologyDownloads() throws Exception {
        String message;
        List<Exception> exception = new ArrayList();
        File owlpathFile = new File(owlpath);
        File[] owlFiles = owlpathFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".owl");
            }
        });

        String prefix;
        for (File file : owlFiles) {
            prefix = file.getName().replace(".owl", "").toUpperCase();
            try {
                ontologyParser = new OntologyParser(file.getPath(), prefix);
            } catch (Exception e) {
                message = "[FAIL - " + prefix + "] Exception in " + file.getPath() + "(" + prefix + "): " + e.getLocalizedMessage();
                exception.add(e);
                System.out.println(message + "\n");
                continue;
            }
            List<OntologyTerm> terms = ontologyParser.getTerms();
            if (terms.size() > 700) {
                System.out.println("[PASS - " + prefix + "] - " + file.getPath() + ". Size: " + terms.size());
            } else {
                System.out.println("[FAIL - " + prefix + "] - " + file.getPath() + ". Size: " + terms.size());
            }
            System.out.println();
        }

        if (! exception.isEmpty()) {
            throw exception.get(0);            // Just throw the first one.
        }
    }
    
    @Test
    public void testEFO() // Because it had that IRI used twice, once with ObjectProperty and once with AnnotationProperty RO_0002200
    throws Exception{
         ontologyParser = new OntologyParser(owlpath + "/efo.owl", "EFO");
        List<OntologyTerm> terms = ontologyParser.getTerms();
        if (terms.isEmpty())
            throw new Exception("testDeprecated: term list is empty!");
    	
    }
    
    
    @Test 
    public void testDeprecated() 
    throws Exception{

        // FIXME FIXME FIXME TEST ALL COMPONENT PIECES!!!


        List<Exception> exception = new ArrayList();
        ontologyParser = new OntologyParser(owlpath + "/mp.owl", "MP");
        List<OntologyTerm> terms = ontologyParser.getTerms();
        if (terms.isEmpty())
            throw new Exception("testDeprecated: term list is empty!");

        boolean found0006374 = false;
        boolean found0002977 = false;
        for (OntologyTerm term : terms){

            /*
             * Test for term MP:0006374 with replacement IDs
             */
        	if (term.getId().getAccession().equals("MP:0006374")){
                found0006374 = true;
        		if(!term.getIsObsolete()){
        			 String message = "[FAIL] Exception in testDeprecated (" +term.getId().getAccession() + " is not marked as deprecated)";
        			 exception.add(new Exception(message));
        		}
        		if (term.getReplacementAcc() == null || !term.getReplacementAcc().equals("MP:0008996")){
        			String message = "[FAIL] Exception in testDeprecated (" +term.getId().getAccession() + " does not have the correct replacement term)";
        			exception.add(new Exception(message));
        		}
        	}

            /*
             * Test for term MP:0002977 with consider IDs
             */
        	if (term.getId().getAccession().equals("MP:0002977")){
                found0002977 = true;
        		if (term.getConsiderIds().size() == 0){
        			String message = "[FAIL] Exception in testDeprecated (" +term.getId().getAccession() + " does not have any consider terms )";
        			exception.add(new Exception(message));
        		} else {
        		    List<String> considerIds = term.getConsiderIds().stream().map(x -> x.getConsiderAccessionId()).collect(Collectors.toList());
        			if (!considerIds.contains("MP:0010241") || !considerIds.contains("MP:0010464") ){
        				String message = "[FAIL] Exception in testDeprecated (" +term.getId().getAccession() + " does not contain the consider terms expected )";
            			exception.add(new Exception(message));
        			}
        		}
        	}

        	// Short circuit if both terms have been seen
            if (found0002977 && found0006374) {
                break;
            }
        }

        if ( ! found0006374) {
            String message = "[FAIL] Expected to find class MP:0006374 but it was not found.";
            exception.add(new Exception(message));
        }

        if ( ! found0002977) {
            String message = "[FAIL] Expected to find class MP:0002977 but it was not found.";
            exception.add(new Exception(message));
        }
        
        if ( ! exception.isEmpty()){
        	throw exception.get(0);            // Just throw the first one because Mike does so
        }
    	
    }
        
}