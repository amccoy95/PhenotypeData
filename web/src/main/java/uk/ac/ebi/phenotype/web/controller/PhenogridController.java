package uk.ac.ebi.phenotype.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.sanger.phenodigm2.dao.PhenoDigmWebDao;
import uk.ac.sanger.phenodigm2.model.*;
import uk.ac.sanger.phenodigm2.web.DiseaseGeneAssociationDetail;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
public class PhenogridController {

    private static final Logger logger = LoggerFactory.getLogger(PhenogridController.class);

    @Autowired
    private PhenoDigmWebDao phenoDigmDao;

    @Resource(name = "globalConfiguration")
    private Map<String, String> config;

    @RequestMapping(value="/phenodigm/phenogrid", method= RequestMethod.GET)
    public PhenoGrid getPhenogrid(@RequestParam String requestPageType, @RequestParam String diseaseId, @RequestParam String geneId) {
        logger.info(String.format("Making phenogrid for %s %s from %s page", diseaseId, geneId, requestPageType));

        DiseaseGeneAssociationDetail diseaseGeneAssociationDetail = phenoDigmDao.getDiseaseGeneAssociationDetail(new DiseaseIdentifier(diseaseId), new GeneIdentifier(geneId, geneId));

        List<DiseaseModelAssociation> diseaseAssociations = diseaseGeneAssociationDetail.getDiseaseAssociations();

        Map<Boolean, List<DiseaseModelAssociation>> diseaseModelAssociations = diseaseAssociations.stream()
                .collect(Collectors.partitioningBy(DiseaseModelAssociation::hasLiteratureEvidence));

        List<DiseaseModelAssociation> literatureAssociations = diseaseModelAssociations.get(true);
        List<DiseaseModelAssociation> phenotypicAssociations = diseaseModelAssociations.get(false);

        //The lists need sorting according to the view in which they will be appearing
        //we'll assume that the default is going to be a disease page
        //but it could be a gene page
        Comparator<DiseaseModelAssociation> pageComparator = makePageComparator(requestPageType);

        //sort the lists according to the view in which they will be appearing
        Collections.sort(literatureAssociations, pageComparator);
        Collections.sort(phenotypicAssociations, pageComparator);

        List<DiseaseModelAssociation> sortedAssociations = new ArrayList<>(literatureAssociations);
        sortedAssociations.addAll(phenotypicAssociations);

        String title = String.format("%s - %s disease-model phenotype matches", diseaseId, geneId, requestPageType);
        List<PhenoGridEntity> xAxis = makeMouseModelEntities(sortedAssociations, requestPageType);
        List<PhenoGridEntity> yAxis = Collections.singletonList(makeDiseaseEntity(diseaseGeneAssociationDetail));
        return new PhenoGrid(null, xAxis, yAxis);
    }

    private PhenoGridEntity makeDiseaseEntity(DiseaseGeneAssociationDetail diseaseGeneAssociationDetail) {
        DiseaseIdentifier diseaseId = diseaseGeneAssociationDetail.getDiseaseId();
        Disease disease = phenoDigmDao.getDisease(diseaseId);
        String id = disease.getDiseaseId();
        String label = disease.getTerm();
        List<PhenotypeTerm> phenotypes = diseaseGeneAssociationDetail.getDiseasePhenotypes();
        return new PhenoGridEntity(id, label, phenotypes, null, new ArrayList<>());
    }

    private List<PhenoGridEntity> makeMouseModelEntities(List<DiseaseModelAssociation> diseaseAssociations, String requestPageType) {
        //TODO: need to make two groups of models:  literature associated and Phenodigm predicted - groupId: groupLabel [PhenoGridEntity]
//        diseaseModelAssociation.hasLiteratureEvidence()

        List<PhenoGridEntity> mouseEntities = new ArrayList<>(diseaseAssociations.size());
        for (int i = 0; i < diseaseAssociations.size(); i++) {
            PhenoGridEntity mouseEntity = makeMouseModelEntityForPage(diseaseAssociations.get(i), i, requestPageType);
            mouseEntities.add(mouseEntity);
        }
        return mouseEntities;
    }

    private PhenoGridEntity makeMouseModelEntityForPage(DiseaseModelAssociation diseaseModelAssociation, int rank, String requestPageType) {
        MouseModel mouseModel = diseaseModelAssociation.getMouseModel();
        Integer modelId = mouseModel.getMgiModelId();

        String label = mouseModel.getAllelicComposition();
        //we only want to show the ids for the mouse phenotypes to cut down on payload size
        List<PhenotypeTerm> phenotypes = makeIdOnlyPhenotypes(mouseModel.getPhenotypeTerms());
        double phenodigmScore = makeScoreForPageType(requestPageType, diseaseModelAssociation);
        PhenoGridScore score = new PhenoGridScore("Phenodigm score", phenodigmScore, rank);
        List<EntityInfo> info = makeMouseModelInfo(mouseModel, phenodigmScore);
        return new PhenoGridEntity(modelId.toString(), label, phenotypes, score, info);
    }

    private List<PhenotypeTerm> makeIdOnlyPhenotypes(List<PhenotypeTerm> phenotypeTerms) {
        return phenotypeTerms.stream().map(phenotypeTerm -> {PhenotypeTerm term = new PhenotypeTerm(); term.setId(phenotypeTerm.getId()); return term;}).collect(toList());
    }

