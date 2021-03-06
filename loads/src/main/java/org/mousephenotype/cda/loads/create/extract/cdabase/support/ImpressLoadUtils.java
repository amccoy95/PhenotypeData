/*******************************************************************************
 * Copyright © 2018 EMBL - European Bioinformatics Institute
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 ******************************************************************************/

package org.mousephenotype.cda.loads.create.extract.cdabase.support;

import org.mousephenotype.cda.db.pojo.*;
import org.mousephenotype.impress2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class ImpressLoadUtils {

    // Save exceptions in a set for client retrieval.
    private Set<String> exceptions = new HashSet<>();
    public Set<String> getExceptions() {
        return exceptions;
    }


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${impress.service.url}")
    protected String impressServiceUrl;

    /**
     * Will perform a request to the supplied URL and unmarshall the response into an object.  In the case where
     * the endpoint does not respond correctly, this method will retry the request up to 5 times, backing off the
     * time between retries to allow the service to recover.  Retry delays are in seconds 1.5, 3.0, 6.0, 12.0, 24.0
     *
     * @param url the endpoint to access to get the data
     * @return Object corresponding to the value retrieved from the URL
     */
    @Retryable(
            maxAttempts = 10,
            backoff = @Backoff(delay = 1500, multiplier = 2),
            value = RuntimeException.class)
    private Object getResponseObjectFromImpress(String url) {
        RestTemplate rt = new RestTemplate();
        Object o = rt.getForEntity(url, Object.class);
        return ((ResponseEntity) o).getBody();
    }



    // PIPELINE


    public List<Pipeline> getPipelines(Datasource datasource) {

        List<Pipeline>        pipelines        = new ArrayList<>();
        List<ImpressPipeline> impressPipelines = getImpressPipelines();

        for (ImpressPipeline impressPipeline : impressPipelines) {

            Pipeline pipeline = toPipeline(impressPipeline, datasource);
            pipelines.add(pipeline);

        }

        return pipelines;
    }

    public List<ImpressPipeline> getImpressPipelines() {

        List<ImpressPipeline> pipelines = new ArrayList<>();

        String url = impressServiceUrl + "/pipeline/list/full";
        try {

            Object body = getResponseObjectFromImpress(url);

            HashMap<String, HashMap<String, Object>> pipelinesMap = (HashMap<String, HashMap<String, Object>>) body;

            for (HashMap<String, Object> pipelineMap : pipelinesMap.values()) {

                ImpressPipeline pipeline = new ImpressPipeline();

                pipeline.setPipelineId(convertToLong( pipelineMap.get("pipelineId")));
                pipeline.setPipelineKey((String) pipelineMap.get("pipelineKey"));
                pipeline.setPipelineType((String) pipelineMap.get("pipelineType"));
                pipeline.setName(newlineToSpace((String) pipelineMap.get("name")));
                pipeline.setIsVisible((Boolean) pipelineMap.get("isVisible"));
                pipeline.setIsActive((Boolean) pipelineMap.get("isActive"));
                pipeline.setIsDeprecated((Boolean) pipelineMap.get("isDeprecated"));
                pipeline.setMajorVersion((Integer) pipelineMap.get("majorVersion"));
                pipeline.setMinorVersion((Integer) pipelineMap.get("minorVersion"));
                pipeline.setDescription(newlineToSpace((String) pipelineMap.get("description")));
                pipeline.setIsInternal((Boolean) pipelineMap.get("isInternal"));
                pipeline.setIsDeleted((Boolean) pipelineMap.get("isDeleted"));
                pipeline.setCentreName(newlineToSpace((String) pipelineMap.get("centreName")));
                pipeline.setImpc(((Integer) pipelineMap.get("impc")).shortValue());
                pipeline.setScheduleCollection(((List<Integer>) pipelineMap.get("scheduleCollection")).stream().mapToLong(Long::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));

                pipelines.add(pipeline);
            }

        } catch (Exception e) {

            logger.info("URL: {}", url);
            e.printStackTrace();
            logger.warn(e.getLocalizedMessage());
            return null;
        }

        return pipelines;
    }

    public Pipeline toPipeline(ImpressPipeline impressPipeline, Datasource datasource) {

        Pipeline pipeline = new Pipeline();

        pipeline.setDatasource(datasource);

        pipeline.setStableKey(impressPipeline.getPipelineId());
        pipeline.setStableId(impressPipeline.getPipelineKey());
        pipeline.setMajorVersion(impressPipeline.getMajorVersion());
        pipeline.setMinorVersion(impressPipeline.getMinorVersion());
        pipeline.setDescription(impressPipeline.getDescription());
        pipeline.setName(impressPipeline.getName());
        pipeline.setScheduleCollection(new ArrayList<>(impressPipeline.getScheduleCollection()));

        return pipeline;
    }


    // SCHEDULE


    public Schedule getSchedule(long pipelineId, long scheduleId) {

        ImpressSchedule impressSchedule = getImpressSchedule(pipelineId, scheduleId);

        return toSchedule(impressSchedule);
    }

    public ImpressSchedule getImpressSchedule(long pipelineId, long scheduleId) {

        ImpressSchedule impressSchedule = new ImpressSchedule();
        String          url             = impressServiceUrl + "/schedule/" + scheduleId;

        RestTemplate rt   = new RestTemplate();

        try {
            Object body = getResponseObjectFromImpress(url);

            HashMap<String, Object> impressScheduleMap = (HashMap<String, Object>) body;

            impressSchedule.setScheduleId(convertToLong( impressScheduleMap.get("scheduleId")));
            impressSchedule.setIsActive((Boolean) impressScheduleMap.get("isActive"));
            impressSchedule.setIsDeprecated((Boolean) impressScheduleMap.get("isDeprecated"));
            impressSchedule.setTimeLabel((String) impressScheduleMap.get("timeLabel"));
            impressSchedule.setTime((String) impressScheduleMap.get("time"));
            impressSchedule.setTimeUnit((String) impressScheduleMap.get("timeUnit"));
            impressSchedule.setStage((String) impressScheduleMap.get("stage"));
            impressSchedule.setPipelineId(convertToLong(  impressScheduleMap.get("pipelineId")));
            impressSchedule.setProcedureCollection(((List<Integer>) impressScheduleMap.get("procedureCollection")).stream().mapToLong(Long::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));

        } catch (Exception e) {

            String message = "pipelineId::scheduleId" + pipelineId + "::" + scheduleId + ": URL: " + url;
            exceptions.add(message);
            return null;
        }

        return impressSchedule;
    }

    public Schedule toSchedule(ImpressSchedule impressSchedule) {

        Schedule schedule = new Schedule();

        try {
            schedule.setIsActive(impressSchedule.getIsActive());
            schedule.setIsDeprecated(impressSchedule.getIsDeprecated());
            schedule.setPipelineId(impressSchedule.getPipelineId());
            schedule.setProcedureCollection(impressSchedule.getProcedureCollection());
            schedule.setScheduleId(impressSchedule.getScheduleId());
            schedule.setStage(impressSchedule.getStage());
            schedule.setTime(impressSchedule.getTime());
            schedule.setTimeLabel(impressSchedule.getTimeLabel());
            schedule.setTimeUnit(impressSchedule.getTimeUnit());
            schedule.setProcedureCollection(impressSchedule.getProcedureCollection());

        } catch (Exception e) {

            e.printStackTrace();
            logger.warn("impressSchedule: {}", impressSchedule);
            return null;
        }

        return schedule;
    }


    // PROCEDURE


    public Procedure getProcedure(long pipelineId, long scheduleId, long procedureId, Datasource datasource) {

        ImpressProcedure impressProcedure = getImpressProcedure(pipelineId, scheduleId, procedureId);

        return toProcedure(impressProcedure, datasource);
    }

    public ImpressProcedure getImpressProcedure(long pipelineId, long scheduleId, long procedureId) {

        ImpressProcedure impressProcedure = new ImpressProcedure();
        String           url              = impressServiceUrl + "/procedure/" + procedureId;

        try {

            Object body = getResponseObjectFromImpress(url);
            Object o;

            HashMap<String, Object> procedureMap = (HashMap<String, Object>) body;

            impressProcedure.setProcedureId(convertToLong(  procedureMap.get("procedureId")));
            impressProcedure.setProcedureKey((String) procedureMap.get("procedureKey"));

            o = procedureMap.get("minFemales");
            impressProcedure.setMinFemales(o == null ? null : ((Integer) o).shortValue());

            o = procedureMap.get("minMales");
            impressProcedure.setMinMales(o == null ? null : ((Integer) o).shortValue());

            o = procedureMap.get("minAnimals");
            impressProcedure.setMinAnimals(o == null ? null : ((Integer) o).shortValue());

            impressProcedure.setIsVisible((Boolean) procedureMap.get("isVisible"));
            impressProcedure.setIsMandatory((Boolean) procedureMap.get("isMandatory"));
            impressProcedure.setIsInternal((Boolean) procedureMap.get("isInternal"));
            impressProcedure.setName(newlineToSpace((String) procedureMap.get("name")));
            impressProcedure.setType((Integer) procedureMap.get("type"));
            impressProcedure.setLevel((String) procedureMap.get("level"));
            impressProcedure.setMajorVersion((Integer) procedureMap.get("majorVersion"));
            impressProcedure.setMinorVersion((Integer) procedureMap.get("minorVersion"));
            impressProcedure.setDescription(newlineToSpace((String) procedureMap.get("description")));
            impressProcedure.setOldProcedureKey((String) procedureMap.get("oldProcedureKey"));
            impressProcedure.setParameterCollection(((List<Integer>) procedureMap.get("parameterCollection")).stream().mapToLong(Long::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
            impressProcedure.setScheduleId(convertToLong(  procedureMap.get("scheduleId")));

        } catch (Exception e) {

            e.printStackTrace();
            String message = "pipelineId::scheduleId::procedureId::" + pipelineId + "::" + scheduleId + "::" + procedureId + ": URL: " + url;
            exceptions.add(message);
            return null;
        }

        return impressProcedure;
    }

    public Procedure toProcedure(ImpressProcedure impressProcedure, Datasource datasource) {

        if (impressProcedure == null) {
            return null;
        }

        Procedure procedure = new Procedure();

        procedure.setDatasource(datasource);

        procedure.setStableKey(impressProcedure.getProcedureId());
        procedure.setStableId(impressProcedure.getProcedureKey());
        procedure.setMajorVersion(impressProcedure.getMajorVersion());
        procedure.setMinorVersion(impressProcedure.getMinorVersion());
        procedure.setDescription(impressProcedure.getDescription());
        procedure.setName(impressProcedure.getName());
        procedure.setLevel(impressProcedure.getLevel());
        procedure.setParameterCollection(new HashSet<>(impressProcedure.getParameterCollection()));
        procedure.setScheduleKey(impressProcedure.getScheduleId());

        return procedure;
    }


    // PARAMETER


    public Parameter getParameter(long pipelineId, long scheduleId, long procedureId, long parameterId, Datasource datasource, Map<Long, String> unitsById) {

        ImpressParameter impressParameter = getImpressParameter(pipelineId, scheduleId, procedureId, parameterId);

        return toParameter(impressParameter, datasource, unitsById);
    }

    public ImpressParameter getImpressParameter(long pipelineId, long scheduleId, long procedureId, long parameterId) {

        ImpressParameter impressParameter = new ImpressParameter();
        String           url              = impressServiceUrl + "/parameter/" + parameterId;

        try {
            Object body = getResponseObjectFromImpress(url);

            HashMap<String, Object> parameterMap = (HashMap<String, Object>) body;

            impressParameter.setParameterId(convertToLong(  parameterMap.get("parameterId")));
            impressParameter.setParameterKey((String) parameterMap.get("parameterKey"));
            impressParameter.setType((String) parameterMap.get("type"));
            impressParameter.setName(newlineToSpace((String) parameterMap.get("name")));
            impressParameter.setIsVisible((Boolean) parameterMap.get("isVisible"));
            impressParameter.setIsActive((Boolean) parameterMap.get("isActive"));
            impressParameter.setIsDeprecated((Boolean) parameterMap.get("isDeprecated"));
            impressParameter.setMajorVersion((Integer) parameterMap.get("majorVersion"));
            impressParameter.setMinorVersion((Integer) parameterMap.get("minorVersion"));
            impressParameter.setDerivation((String) parameterMap.get("derivation"));
            impressParameter.setDescription(newlineToSpace((String) parameterMap.get("description")));
            impressParameter.setIsAnnotation((Boolean) parameterMap.get("isAnnotation"));
            impressParameter.setIsDerived((Boolean) parameterMap.get("isDerived"));
            impressParameter.setIsImportant((Boolean) parameterMap.get("isImportant"));
            impressParameter.setIsIncrement((Boolean) parameterMap.get("isIncrement"));
            impressParameter.setIsMedia((Boolean) parameterMap.get("isMedia"));
            impressParameter.setIsOption((Boolean) parameterMap.get("isOption"));
            impressParameter.setIsRequired((Boolean) parameterMap.get("isRequired"));
            impressParameter.setQcCheck((Boolean) parameterMap.get("qcCheck"));

            Double d = (Double) parameterMap.get("qcMin");
            impressParameter.setQcMin(d == null ? null : d.floatValue());

            d = (Double) parameterMap.get("qcMax");
            impressParameter.setQcMax(d == null ? null : d.floatValue());

            impressParameter.setQcNotes(newlineToSpace((String) parameterMap.get("qcNotes")));
            impressParameter.setValueType((String) parameterMap.get("valueType"));
            impressParameter.setGraphType((String) parameterMap.get("graphType"));
            impressParameter.setDataAnalysisNotes(newlineToSpace((String) parameterMap.get("dataAnalysisNotes")));
            impressParameter.setIsInternal((Boolean) parameterMap.get("isInternal"));
            impressParameter.setIsDeleted((Boolean) parameterMap.get("isDeleted"));
            impressParameter.setOldParameterKey((String) parameterMap.get("oldParameterKey"));
            impressParameter.setOriginalParamId(convertToLong(parameterMap.get("originalParamId")));
            impressParameter.setOntologyGroupId(convertToLong(parameterMap.get("ontologyGroupId")));
            impressParameter.setWeight((Integer) parameterMap.get("weight"));
            impressParameter.setProcedureId(convertToLong(parameterMap.get("procedureId")));

            Long      unitId       = convertToLong(parameterMap.get("unit"));
            ImpressUnits impressUnits = null;
            if (unitId != null) {
                impressUnits = new ImpressUnits();
                impressUnits.setId(unitId);
            }
            impressParameter.setUnit(impressUnits);

            impressParameter.setIncrementCollection((List<Integer>) parameterMap.get("incrementCollection"));
            impressParameter.setOptionCollection((List<Integer>) parameterMap.get("optionCollection"));
            impressParameter.setMptermCollection((List<Integer>) parameterMap.get("mptermCollection"));

        } catch (Exception e) {

            String message = "pipelineId::scheduleId::procedureId::parameterId" + pipelineId + "::" + scheduleId + "::" + procedureId + "::" + parameterId + ": URL: " + url;
            exceptions.add(message);
            return null;
        }

        return impressParameter;
    }

    public Parameter toParameter(ImpressParameter impressParameter, Datasource datasource, Map<Long, String> unitsById) {

        if (impressParameter == null) {
            return null;
        }

        Parameter parameter = new Parameter();

        parameter.setDatasource(datasource);

        parameter.setStableKey(impressParameter.getParameterId());
        parameter.setStableId(impressParameter.getParameterKey());
        parameter.setType(impressParameter.getType());
        parameter.setName(impressParameter.getName());
        parameter.setMajorVersion(impressParameter.getMajorVersion());
        parameter.setMinorVersion(impressParameter.getMinorVersion());
        parameter.setDescription(impressParameter.getDescription());
        parameter.setAnnotateFlag(impressParameter.getIsAnnotation());
        parameter.setDerivedFlag(impressParameter.getIsDerived());
        parameter.setImportantFlag(impressParameter.getIsImportant());
        parameter.setIncrementFlag(impressParameter.getIsIncrement());
        parameter.setMediaFlag(impressParameter.getIsMedia());
        parameter.setOptionsFlag(impressParameter.getIsOption());
        parameter.setMetaDataFlag(impressParameter.getType().equals("procedureMetadata"));
        parameter.setRequiredFlag(impressParameter.getIsRequired());
        parameter.setDatatype(impressParameter.getValueType());
        parameter.setDataAnalysisNotes(impressParameter.getDataAnalysisNotes());
        if (parameter.getDerivedFlag()) {
            parameter.setFormula(impressParameter.getDerivation());
        }

        ImpressUnits unit = impressParameter.getUnit();
        parameter.setUnit(unit == null ? null : unitsById.get(unit.getId()));

        parameter.setOntologyGroupId(impressParameter.getOntologyGroupId());

        return parameter;
    }


    // INCREMENT


    public List<ParameterIncrement> getIncrements(long pipelineId, long scheduleId, long procedureId, long parameterId) {

        List<ParameterIncrement> increments = new ArrayList<>();

        List<ImpressIncrement> impressIncrements = getImpressIncrements(pipelineId, scheduleId, procedureId, parameterId);

        for (ImpressIncrement impressIncrement : impressIncrements) {
            ParameterIncrement increment = toIncrement(impressIncrement);
            increments.add(increment);
        }

        return increments;
    }

    public List<ImpressIncrement> getImpressIncrements(long pipelineId, long scheduleId, long procedureId, long parameterId) {

        List<ImpressIncrement> increments = new ArrayList<>();

        String url = impressServiceUrl + "/increment/belongingtoparameter/full/" + parameterId;
        try {

            Object body = getResponseObjectFromImpress(url);
            Object o;

            List<Map<String, Object>> list = (List<Map<String, Object>>) body;

            for (Map<String, Object> map : list) {

                ImpressIncrement increment = new ImpressIncrement();

                increment.setIncrementId((Integer) map.get("incrementId"));

                try {
                    increment.setWeight((Integer) map.get("weight"));
                } catch (NullPointerException e) {
                    logger.warn("weight for parameterId {} is NULL! url = {}", parameterId, url);
                }

                increment.setIsActive((Boolean) map.get("isActive"));

                o = map.get("incrementString");
                increment.setIncrementString(o == null ? "" : (String) o);

                o = map.get("incrementType");
                increment.setIncrementType(o == null ? "" : (String) o);

                o = map.get("incrementUnit");
                increment.setIncrementUnit(o == null ? "" : (String) o);

                o = map.get("incrementMin");
                increment.setIncrementMin(o == null ? null : (Integer) o);

                increment.setIsDeleted((Boolean) map.get("isDeleted"));

                o = map.get("originalId");
                increment.setOriginalId(o == null ? null : (Integer) o);

                increment.setParameterId((Integer) map.get("parameterId"));

                increments.add(increment);
            }

        } catch (Exception e) {

            String message = pipelineId + "::" + scheduleId + "::" + procedureId + "::" + parameterId + ": URL: " + url;
            exceptions.add(message);
            return null;
        }

        return increments;
    }

    public ParameterIncrement toIncrement(ImpressIncrement impressIncrement) {

        if (impressIncrement == null) {
            return null;
        }

        ParameterIncrement increment = new ParameterIncrement();


        Integer i = impressIncrement.getIncrementMin();
        increment.setMinimum(i == null ? "" : i.toString());

        increment.setUnit(impressIncrement.getIncrementUnit() == null ? "" : impressIncrement.getIncrementUnit());

        increment.setDataType(impressIncrement.getIncrementType() == null ? "" : impressIncrement.getIncrementType());

        increment.setValue(impressIncrement.getIncrementString());

        return increment;
    }


    // OPTION


    public List<ParameterOption> getOptions(long pipelineId, long scheduleId, long procedureId, Parameter parameter, Set<String> normalCategory) {

        List<ParameterOption> options = new ArrayList<>();

        List<ImpressOption> impressOptions = getImpressOptions(pipelineId, scheduleId, procedureId, parameter.getStableKey());

        for (ImpressOption impressOption : impressOptions) {
            ParameterOption option = toOption(impressOption, parameter.getStableId(), normalCategory);
            options.add(option);
        }

        return options;
    }

    public List<ImpressOption> getImpressOptions(long pipelineId, long scheduleId, long procedureId, long parameterId) {

        List<ImpressOption> options = new ArrayList<>();

        String url = impressServiceUrl + "/option/belongingtoparameter/full/" + parameterId;

        try {

            Object body = getResponseObjectFromImpress(url);
            Object o;

            List<HashMap<String, Object>> impressOptionsList = (List<HashMap<String, Object>>) body;

            for (HashMap<String, Object> map : impressOptionsList) {

                ImpressOption option = new ImpressOption();

                option.setOptionId(convertToLong(  map.get("optionId")));
                option.setPhoWeight((Integer) map.get("phoweight"));

                o = map.get("parentId");
                option.setParentId(o == null ? null : (Integer) o);

                o = map.get("name");
                option.setName(o == null ? "" : newlineToSpace((String) o));

                option.setIsDefault((Boolean) map.get("isDefault"));
                option.setIsActive((Boolean) map.get("isActive"));
                option.setPoWeight((Integer) map.get("poweight"));

                o = map.get("description");
                option.setDescription(o == null ? "" : (String) o);

                option.setIsDeleted((Boolean) map.get("isDeleted"));

                option.setParameterId(convertToLong(  map.get("parameterId")));

                options.add(option);
            }

        } catch (Exception e) {

            String message = pipelineId + "::" + scheduleId + "::" + procedureId + "::" + parameterId + ": URL: " + url;
            exceptions.add(message);
            return null;
        }

        return options;
    }

    public ParameterOption toOption(ImpressOption impressOption, String parameterStableId, Set<String> normalCategory) {

        if (impressOption == null) {
            return null;
        }

        ParameterOption option = new ParameterOption();

        option.setName(impressOption.getName());
        option.setDescription(impressOption.getDescription());

        // This is the same format as the populate method, parameterStableId_NormalOption
        String candidate = parameterStableId + "_" + option.getName();
        option.setNormalCategory(normalCategory.contains(candidate));

        return option;
    }


    // UNITS


    public Map<Long, String> getUnits() {

        Map<Long, String> units = new HashMap<>();

        String url = impressServiceUrl + "/unit/list";

        Object body = getResponseObjectFromImpress(url);

        HashMap<String, String> unitsMap = (HashMap<String, String>) body;
        for (Map.Entry<String, String> entry : unitsMap.entrySet()) {
            units.put(Long.parseLong(entry.getKey()), entry.getValue());
        }

        return units;
    }


    // ANNOTATIONS


    public Map<String, String> getOntologyTermsFromWs(long pipelineId, long scheduleId, long procedureId, Parameter parameter) {

        Map<String, String> ontologyTerms = new HashMap<>();

        // A null ontologyGroupId indicates there are no ontology terms.
        if (parameter.getOntologyGroupId() == null) {
            return ontologyTerms;
        }

        String url = impressServiceUrl + "/ontologyoption/optionsingroup/" + parameter.getOntologyGroupId();

        try {

            Object body = getResponseObjectFromImpress(url);

            List<Map<String, Object>> list = (List<Map<String, Object>>) body;
            for (Map<String, Object> ontologyOptionMap : list) {
                Map<String, Object> ontologyTermMap = (Map<String, Object>) ontologyOptionMap.get("ontologyTermId");

                String acc = (String) ontologyTermMap.get("ontologyTerm");
                String term = (String) ontologyTermMap.get("ontologyTermName");
                ontologyTerms.put(acc, term);
            }

        } catch (Exception e) {

            String message = pipelineId + "::" + scheduleId + "::" + procedureId + "::" + parameter.getStableKey() + "::" +
                    parameter.getStableId() + ": URL: " + url;
            exceptions.add(message);
            return ontologyTerms;
        }

        return ontologyTerms;
    }


    public Map<String, String> getMpOntologyTermsFromWs(long pipelineId, long scheduleId, long procedureId, Parameter parameter) {

        Map<String, String> ontologyTerms = new HashMap<>();

        String url = impressServiceUrl + "/ontologyterm/belongingtoparameter/" + parameter.getStableKey();

        try {

            Object body = getResponseObjectFromImpress(url);

            Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) body;
            for (Map<String, Object> ontologyTermMap : map.values()) {
                String acc  = (String) ontologyTermMap.get("ontologyTerm");
                String term = (String) ontologyTermMap.get("ontologyTermName");
                ontologyTerms.put(acc, term);
            }

        } catch (HttpClientErrorException e) {

            // If there are no ontology terms, an HttpClientErrorException will be thrown with HttpStatus 404.
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ontologyTerms;
            }
        } catch (Exception e) {

            String message = pipelineId + "::" + scheduleId + "::" + procedureId + "::" + parameter.getStableKey() + "::" +
                    parameter.getStableId() + ": URL: " + url;
            exceptions.add(message);
            return ontologyTerms;
        }

        return ontologyTerms;
    }

    public Map<String, Long> getOntologyTermStableKeysByAccFromWs() {

        Map<String, Long> map = new HashMap<>();

        String url = impressServiceUrl + "/ontologyterm/list";

        try {

            Object body = getResponseObjectFromImpress(url);
            Map<String, String> wsOntologyTermsMap = (Map<String, String>) body;
            for (Map.Entry<String, String> entry : wsOntologyTermsMap.entrySet()) {
                Long ontologyTermId = Long.parseLong(entry.getKey());
                String  accessionId    = entry.getValue();
                map.put(accessionId, ontologyTermId);
            }
        } catch (Exception e) {

            e.printStackTrace();
            logger.warn("URL: {}. Error: {}", url, e.getLocalizedMessage());
            return null;
        }

        return map;
    }

    public Map<String, List<ImpressParamMpterm>> getParamMpTermsByOntologyTermAccessionId(long pipelineId, long scheduleId, long procedureId, Parameter parameter, Map<Long, OntologyTerm> updatedOntologyTermsByStableKey) {

        Map<String, List<ImpressParamMpterm>> terms = new HashMap<>();

        String url = impressServiceUrl + "/ontologyterm/linksbelongingtoparameter/" + parameter.getStableKey();

        try {

            terms = getTerms(url, updatedOntologyTermsByStableKey);

        } catch (Exception e) {
            String message = pipelineId + "::" + scheduleId + "::" + procedureId + "::" + parameter.getStableKey() + "::" +
                    parameter.getStableId() + ": URL: " + url;
            exceptions.add(message);
        }

        return terms;
    }

    @Retryable(
            maxAttempts = 4,
            backoff = @Backoff(delay = 2000, multiplier = 2),
            value = RuntimeException.class)
    private Map<String, List<ImpressParamMpterm>> getTerms(String url, Map<Long, OntologyTerm> updatedOntologyTermsByStableKey) {
        Map<String, List<ImpressParamMpterm>> terms = new HashMap<>();
        Object body = getResponseObjectFromImpress(url);
        List<Map<String, Object>> maps = (List<Map<String, Object>>) body;

        for (Map<String, Object> map : maps) {
            ImpressParamMpterm paramMpTerm = getTerm(map);
            String ontologyTermAccessionId = updatedOntologyTermsByStableKey.get(paramMpTerm.getOntologyTermId()).getId().getAccession();

            if ( ! terms.containsKey(ontologyTermAccessionId)) {
                terms.put(ontologyTermAccessionId, new ArrayList<>());
            }

            terms.get(ontologyTermAccessionId).add(paramMpTerm);
        }
        return terms;
    }


    // GENERAL PUBLIC METHODS


    /**
     * If token is not null, replaces all newline characters with a single space. Some impress names have a newline
     * in the middle of a two-word name, which causes processing problems downstream. Some of this data is old and,
     * despite requests to the DCC to scrub it, it is still bad.
     * @param token String to be scrubbed. If null, null is returned. If not null, all newlines are replaced with a
     *              single space
     * @return the scrubbed token, if not null; otherwise, null
     */
    public static String newlineToSpace(String token) {
        String result = token;

        if (result != null) {
            result = result.replaceAll("\\n", " ");
        }

        return result;
    }

    public Long convertToLong(Object valueToConvert) {
        Long retVal = null;
        if (valueToConvert != null) {
            try {
                Integer integerValue = (Integer) valueToConvert;
                retVal = new Long(integerValue);
            } catch (Exception e) {
                logger.debug("Bad value for conversion: {}", valueToConvert, e);
            }
        }
        return retVal;
    }

    public String getImpressServiceUrl() {
        return impressServiceUrl;
    }


    // PRIVATE METHODS


    private ImpressParamMpterm getTerm(Map<String, Object> map) {

        ImpressParamMpterm term = new ImpressParamMpterm();
        term.setParamMptermId(convertToLong(  map.get("paramMptermId")));
        term.setOntologyTermId(convertToLong(  map.get("ontologyTermId")));
        term.setWeight((Integer) map.get("weight"));
        term.setIsDeleted((Boolean) map.get("isDeleted"));
        term.setOptionText((String) map.get("optionText"));
        term.setIncrementId((Integer) map.get("incrementId"));
        term.setParameterId(convertToLong(  map.get("parameterId")));
        term.setSex((String) map.get("sex"));
        term.setSelectionOutcome((String) map.get("selectionOutcome"));

        return term;
    }
}