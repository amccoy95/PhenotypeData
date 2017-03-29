package uk.ac.ebi.phenotype.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONObject;
import org.mousephenotype.cda.owl.OntologyParser;
import org.mousephenotype.cda.owl.OntologyTermDTO;
import org.mousephenotype.cda.solr.service.dto.MpDTO;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ckchen on 17/03/2017.
 */

@Component
public class Loader implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("komp2DataSource")
    DataSource komp2DataSource;

    @Autowired
    @Qualifier("phenodigmDataSource")
    DataSource phenodigmDataSource;

    @NotNull
    @Value("${allele2File}")
    private String pathToAlleleFile;

    @NotNull
    @Value("${human2mouseFilename}")
    private String pathToHuman2mouseFilename;

    @NotNull
    @Value("${mpListPath}")
    private String mpListPath;

    @NotNull
    @Value("${owlpath}")
    protected String owlpath;


//    @Autowired
//    @Qualifier("allele2Core")
//    private SolrClient allele2Core;


    @Autowired
    GeneRepository geneRepository;

    @Autowired
    AlleleRepository alleleRepository;

    @Autowired
    EnsemblGeneIdRepository ensemblGeneIdRepository;

    @Autowired
    MarkerSynonymRepository markerSynonymRepository;

    @Autowired
    HumanGeneSymbolRepository humanGeneSymbolRepository;

    @Autowired
    MpRepository mpRepository;

    @Autowired
    HpRepository hpRepository;

    @Autowired
    OntoSynonymRepository ontoSynonymRepository;

    Map<String, Gene> loadedGenes = new HashMap<>();
    Map<String, Gene> loadedGeneSymbols = new HashMap<>();

    private OntologyParser mpHpParser;
    private OntologyParser mpParser;

    private static final int LEVELS_FOR_NARROW_SYNONYMS = 2;

    public Loader() {}

    @Override
    public void run(String... strings) throws Exception {

        Map<String, Gene> loadedGenes = new HashMap<>();

//        geneRepository.deleteAll();
//        alleleRepository.deleteAll();
//        ensemblGeneIdRepository.deleteAll();
//        markerSynonymRepository.deleteAll();
//        humanGeneSymbolRepository.deleteAll();

        Connection komp2Conn = komp2DataSource.getConnection();
        Connection diseaseConn = phenodigmDataSource.getConnection();

        // loading Gene, Allele, EnsemblGeneId, MarkerSynonym
        // based on Peter's allele2 flatfile
        // loadGene1();
        //loadHumanOrtholog();

        loadPhenotypes();

    }
    protected Set<String> getWantedMPIds() throws SQLException {

        // Select MP terms from images too
        Set<String> wantedIds = new HashSet<>();

        // Get mp terms from Sanger images
        PreparedStatement statement = komp2DataSource.getConnection().prepareStatement("SELECT DISTINCT (UPPER(TERM_ID)) AS TERM_ID, (UPPER(TERM_NAME)) as TERM_NAME FROM  IMA_IMAGE_TAG iit INNER JOIN ANN_ANNOTATION aa ON aa.FOREIGN_KEY_ID=iit.ID");
        ResultSet res = statement.executeQuery();
        while (res.next()) {
            String r = res.getString("TERM_ID");
            if (r.startsWith("MP:")) {
                wantedIds.add(r);
            }
        }

        //All MP terms we can have annotations to (from IMPRESS)
        wantedIds.addAll(getOntologyIds(5, komp2DataSource));

        return wantedIds;
    }

    public List<String> getOntologyIds(Integer ontologyId, DataSource ds) throws SQLException{

        PreparedStatement statement = ds.getConnection().prepareStatement(
                "SELECT DISTINCT(ontology_acc) FROM phenotype_parameter_ontology_annotation ppoa WHERE ppoa.ontology_db_id=" + ontologyId);
        ResultSet res = statement.executeQuery();
        List<String> terms = new ArrayList<>();

        while (res.next()) {
            terms.add(res.getString("ontology_acc"));
        }

        return terms;
    }

    public void loadPhenotypes() throws IOException, OWLOntologyCreationException, OWLOntologyStorageException, SQLException, URISyntaxException, SolrServerException {
        long begin = System.currentTimeMillis();

//        mpCalls = mpIndexer.populateMpCallMaps();
        final List<String> TOP_LEVEL_MP_TERMS = new ArrayList<>(Arrays.asList("MP:0010768", "MP:0002873", "MP:0001186", "MP:0003631",
                "MP:0005367",  "MP:0005369", "MP:0005370", "MP:0005371", "MP:0005377", "MP:0005378", "MP:0005375", "MP:0005376",
                "MP:0005379", "MP:0005380",  "MP:0005381", "MP:0005384", "MP:0005385", "MP:0005382", "MP:0005388", "MP:0005389", "MP:0005386",
                "MP:0005387", "MP:0005391",  "MP:0005390", "MP:0005394", "MP:0005397", "MP:0010771"));


        OntologyParser mpParser = new OntologyParser(owlpath + "/mp.owl", "MP", TOP_LEVEL_MP_TERMS, getWantedMPIds());

        System.out.println("Loaded mp parser");

        mpHpParser = new OntologyParser(owlpath + "/mp-hp.owl", "MP", null, null);
        System.out.println("Loaded mp hp parser");

        int mpCount = 0;
        for (String mpId: mpParser.getTermsInSlim()) {

            OntologyTermDTO mpDTO = mpParser.getOntologyTerm(mpId);
            String termId = mpDTO.getAccessionId();

            Mp ph = mpRepository.findByMpId(termId);
            if (ph == null){
                ph = new Mp();
                ph.setMpId(termId);
            }
            if (ph.getMpTerm() == null) {
                ph.setMpTerm(mpDTO.getName());
            }
            if (ph.getMpDefinition() == null) {
                ph.setMpDefinition(mpDTO.getDefinition());
            }

            if (ph.getOntoSynonyms() == null) {
                for (String mpsym : mpDTO.getSynonyms()) {
                    OntoSynonym ms = new OntoSynonym();
                    ms.setOntoSynonym(mpsym);
                    ms.setMousePhenotype(ph);
                    ontoSynonymRepository.save(ms);

                    if (ph.getOntoSynonyms() == null) {
                        ph.setOntoSynonyms(new HashSet<OntoSynonym>());
                    }
                    ph.getOntoSynonyms().add(ms);
                }
            }

            // PARENT
            if (ph.getMpParentIds() == null) {
                for (String parId : mpDTO.getParentIds()) {
                    Mp thisPh = mpRepository.findByMpId(parId);
                    if (thisPh == null) {
                        thisPh = new Mp();
                    }
                    thisPh.setMpId(parId);
                    mpRepository.save(thisPh);

                    if (ph.getMpParentIds() == null) {
                        ph.setMpParentIds(new HashSet<Mp>());
                    }
                    ph.getMpParentIds().add(thisPh);
                }
            }

            // MARK MP WHICH IS TOP LEVEL
            if (mpDTO.getTopLevelIds() == null || mpDTO.getTopLevelIds().size() == 0){
                // add self as top level
                ph.setTopLevelStatus(true);
            }

            // add mp-hp mapping using Monarch's mp-hp hybrid ontology
            MpDTO mpn = new MpDTO();
            OntologyTermDTO mpTerm = mpHpParser.getOntologyTerm(termId);
            if (mpTerm == null) {
                logger.error("MP term not found using mpHpParser.getOntologyTerm(termId); where termId={}", termId);
            } else {
                Set<OntologyTermDTO> hpTerms = mpTerm.getEquivalentClasses();
                for (OntologyTermDTO hpTerm : hpTerms) {
                    Set<String> hpIds = new HashSet<>();
                    hpIds.add(hpTerm.getAccessionId());

                    for(String hpid : hpIds){
                        Hp hp = hpRepository.findByHpId(hpid);
                        if (hp == null){
                            hp = new Hp();
                            hp.setHpId(hpid);
                        }
                        hpRepository.save(hp);
                        // term and synonym will be populated via phenodigm
                    }
//
//                    mp.setHpId(new ArrayList(hpIds));
//                    if (hpTerm.getName() != null) {
//                        Set<String> hpNames = new HashSet<>();
//                        hpNames.add(hpTerm.getName());
//                        mp.setHpTerm(new ArrayList(hpNames));
//                    }
//                    if (hpTerm.getSynonyms() != null) {
//                        mp.setHpTermSynonym(new ArrayList(hpTerm.getSynonyms()));
//                    }
                }
                // get the children of MP not in our slim (narrow synonyms)
//                if (isOKForNarrowSynonyms(mpn)) {
//                    mpn.setMpNarrowSynonym(new ArrayList(mpHpParser.getNarrowSynonyms(mpTerm, LEVELS_FOR_NARROW_SYNONYMS)));
//                } else {
//                    mpn.setMpNarrowSynonym(new ArrayList(getRestrictedNarrowSynonyms(mpTerm, LEVELS_FOR_NARROW_SYNONYMS)));
//                }
            }

//            mp.setMpTermSynonym(mpDTO.getSynonyms());
//
//            getMaTermsForMp(mp);
//
//            // this sets the number of postqc/preqc phenotyping calls of this MP
//            addPhenotype1(mp, runStatus);
//            mp.setPhenoCalls(sumPhenotypingCalls(termId));
//            addPhenotype2(mp);
//
//            List<JSONObject> searchTree = browser.createTreeJson(mpDTO, "/data/phenotype/", mpParser, mpGeneVariantCount, TOP_LEVEL_MP_TERMS);
//            mp.setSearchTermJson(searchTree.toString());
//            String scrollNodeId = browser.getScrollTo(searchTree);
//            mp.setScrollNode(scrollNodeId);
//            List<JSONObject> childrenTree = browser.getChildrenJson(mpDTO, "/data/phenotype/", mpParser, mpGeneVariantCount);
//            mp.setChildrenJson(childrenTree.toString());

            mpRepository.save(ph);
            mpCount++;

            if (mpCount % 1000 == 0) {
                logger.info("Added {} mp nodes",  mpCount);
            }
        }


    }

    public void loadHumanOrtholog() throws IOException {

        long begin = System.currentTimeMillis();
        BufferedReader in = new BufferedReader(new FileReader(new File(pathToHuman2mouseFilename)));

        int symcount = 0;

        String line = in.readLine();
        while (line != null) {
            //System.out.println(line);
            String[] array = line.split("\t");

            if (! array[0].isEmpty() && ! array[4].isEmpty()) {

                String humanSym = array[0];
                String mouseSym = array[4];

                // only want IMPC gene symbols
                //if (loadedGeneSymbols.containsKey(mouseSym)) {
                Gene gene = geneRepository.findByMarkerSymbol(mouseSym);
                if (gene != null){

                    symcount++;

                    Set<HumanGeneSymbol> hgset = new HashSet<>();
                    HumanGeneSymbol hgs = new HumanGeneSymbol();
                    hgs.setHumanGeneSymbol(humanSym);
                    hgs.setGene(gene);
                    humanGeneSymbolRepository.save(hgs);


                   // Gene gene = loadedGeneSymbols.get(mouseSym);
                    if (gene.getHumanGeneSymbols() == null) {
                        hgset.add(hgs);
                        gene.setHumanGeneSymbols(hgset);
                    } else {
                        gene.getHumanGeneSymbols().add(hgs);
                    }

                    geneRepository.save(gene);

                    if (symcount % 5000 == 0) {
                        logger.info("Loaded {} HumanGeneSymbol nodes", symcount);
                    }
                }
            }

            line = in.readLine();
        }

        logger.info("Loaded {} HumanGeneSymbol nodes", symcount);

        String job = "HumanGeneSymbol nodes";
        loadTime(begin, System.currentTimeMillis(), job);
    }


    public void loadGene1() throws IOException, SolrServerException {

        long begin = System.currentTimeMillis();

        final Map<String, String> ES_CELL_STATUS_MAPPINGS = new HashMap<>();
        ES_CELL_STATUS_MAPPINGS.put("No ES Cell Production", "Not Assigned for ES Cell Production");
        ES_CELL_STATUS_MAPPINGS.put("ES Cell Production in Progress", "Assigned for ES Cell Production");
        ES_CELL_STATUS_MAPPINGS.put("ES Cell Targeting Confirmed", "ES Cells Produced");


        final Map<String, String> MOUSE_STATUS_MAPPINGS = new HashMap<>();
        MOUSE_STATUS_MAPPINGS.put("Chimeras obtained", "Assigned for Mouse Production and Phenotyping");
        MOUSE_STATUS_MAPPINGS.put("Micro-injection in progress", "Assigned for Mouse Production and Phenotyping");
        MOUSE_STATUS_MAPPINGS.put("Cre Excision Started", "Mice Produced");
        MOUSE_STATUS_MAPPINGS.put("Rederivation Complete", "Mice Produced");
        MOUSE_STATUS_MAPPINGS.put("Rederivation Started", "Mice Produced");
        MOUSE_STATUS_MAPPINGS.put("Genotype confirmed", "Mice Produced");
        MOUSE_STATUS_MAPPINGS.put("Cre Excision Complete", "Mice Produced");
        MOUSE_STATUS_MAPPINGS.put("Phenotype Attempt Registered", "Mice Produced");


        Map<String, Integer> columns = new HashMap<>();

        BufferedReader in = new BufferedReader(new FileReader(new File(pathToAlleleFile)));
        BufferedReader in2 = new BufferedReader(new FileReader(new File(pathToAlleleFile)));

        String[] header = in.readLine().split("\t");
        for (int i = 0; i < header.length; i++){
            columns.put(header[i], i);
        }

        int geneCount = 0;
        int alleleCount = 0;


        String line = in.readLine();
        while (line != null) {
            //System.out.println(line);
            String[] array = line.split("\t", -1);
            if (array.length == 1) {
                continue;
            }

            if (! array[columns.get("latest_project_status")].isEmpty() && array[columns.get("type")].equals("Gene")) {
                String mgiAcc = array[columns.get("mgi_accession_id")];
                Gene gene = geneRepository.findByMgiAccessionId(mgiAcc);
                if (gene == null) {
                    logger.debug("Gene {} not found. Creating Gene with ", mgiAcc);
                    gene = new Gene();
                    gene.setMgiAccessionId(mgiAcc);
                }

                String thisSymbol = null;

                if (! array[columns.get("marker_symbol")].isEmpty()) {
                    thisSymbol = array[columns.get("marker_symbol")];
                    gene.setMarkerSymbol(array[columns.get("marker_symbol")]);
                }
                if (! array[columns.get("feature_type")].isEmpty()) {
                    gene.setMarkerType(array[columns.get("feature_type")]);
                }
                if (! array[columns.get("marker_name")].isEmpty()) {
                    gene.setMarkerName(array[columns.get("marker_name")]);
                }
                if (! array[columns.get("synonym")].isEmpty()) {
                    List<String> syms = Arrays.asList(StringUtils.split(array[columns.get("synonym")], "|"));
                    Set<MarkerSynonym> mss = new HashSet<>();

                    for (String sym : syms) {
                        MarkerSynonym msObj = markerSynonymRepository.findByMarkerSynonym(sym);
                        if (msObj == null) {
                            MarkerSynonym ms = new MarkerSynonym();
                            ms.setMarkerSynonym(sym);
                            ms.setGene(gene);
                            mss.add(ms);
                        }
                    }
                    gene.setMarkerSynonyms(mss);
                }
                if (! array[columns.get("feature_chromosome")].isEmpty()) {
                    gene.setChrId(array[columns.get("feature_chromosome")]);
                    gene.setChrStart(array[columns.get("feature_coord_start")]);
                    gene.setChrEnd(array[columns.get("feature_coord_end")]);
                    gene.setChrStrand(array[columns.get("feature_strand")]);
                }

                if (! array[columns.get("gene_model_ids")].isEmpty()) {
                    String[] ids = StringUtils.split(array[columns.get("gene_model_ids")], "|");
                    for(int j=0; j<ids.length; j++){
                        String thisId = ids[j];
                        if (thisId.startsWith("ensembl_ids") || thisId.startsWith("\"ensembl_ids")){
                            String[] vals = StringUtils.split(thisId, ":");
                            if (vals.length == 2) {
                                String ensgId = vals[1];
                                //System.out.println("Found " + ensgId);
                                EnsemblGeneId ensg = ensemblGeneIdRepository.findByEnsemblGeneId(ensgId);
                                if (ensg == null) {
                                    ensg = new EnsemblGeneId();
                                    ensg.setEnsemblGeneId(ensgId);
                                    ensg.setGene(gene);
                                    ensemblGeneIdRepository.save(ensg);

                                    if (gene.getEnsemblGeneIds() == null) {
                                        Set<EnsemblGeneId> eset = new HashSet<EnsemblGeneId>();
                                        eset.add(ensg);
                                        gene.setEnsemblGeneIds(eset);
                                    } else {
                                        gene.getEnsemblGeneIds().add(ensg);
                                    }
                                }
                            }
                        }
                    }
                }

                geneRepository.save(gene);
                loadedGenes.put(mgiAcc, gene);
                loadedGeneSymbols.put(thisSymbol, gene);

                geneCount++;
                if (geneCount % 5000 == 0) {
                    logger.info("Loaded {} Gene nodes", geneCount);
                }
            }

            line = in.readLine();
        }
        logger.info("Done loading the Type Gene");

        String line2 = in2.readLine();
        while (line2 != null) {
            //System.out.println(line);
            String[] array = line2.split("\t", -1);
            if (array.length == 1) {
                continue;
            }

            String mgiAcc = array[columns.get("mgi_accession_id")];

            if (array[columns.get("type")].equals("Allele") && loadedGenes.containsKey(mgiAcc) && ! array[columns.get("allele_mgi_accession_id")].isEmpty()) {

                Gene gene = loadedGenes.get(mgiAcc);

                String alleleAcc = array[columns.get("allele_mgi_accession_id")];

                Allele allele = new Allele();
                allele.setAlleleMgiAccessionId(alleleAcc);
                allele.setGene(gene);

                if (!array[columns.get("allele_symbol")].isEmpty()) {
                    allele.setAlleleSymbol(array[columns.get("allele_symbol")]);
                }
                if (!array[columns.get("allele_description")].isEmpty()) {
                    allele.setAlleleDescription(array[columns.get("allele_description")]);
                }
                if (!array[columns.get("mutation_type")].isEmpty()) {
                    allele.setMutationType(array[columns.get("mutation_type")]);
                }
                if (!array[columns.get("es_cell_status")].isEmpty()) {
                    allele.setEsCellStatus(ES_CELL_STATUS_MAPPINGS.get(array[columns.get("es_cell_status")]));
                }
                if (!array[columns.get("mouse_status")].isEmpty()) {
                    allele.setMouseStatus(MOUSE_STATUS_MAPPINGS.get(array[columns.get("mouse_status")]));
                }
                if (!array[columns.get("phenotype_status")].isEmpty()) {
                    allele.setPhenotypeStatus(array[columns.get("phenotype_status")]);
                }

                if (gene.getAlleles() == null){
                    Set<Allele> aset = new HashSet<Allele>();
                    aset.add(allele);
                    gene.setAlleles(aset);
                }
                else {
                    gene.getAlleles().add(allele);
                }

                alleleRepository.save(allele);

                alleleCount++;
                if (alleleCount % 5000 == 0){
                    logger.info("Loaded {} Allele nodes", alleleCount);
                }
            }

            line2 = in2.readLine();
        }

        logger.info("Loaded {} Allele nodes and {} Gene nodes", alleleCount, geneCount);

        String job = "Gene, Allele, MarkerSynonym and EnsemblGeneId nodes";
        loadTime(begin, System.currentTimeMillis(), job);

        // based on MGI report HMD_HumanPhenotype.rpt
        // Mouse/Human Orthology with Phenotype Annotations (tab-delimited)
        loadHumanOrtholog();
    }

