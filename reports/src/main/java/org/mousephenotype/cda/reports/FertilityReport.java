/*******************************************************************************
 * Copyright © 2015 EMBL - European Bioinformatics Institute
 * <p>
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this targetFile except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 ******************************************************************************/

package org.mousephenotype.cda.reports;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.mousephenotype.cda.reports.support.ReportException;
import org.mousephenotype.cda.solr.service.ObservationService;
import org.mousephenotype.cda.solr.service.dto.ObservationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.io.IOException;
import java.util.*;

/**
 * Fertility report.
 *
 * Created by mrelac on 24/07/2015.
 */
@Component
public class FertilityReport extends AbstractReport {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ObservationService observationService;

    public static final String MALE_FERTILITY_PARAMETER = "IMPC_FER_001_001";
    public static final String FEMALE_FERTILITY_PARAMETER = "IMPC_FER_019_001";

    public FertilityReport() {
        super();
    }

    @Override
    public String getDefaultFilename() {
        return Introspector.decapitalize(ClassUtils.getShortClassName(this.getClass()));
    }

    public void run(String[] args) throws ReportException {

        List<String> errors = parser.validate(parser.parse(args));
        if (!errors.isEmpty()) {
            logger.error("FertilityReport parser validation error: " + StringUtils.join(errors, "\n"));
            return;
        }
        initialise(args);

        long start = System.currentTimeMillis();

        try {
            QueryResponse response = observationService.getData(resources, Arrays.asList( new String[] { FEMALE_FERTILITY_PARAMETER, MALE_FERTILITY_PARAMETER } ), null);
            List<Map<String, String>> fertilityResourceList = observationService.getCategories(resources, Arrays.asList(new String[] { FEMALE_FERTILITY_PARAMETER, MALE_FERTILITY_PARAMETER }), "sex,category,gene_symbol");

            // Gather collections for summary.
            Set<String> bothFertile;
            Set<String> bothInfertile;
            Set<String> conflictingFemales;
            Set<String> conflictingMales;
            Set<String> femalesFertile = new TreeSet<>();
            Set<String> femalesInfertile = new TreeSet<>();
            Set<String> malesFertile = new TreeSet<>();
            Set<String> malesInfertile = new TreeSet<>();
            for (Map<String, String> fertilityResourceMap : fertilityResourceList) {
                String geneSymbol = "";
                String sex = "";
                String category = "";
                for (Map.Entry<String, String> entry : fertilityResourceMap.entrySet()) {
                    switch (entry.getKey()) {
                        case "gene_symbol": geneSymbol = entry.getValue();                break;
                        case "sex":         sex = entry.getValue().toLowerCase();         break;
                        case "category":    category = entry.getValue().toLowerCase();    break;
                        default: throw new ReportException("Unexpected facet '" + entry.getKey() + "'");
                    }
                }

                if (sex.equals("female") ) {
                    if (category.equals("fertile")) {
                        femalesFertile.add(geneSymbol);
                    } else if (category.equals("infertile")){
                        femalesInfertile.add(geneSymbol);
                    } else {
                        throw new ReportException("Invalid category' " + category + "'");
                    }
                } else if (sex.equals("male")) {
                    if (category.equals("fertile")) {
                        malesFertile.add(geneSymbol);
                    } else if (category.equals("infertile")){
                        malesInfertile.add(geneSymbol);
                    } else {
                        throw new ReportException("Invalid category' " + category + "'");
                    }
                } else {
                    throw new ReportException("Invalid sex' " + sex + "'");
                }
            }
            bothFertile = new TreeSet<>(femalesFertile);
            bothFertile.retainAll(malesFertile);
            bothInfertile = new TreeSet<>(femalesInfertile);
            bothInfertile.retainAll(malesInfertile);
            conflictingFemales = new TreeSet<>(femalesFertile);
            conflictingFemales.retainAll(femalesInfertile);
            conflictingMales = new TreeSet<>(malesFertile);
            conflictingMales.retainAll(malesInfertile);

            // Write summary section.
            csvWriter.writeRow(Arrays.asList(new String[] { "Phenotype", "# Genes", "Gene Symbols"  }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Both fertile", Integer.toString(bothFertile.size()), StringUtils.join(bothFertile, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Both infertile", Integer.toString(bothInfertile.size()), StringUtils.join(bothInfertile, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Females fertile", Integer.toString(femalesFertile.size()), StringUtils.join(femalesFertile, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Females infertile", Integer.toString(femalesInfertile.size()), StringUtils.join(femalesInfertile, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Males fertile", Integer.toString(malesFertile.size()), StringUtils.join(malesFertile, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Males infertile", Integer.toString(malesInfertile.size()), StringUtils.join(malesInfertile, ", ") }));

            csvWriter.writeNext(EMPTY_ROW);

             // Write conflicting section.
            csvWriter.writeRow(Arrays.asList(new String[] { "Conflicting females", Integer.toString(conflictingFemales.size()), StringUtils.join(conflictingFemales, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "Conflicting males", Integer.toString(conflictingMales.size()), StringUtils.join(conflictingMales, ", ") }));
            csvWriter.writeRow(Arrays.asList(new String[] { "NOTE: Symbols in the conflicting list represent genes that are included in more than one fertility category." } ));

            csvWriter.writeNext(EMPTY_ROW);

            // Write detail section.
            csvWriter.writeRow(Arrays.asList(new String[] { "Gene Symbol", "MGI Gene Id", "Colony Id", "Sex", "Zygosity", "Phenotype", "Comment" } ));//            allTable.add(header);
            for ( SolrDocument doc : response.getResults()) {
                String category = doc.getFieldValue(ObservationDTO.CATEGORY).toString();
                String geneSymbol = doc.getFieldValue(ObservationDTO.GENE_SYMBOL).toString();
                List<String> row = new ArrayList<>();
                row.add(doc.getFieldValue(ObservationDTO.GENE_SYMBOL) != null ? doc.getFieldValue(ObservationDTO.GENE_SYMBOL).toString() : "");
                row.add(doc.getFieldValue(ObservationDTO.GENE_ACCESSION_ID) != null ? doc.getFieldValue(ObservationDTO.GENE_ACCESSION_ID).toString() : "");
                row.add(doc.getFieldValue(ObservationDTO.COLONY_ID).toString());
                row.add(doc.getFieldValue(ObservationDTO.SEX).toString());
                row.add(doc.getFieldValue(ObservationDTO.ZYGOSITY).toString());
                row.add(category);
                if ((conflictingFemales.contains(geneSymbol)) || (conflictingMales.contains(geneSymbol))) {
                    row.add("Conflicting Data");
                }

                csvWriter.writeRow(row);
            }

        } catch (SolrServerException e) {
            throw new ReportException("Exception creating " + this.getClass().getCanonicalName() + ". Reason: " + e.getLocalizedMessage());
        }

        try {
            csvWriter.close();
        } catch (IOException e) {
            throw new ReportException("Exception closing csvWriter: " + e.getLocalizedMessage());
        }

        log.info(String.format("Finished. [%s]", commonUtils.msToHms(System.currentTimeMillis() - start)));
    }
}