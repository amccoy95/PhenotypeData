package uk.ac.ebi.phenotype.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mousephenotype.cda.solr.service.dto.AnatomyDTO;
import org.mousephenotype.cda.solr.service.dto.DiseaseDTO;
import org.mousephenotype.cda.solr.service.dto.GeneDTO;
import org.mousephenotype.cda.solr.service.dto.MpDTO;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Created by ckchen on 18/07/2017.
 */
public class BatchQueryForm {

    private Map<String, String> datatypeField = new HashMap<>();
    private Map<String, List<String>> qryIdRow = new HashMap<>();
    public JSONObject j = new JSONObject();
    public List<String> rows = new ArrayList<>();
    String NA = "info not available";
    private String hostName;
    private String baseUrl;

    public BatchQueryForm(String mode, HttpServletRequest request, SolrDocumentList results, String fllist, String dataType, List<String> queryIds) throws JSONException {

        Map<String, String> qidMap = new HashMap<>();
        // users query id(s)
        List<String> queryIdsNoQuotes = new ArrayList<>();
        for (String id : queryIds){
            id = id.replaceAll("\"", "");
            String idlc = id.toLowerCase();
            //System.out.println("search qid: " + id + " --> " + idlc);
            queryIdsNoQuotes.add(idlc);
            qidMap.put(idlc, id);
        }

        // field of dataType that shows as hyperlink
        datatypeField.put("gene", GeneDTO.MGI_ACCESSION_ID);
        datatypeField.put("ensembl", GeneDTO.ENSEMBL_GENE_ID);
        datatypeField.put("mouse_marker_symbol", GeneDTO.MARKER_SYMBOL);
        datatypeField.put("human_marker_symbol", GeneDTO.HUMAN_GENE_SYMBOL);
        datatypeField.put("mp", MpDTO.MP_ID);
        datatypeField.put("hp", MpDTO.HP_ID);
        datatypeField.put("disease", DiseaseDTO.DISEASE_ID);
        datatypeField.put("anatomy", AnatomyDTO.ANATOMY_ID);

        System.out.println("datatype: " + dataType);
        hostName = "http:" + request.getAttribute("mappedHostname").toString();
        baseUrl = request.getAttribute("baseUrl").toString();

        List<String> checkedFields = null;
        if (mode.equals("export")){
            String idlink = "id_link";
            fllist = idlink + "," + fllist;
            checkedFields = Arrays.asList(fllist.split(","));
            checkedFields.set(0, checkedFields.get(1));
            checkedFields.set(1, idlink);
        }
        else {
            checkedFields = Arrays.asList(fllist.split(","));
        }

        j.put("aaData", new Object[0]);
        j.put("iTotalRecords", results.size());
        j.put("iTotalDisplayRecords", results.size());

        if (mode.equals("export")) {
            rows.add(join(checkedFields, "\t"));
        }

        System.out.println("Found " + results.size() + " out of " + queryIds.size() + " in user's query list");

        for (int i = 0; i < results.size(); ++i) {
            SolrDocument doc = results.get(i);

            //System.out.println("doc: "+doc.toString());

            List<String> rowData = new ArrayList<String>();
            int count = 0;
            for (String field : checkedFields){
                count++;
                if (doc.containsKey(field)) {

                    if (mode.equals("onPage")) {
                        populateCells(rowData, doc, field, dataType);
                    }
                    else if (mode.equals("export")){
                        populateExportCells(rowData, doc, field, dataType);
                    }
                }
                else if (field.equals("id_link")){
                    continue;
                }
                else {
                    if (doc.containsKey("latest_phenotype_status")
                        && doc.getFieldValue("latest_phenotype_status").equals("Phenotyping Complete")
                        && field.startsWith("mp_")
                        && ! field.equals("mp_term_definition") ){
                        String val = "no abnormal phenotype detected";
                        rowData.add(val);
                    }
                    else {
                        rowData.add(NA);
                    }
                }
            }

            // so that we could output result per user's order of search input
            String keystr = doc.getFieldValue(datatypeField.get(dataType)).toString().toLowerCase().replace("[", "").replace("]", "");

            if (! keystr.contains(",")){
                qryIdRow.put(keystr, rowData);
            }
            else {
                // some mouse symbol are mapped to multiple human gene symbol: we only need to fetch the ones that are in user's query
                //System.out.println("symbol: " +keystr);
                List<String> keys = Arrays.asList(split(keystr, ","));
                for( String key : keys) {
                    key = key.trim();
                    if (queryIdsNoQuotes.contains(key)) {
                        qryIdRow.put(key, rowData);
                    }
                }
            }
        }

        //System.out.println("rows: "+ qryIdRow.toString());

        // ids used in query that have results
        List<String> foundQryIds = new ArrayList<>();
        for (String qid : qryIdRow.keySet()){
            //System.out.println("found qid: "+ qid);
            foundQryIds.add(qid.toLowerCase());
        }

        // find the ids that are not found and displays them to users
        ArrayList nonFoundIds = (java.util.ArrayList) CollectionUtils.disjunction(queryIdsNoQuotes, new ArrayList(foundQryIds));

        //System.out.println("non found: " + nonFoundIds);

        // find index of the field in field list for the dataType
        int fieldIndex = 0;
        if ( nonFoundIds.size() > 0){
            String fname = datatypeField.get(dataType);
            int fc = 0;
            for(String fn : checkedFields){
                if (fname.equals(fn)){
                    fieldIndex = fc;
                    break;
                }
                fc++;
            }
        }

        // fill in NA for the fields for the search query not found
        for (Object nf : nonFoundIds){
            String nfs = nf.toString();
            if (! qryIdRow.containsKey(nf)) {
                List<String> rowData = new ArrayList<String>();
                for (int i = 0; i < checkedFields.size(); i++) {
                    if (i == fieldIndex) {
                        rowData.add(qidMap.get(nfs));
                    } else {
                        rowData.add(NA);
                    }
                }
                qryIdRow.put(nfs.toLowerCase(), rowData);
            }
            else {
                queryIdsNoQuotes.add(nfs);
            }
        }


        // output as the order of input queries
        for (String lcQryId : queryIdsNoQuotes) {
            //System.out.println("checking " + lcQryId);

            if (mode.equals("onPage")) {
                j.getJSONArray("aaData").put(qryIdRow.get(lcQryId));

            }
            else {
                // export result
                rows.add(join(qryIdRow.get(lcQryId), "\t"));
            }
        }
    }