//    public void loadEnsemblGeneIdsAndGene(Connection komp2Conn){
//        long begin = System.currentTimeMillis();
//
//        try {
//            String query = "SELECT acc, xref_acc FROM xref WHERE acc LIKE 'MGI:%' AND xref_acc LIKE 'ENSMUSG%'";
//            PreparedStatement p = komp2Conn.prepareStatement(query);
//
//            ResultSet r = p.executeQuery();
//            int count = 0;
//            while (r.next()) {
//
//                count++;
//
//                String ensgId = r.getString("xref_acc");
//                String mgiAcc = r.getString("acc");
//
//                //System.out.println(ensgId + " --- " + mgiAcc);
//
//                // multiple ensembl gene id can be assigned to a gene
//                Gene gene = geneRepository.findByMgiAccessionId(mgiAcc);
//                if (gene == null) {
//                    logger.debug("Gene {} not found. Creating Gene with ", mgiAcc);
//                    gene = new Gene();
//                    gene.setMgiAccessionId(mgiAcc);
//                    geneRepository.save(gene);
//                }
//
//                EnsemblGeneId ensg = new EnsemblGeneId();
//                ensg.setEnsemblGeneId(ensgId);
//                ensg.setGene(gene);
//                ensemblGeneIdRepository.save(ensg);
//
//                if (gene.getEnsemblGeneIds() == null){
//                    Set<EnsemblGeneId> eset = new HashSet<EnsemblGeneId>();
//                    eset.add(ensg);
//                    gene.setEnsemblGeneIds(eset);
//                }
//                else {
//                    gene.getEnsemblGeneIds().add(ensg);
//                }
//
//                if (count%5000 ==0){
//                    logger.info("Loaded " + count + " EnsemblGeneId and Gene nodes");
//                }
//            }
//
//            logger.info("Loaded " + count + " EnsemblGeneId/Gene nodes");
//            String job = "EnsemblGeneId/Gene nodes";
//            loadTime(begin, System.currentTimeMillis(), job);
//
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
//    }

    public void loadTime(long begin, long end, String job){

        long elapsedTimeMillis = end - begin;
        double minutes = (elapsedTimeMillis / (1000.0 * 60)) % 60;
        logger.info("Time taken to load " + job + ": " + minutes + " min");
    }
}