    private List<EntityInfo> makeMouseModelInfo(MouseModel mouseModel, double phenodigmScore) {
        EntityInfo source = new EntityInfo("Source: ", mouseModel.getSource(), null);
        //TODO: re-work the MouseModel to have a proper allele representation e.g. list<Allele> where Allele has an mgi id, gene symbol and a lab code.
        EntityInfo genotype = new EntityInfo("Genotype: ", mouseModel.getAllelicCompositionLink(), null);
        EntityInfo background = new EntityInfo("Background: ", mouseModel.getGeneticBackground(), null);
        EntityInfo impcGene = new EntityInfo("Gene: ", mouseModel.getMgiGeneId(), config.get("baseUrl") + "/genes/" +  mouseModel.getMgiGeneId());
        EntityInfo scoreInfo = new EntityInfo("Phenodigm score: ", Double.toString(phenodigmScore), null);
        List<EntityInfo> info = new ArrayList<>(Arrays.asList(source, genotype, background, impcGene, scoreInfo));

        info.add(new EntityInfo("Observed phenotypes: ", "", null));
        List<EntityInfo> phenotypes = mouseModel.getPhenotypeTerms()
                .stream()
                .map(phenotype -> new EntityInfo("", phenotype.getTerm(), config.get("baseUrl") + "/phenotypes/" + phenotype.getId()))
                .collect(toList());
        info.addAll(phenotypes);

        return info;
    }

    private String addSuppTagsToLabCodes(String allelicComposition) {
        //e.g. "Fgfr2<tm1.1Dsn>/Fgfr2<+>"
        String[] alleles = allelicComposition.split("/");
        String alleleString = stripAngleBracketsAndAddSuppTags(alleles[0]) + "/" + stripAngleBracketsAndAddSuppTags(alleles[1]);
        return alleleString;
    }

    private String stripAngleBracketsAndAddSuppTags(String allele) {
        //allele examples "Fgfr2<+>", "Fgfr2<tm1.1Dsn>", "Fgfr2<tm1Lni>"
        String[] tokens = allele.split("\\<");
        String geneSymbol = tokens[0];
        if (tokens.length == 2) {
            String labCode = tokens[1].replace(">", "");
            return geneSymbol + htmlSup(labCode);
        } else {
            return geneSymbol;
        }
    }

    private String htmlSup(String string) {
        return "<sup>" + string + "</sup>";
    }

    private double makeScoreForPageType(String requestPageType, DiseaseModelAssociation diseaseModelAssociation){
        if (requestPageType.equals("disease")) {
            return diseaseModelAssociation.getDiseaseToModelScore();
        }
        return diseaseModelAssociation.getModelToDiseaseScore();
    }

    private Comparator<DiseaseModelAssociation> makePageComparator(String requestPageType) {
        if (requestPageType.equals("gene")) {
            logger.info("Sorting DiseaseAssociations according to m2d score for Gene page");
            return DiseaseModelAssociation.GeneToDiseaseScoreComparator;
        }
        else {
            logger.info("Sorting DiseaseAssociations according to d2m score for Disease page");
            return DiseaseModelAssociation.DiseaseToGeneScoreComparator;
        }
    }

    private class PhenoGrid {

        private final String title;
        private final List<PhenoGridEntity> xAxis;
        private final List<PhenoGridEntity> yAxis;

        public PhenoGrid(String title, List<PhenoGridEntity> xAxis, List<PhenoGridEntity> yAxis) {
            this.title = title;
            this.xAxis = xAxis;
            this.yAxis = yAxis;
        }

        public String getTitle() {
            return title;
        }

        public List<PhenoGridEntity> getxAxis() {
            return xAxis;
        }

        public List<PhenoGridEntity> getyAxis() {
            return yAxis;
        }
    }

    private class PhenoGridEntity {
        private String id; //not sure if this is required or not
        private String label;
        private List<PhenotypeTerm> phenotypes = new ArrayList<>();
        private PhenoGridScore score;
        private List<EntityInfo> info = new ArrayList<>();

        public PhenoGridEntity(String id, String label, List<PhenotypeTerm> phenotypes, PhenoGridScore score, List<EntityInfo> info) {
            this.id = id;
            this.label = label;
            this.phenotypes = phenotypes;
            this.score = score;
            this.info = info;
        }

        public String getId() {
            return id;
        }
        public String getLabel() {
            return label;
        }

        public List<PhenotypeTerm> getPhenotypes() {
            return phenotypes;
        }

        public PhenoGridScore getScore() {
            return score;
        }

        public List<EntityInfo> getInfo() {
            return info;
        }
    }

    private class PhenoGridScore {
        private final String metric;
        private final double score;
        private final int rank;

        public PhenoGridScore(String metric, double score, int rank) {
            this.metric = metric;
            this.score = score;
            this.rank = rank;
        }

        public String getMetric() {
            return metric;
        }

        public double getScore() {
            return score;
        }

        public int getRank() {
            return rank;
        }
    }

    private class EntityInfo {
        private final String id;
        private final String value;
        private final String href;

        public EntityInfo(String id, String value, String href) {
            this.id = id;
            this.value = value;
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public String getHref() {
            return href;
        }
    }

//            "info": {
//        "fields": [
//        {
//            "id": "Genotype",
//                "value": "Fgfr2<sup>tm2.3Dsn</sup>/Fgfr2<sup>+</sup>"
//        },
//        {
//            "id": "Background",
//                "value": "involves: 129 * C57BL/6 * FVB/N"
//        },
//        {
//            "id": "Source",
//                "value": "MGI"
//        },
//        {
//            "id": "MGI allele",
//                "value": "Fgfr2<sup>tm2.3Dsn</sup>",
//                "href": "http://informatics.jax.org/accession/MGI:2153817"
//        },
//        {
//            "id": "IMPC gene",
//                "value": "MGI:95523",
//                "href": "http://www.mousephenotype.org/data/genes/MGI:95523"
//        }
//        ]
//    }
}