    public void populateCells(List<String> rowData, SolrDocument doc, String field, String dataType) {

        String valtype =  doc.getFieldValue(field).getClass().getTypeName();
        String val = doc.getFieldValue(field).toString();

        if (valtype.equals("java.util.ArrayList")){
            rowData.add(val.replace("[", "").replace("]", ""));
        }
        else {
            if ( (dataType.equals("gene") || dataType.equals("ensembl") || dataType.equals("mouse_marker_symbol") || dataType.equals("human_marker_symbol"))
                    && field.equals("mgi_accession_id") ){
                rowData.add("<a target='_blank' href='" + baseUrl + "/genes/" + val + "'>" + val + "</a>");
            }
            else if ( dataType.equals("mp") && field.equals("mp_id") ){
                rowData.add("<a target='_blank' href='" + baseUrl + "/phenotypes/" + val + "'>" + val + "</a>");
            }
            else if ( dataType.equals("anatomy") && field.equals("anatomy_id") ){
                rowData.add("<a target='_blank' href='" + baseUrl + "/anatomy/" + val + "'>" + val + "</a>");
            }
            else if ( dataType.equals("disease") && field.equals("disease_id") ){
                rowData.add("<a target='_blank' href='" + baseUrl + "/disease/" + val + "'>" + val + "</a>");
            }
            else {
                rowData.add(val); // hp and other fields
            }
        }
    }

    public void populateExportCells(List<String> rowData, SolrDocument doc, String field, String dataType) {

        String valtype =  doc.getFieldValue(field).getClass().getTypeName();
        String val = doc.getFieldValue(field).toString();

        if (valtype.equals("java.util.ArrayList")){
            rowData.add(val.replace("[", "").replace("]", ""));

            if ( dataType.equals("hp") && field.equals("hp_id") ){
                rowData.add(NA);
            }
        }
        else {
            if ( (dataType.equals("gene") || dataType.equals("ensembl") || dataType.equals("mouse_marker_symbol") || dataType.equals("human_marker_symbol"))
                && field.equals("mgi_accession_id") ){
                rowData.add(val);
                String link = hostName + "/" + baseUrl + "/genes/" + val;
                rowData.add(link);
            }
            else if ( dataType.equals("mp") && field.equals("mp_id") ){
                rowData.add(val);
                String link = hostName + "/" + baseUrl + "/phenotypes/" + val;
                rowData.add(link);
            }
            else if ( dataType.equals("anatomy") && field.equals("anatomy_id") ){
                rowData.add(val);
                String link = hostName + "/" + baseUrl + "/anatomy/" + val;
                rowData.add(link);
            }
            else if ( dataType.equals("disease") && field.equals("disease_id") ){
                rowData.add(val);
                String link = hostName + "/" + baseUrl + "/disease/" + val;
                rowData.add(link);
            }
            else {
                rowData.add(val); // other fields
            }
        }
    }

}

