/*******************************************************************************
 * Copyright © 2015 EMBL - European Bioinformatics Institute
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

package org.mousephenotype.cda.loads.sanitycheck;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.mousephenotype.dcc.exportlibrary.datastructure.core.common.StatusCode;
import org.mousephenotype.dcc.exportlibrary.datastructure.core.procedure.*;
import org.mousephenotype.dcc.exportlibrary.datastructure.core.specimen.*;
import org.mousephenotype.dcc.exportlibrary.xmlserialization.exceptions.XMLloadingException;
import org.mousephenotype.dcc.utils.xml.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.List;

/**
 * Created by mrelac on 09/02/2016.
 * <p/>
 * This class encapsulates the code and data necessary to load the specified target database with the source dcc
 * experiment files currently found at /nfs/komp2/web/phenotype_data/impc. This class is meant to be an executable jar
  * whose arguments describe the source file, target database name and authentication, and spring context file.
 */
public class ExperimentLoader {
    /**
     * Load specimen data that was encoded using the IMPC XML format
     */

    // Required by the Harwell DCC export utilities
    public static final String CONTEXT_PATH = "org.mousephenotype.dcc.exportlibrary.datastructure.core.common:org.mousephenotype.dcc.exportlibrary.datastructure.core.procedure:org.mousephenotype.dcc.exportlibrary.datastructure.core.specimen:org.mousephenotype.dcc.exportlibrary.datastructure.tracker.submission:org.mousephenotype.dcc.exportlibrary.datastructure.tracker.validation";
    private static final Logger logger = LoggerFactory.getLogger(ExperimentLoader.class);

    private String filename;
    private String username = "";
    private String password = "";
    private String dbName;
    private boolean truncate = false;

    private Connection connection;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, XMLloadingException, KeyManagementException, SQLException, JAXBException, DccLoaderException {

        ExperimentLoader main = new ExperimentLoader();
        main.initialize(args);
        main.run();

        logger.debug("Process finished.  Exiting.");
    }

    private void initialize(String[] args)
            throws IOException, SQLException, KeyManagementException, NoSuchAlgorithmException {

        OptionParser parser = new OptionParser();

        // parameter to indicate the database name
        parser.accepts("dbname").withRequiredArg().ofType(String.class);

        // parameter to indicate the name of the file to process
        parser.accepts("filename").withRequiredArg().ofType(String.class);

        // parameter to indicate the database username
        parser.accepts("username").withRequiredArg().ofType(String.class);

        // parameter to indicate the database password
        parser.accepts("password").withRequiredArg().ofType(String.class);

        // parameter to indicate the database password
        parser.accepts("truncate").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        dbName = (String) options.valuesOf("dbname").get(0);
        String dbUrl = "jdbc:mysql://mysql-mi-dev:4356/"
                + dbName
                + "?autoReconnect=true&amp;useUnicode=true&amp;connectionCollation=utf8_general_ci&amp;characterEncoding=utf8&amp;characterSetResults=utf8&amp;zeroDateTimeBehavior=convertToNull";

        username = (String) options.valuesOf("username").get(0);
        password = (String) options.valuesOf("password").get(0);
        connection = DriverManager.getConnection(dbUrl, username, password);

        filename = (String) options.valuesOf("filename").get(0);
        truncate = false;
        if ( ! options.valuesOf("truncate").isEmpty()) {
            truncate = (options.valuesOf("truncate").get(0).toString().toLowerCase().equals("true") ? true : false);
        }
        logger.info("Loading experiment file {}", filename);
    }

    private void run() throws JAXBException, XMLloadingException, IOException, SQLException, KeyManagementException, NoSuchAlgorithmException, DccLoaderException {
        List<CentreProcedure> centerProcedures = XMLUtils.unmarshal(ExperimentLoader.CONTEXT_PATH, CentreProcedureSet.class, filename).getCentre();

        if (centerProcedures.size() == 0) {
            logger.error("{} failed to unmarshall", filename);
            throw new XMLloadingException(filename + " failed to unmarshall.");
        }

        logger.debug("There are {} center procedure sets in experiment file {}", centerProcedures.size(), filename);

        long centerPk, centerProcedurePk, experimentPk, procedurePk, specimenPk, statuscodePk;
        PreparedStatement ps;
        ResultSet rs;
        String query;
        for (CentreProcedure centerProcedure : centerProcedures) {
            logger.debug("Parsing experiments for center {}", centerProcedure.getCentreID());

            if (truncate) {
                LoaderUtils.truncateExperimentTables(connection);
            }

            connection.setAutoCommit(false);    // BEGIN TRANSACTION

            // Get centerPk
            centerPk = LoaderUtils.getCenterPk(connection, centerProcedure.getCentreID().value(), centerProcedure.getPipeline(), centerProcedure.getProject());
            if (centerPk < 1) {
                System.out.println("UNKNOWN CENTER,PIPELINE,PROJECT: '" + centerProcedure.getCentreID().value() + ","
                        + centerProcedure.getPipeline() + "," + centerProcedure.getProject() + "'. INSERTING...");
                centerPk = LoaderUtils.insertIntoCenter(connection, centerProcedure.getCentreID().value(), centerProcedure.getPipeline(), centerProcedure.getProject());
            }

            for (Experiment experiment : centerProcedure.getExperiment()) {
                for (String specimenId : experiment.getSpecimenID()) {
                    // get specimenPk
                    Specimen specimen = LoaderUtils.getSpecimen(connection, specimenId, centerProcedure.getCentreID().value());
                    if (specimen == null) {
                        System.out.println("UNKNOWN SPECIMEN,CENTER: '" + specimenId + "," + centerProcedure.getCentreID().value() + "'. INSERTING...");
                        connection.rollback();
                        continue;
                    }
                    specimenPk = specimen.getHjid();

                    // procedure
                    ps = connection.prepareStatement("SELECT * FROM procedure_ WHERE procedureId = ?;");
                    ps.setString(1, experiment.getProcedure().getProcedureID());
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        procedurePk = rs.getLong("pk");
                    } else {
                        query = "INSERT INTO procedure_ (procedureId) VALUES (?);";
                        ps = connection.prepareStatement(query);
                        ps.setString(1, experiment.getProcedure().getProcedureID());
                        ps.execute();
                        rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                        rs.next();
                        procedurePk = rs.getLong(1);

                        // procedure_procedureMetadata
                        if ((experiment.getProcedure().getProcedureMetadata() != null) && ( ! experiment.getProcedure().getProcedureMetadata().isEmpty())) {
                            for (ProcedureMetadata procedureMetadata : experiment.getProcedure().getProcedureMetadata()) {
                                long procedureMetadataPk = LoaderUtils.selectOrInsertProcedureMetadata(connection, procedureMetadata.getParameterID(), null).getHjid();
                                query = "INSERT INTO procedure_procedureMetadata(procedure_fk, procedureMetadata_fk) VALUES (?, ?)";
                                ps = connection.prepareStatement(query);
                                ps.setLong(1, procedurePk);
                                ps.setLong(2, procedureMetadataPk);
                                ps.execute();
                            }
                        }
                    }

                    // center_procedure
                    ps = connection.prepareStatement("SELECT * FROM center_procedure WHERE center_fk = ? and procedure_fk = ?");
                    ps.setLong(1, centerPk);
                    ps.setLong(2, procedurePk);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        centerProcedurePk = rs.getLong("pk");
                    } else {
                        query = "INSERT INTO center_procedure (center_fk, procedure_fk) VALUES (?, ?);";
                        ps = connection.prepareStatement(query);
                        ps.setLong(1, centerPk);
                        ps.setLong(2, procedurePk);
                        ps.execute();
                        rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                        rs.next();
                        centerProcedurePk = rs.getLong(1);
                    }

                    // housing
                    if ((centerProcedure.getHousing() != null) && ( ! centerProcedure.getHousing().isEmpty())) {
                        query = "INSERT INTO housing (fromLims, lastUpdated, center_procedure_fk) VALUES (?, ?, ?);";
                        ps = connection.prepareStatement(query);
                        for (Housing housing : centerProcedure.getHousing()) {
                            ps.setInt(1, (housing.isFromLIMS() ? 1 : 0));
                            ps.setDate(2, housing.getLastUpdated() == null ? null : new Date(housing.getLastUpdated().getTime().getTime()));
                            ps.setLong(3, centerProcedurePk);
                            ps.execute();
                        }
                    }

                    // line
                    if ((centerProcedure.getLine() != null) && ( ! centerProcedure.getLine().isEmpty())) {
                        for (Line line : centerProcedure.getLine()) {
                            ps = connection.prepareStatement("SELECT * FROM line WHERE colonyId = ? AND center_procedure_fk = ?");
                            ps.setString(1, line.getColonyID());
                            ps.setLong(2, centerProcedurePk);
                            rs = ps.executeQuery();
                            long linePk;
                            if (rs.next()) {
                                linePk = rs.getLong("pk");
                            } else {
                                query = "INSERT INTO line (colonyId, center_procedure_fk) VALUES (?, ?);";
                                ps = connection.prepareStatement(query);


                                ps.setString(1, line.getColonyID());
                                ps.setLong(2, centerProcedurePk);
                                ps.execute();
                                rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                                rs.next();
                                linePk = rs.getLong(1);
                            }

                            if ((line.getStatusCode() != null) && ( ! line.getStatusCode().isEmpty())) {
                                // line_statuscode
                                query = "INSERT INTO line_statuscode (line_fk, statuscode_fk) VALUES (?, ?);";
                                ps = connection.prepareStatement(query);
                                for (StatusCode statuscode : line.getStatusCode()) {
                                    StatusCode existingStatuscode = LoaderUtils.selectOrInsertStatuscode(connection, statuscode);
                                    statuscodePk = existingStatuscode.getHjid();
                                    ps.setLong(1, linePk);
                                    ps.setLong(2, statuscodePk);
                                    ps.execute();
                                }
                            }
                        }
                    }

                    // experiment
                    ps = connection.prepareStatement("SELECT * FROM experiment WHERE experimentId = ? and center_procedure_fk = ?");
                    ps.setString(1, experiment.getExperimentID());
                    ps.setLong(2, centerProcedurePk);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        experimentPk = rs.getLong("pk");
                    } else {
                        query = "INSERT INTO experiment (dateOfExperiment, experimentId, sequenceId, center_procedure_fk) VALUES (?, ?, ?, ?);";
                        ps = connection.prepareStatement(query);
                        ps.setDate(1, experiment.getDateOfExperiment() == null ? null : new Date(experiment.getDateOfExperiment().getTime().getTime()));
                        ps.setString(2, experiment.getExperimentID());
                        if (experiment.getSequenceID() == null)
                            ps.setNull(3, Types.BIGINT);
                        else
                            ps.setString(3, experiment.getSequenceID());
                        ps.setLong(4, centerProcedurePk);
                        ps.execute();
                        rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                        rs.next();
                        experimentPk = rs.getLong(1);
                    }

                    // experiment_statuscode
                    if ((experiment.getStatusCode() != null) && ( ! experiment.getStatusCode().isEmpty())) {
                        for (StatusCode statuscode : experiment.getStatusCode()) {
                            long statuscodeFk = LoaderUtils.selectOrInsertStatuscode(connection, statuscode).getHjid();
                            ps = connection.prepareStatement("SELECT * FROM experiment_statuscode WHERE experiment_fk = ? and statuscode_fk = ?");
                            ps.setLong(1, experimentPk);
                            ps.setLong(2, statuscodeFk);
                            rs = ps.executeQuery();
                            if ( ! rs.next()) {
                                query = "INSERT INTO experiment_statuscode (experiment_fk, statuscode_fk) VALUES (?, ?)";
                                ps = connection.prepareStatement(query);
                                ps.setLong(1, experimentPk);
                                ps.setLong(2, statuscodeFk);
                                ps.execute();
                            }
                        }
                    }

                    // experiment_specimen
                    ps = connection.prepareStatement("SELECT * FROM experiment_specimen WHERE experiment_fk = ? and specimen_fk = ?");
                    ps.setLong(1, experimentPk);
                    ps.setLong(2, specimenPk);
                    rs = ps.executeQuery();
                    if ( ! rs.next()) {
                        query = "INSERT INTO experiment_specimen (experiment_fk, specimen_fk) VALUES (?, ?)";
                        ps = connection.prepareStatement(query);
                        ps.setLong(1, experimentPk);
                        ps.setLong(2, specimenPk);
                        ps.execute();
                    }

                    // simpleParameter
                    if ((experiment.getProcedure().getSimpleParameter() != null) && ( ! experiment.getProcedure().getSimpleParameter().isEmpty())) {
                        for (SimpleParameter simpleParameter : experiment.getProcedure().getSimpleParameter()) {
                            query = "INSERT INTO simpleParameter (parameterId, sequenceId, unit, value, parameterStatus, procedure_fk)"
                                    + " VALUES (?, ?, ?, ?, ?, ?)";
                            ps = connection.prepareStatement(query);
                            ps.setString(1, simpleParameter.getParameterID());
                            if (simpleParameter.getSequenceID() == null)
                                ps.setNull(2, Types.BIGINT);
                            else
                                ps.setLong(2, simpleParameter.getSequenceID().longValue());
                            ps.setString(3, simpleParameter.getUnit());
                            ps.setString(4, simpleParameter.getValue());
                            ps.setString(5, simpleParameter.getParameterStatus());
                            ps.setLong(6, procedurePk);
                            ps.execute();
                        }
                    }

                    // ontologyParameter and ontologyParameterTerm
                    if ((experiment.getProcedure().getOntologyParameter() != null) && ( ! experiment.getProcedure().getOntologyParameter().isEmpty())) {
                        for (OntologyParameter ontologyParameter : experiment.getProcedure().getOntologyParameter()) {
                            query = "INSERT INTO ontologyParameter (parameterId, parameterStatus, sequenceId, procedure_fk)"
                                    + " VALUES (?, ?, ?, ?)";
                            ps = connection.prepareStatement(query);
                            ps.setString(1, ontologyParameter.getParameterID());
                            ps.setString(2, ontologyParameter.getParameterStatus());
                            if (ontologyParameter.getSequenceID() == null)
                                ps.setNull(3, Types.BIGINT);
                            else
                                ps.setLong(3, ontologyParameter.getSequenceID().longValue());
                            ps.setLong(4, procedurePk);
                            ps.execute();
                            rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                            rs.next();
                            long ontologyParameterPk = rs.getLong(1);

                            for (String term : ontologyParameter.getTerm()) {
                                query = "INSERT INTO ontologyParameterTerm(term, ontologyParameter_fk) VALUES (?, ?)";
                                ps = connection.prepareStatement(query);
                                ps.setString(1, term);
                                ps.setLong(2, ontologyParameterPk);
                            }
                        }
                    }

                    // seriesParameter
                    if ((experiment.getProcedure().getSeriesParameter() != null) && ( ! experiment.getProcedure().getSeriesParameter().isEmpty())) {
                        for (SeriesParameter seriesParameter : experiment.getProcedure().getSeriesParameter()) {
                            query = "INSERT INTO seriesParameter (parameterId, parameterStatus, sequenceId, procedure_fk)"
                                    + " VALUES (?, ?, ?, ?)";
                            ps = connection.prepareStatement(query);
                            ps.setString(1, seriesParameter.getParameterID());
                            ps.setString(2, seriesParameter.getParameterStatus());
                            if (seriesParameter.getSequenceID() == null)
                                ps.setNull(3, Types.BIGINT);
                            else
                                ps.setLong(3, seriesParameter.getSequenceID().longValue());
                            ps.setLong(4, procedurePk);
                            ps.execute();
                            rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                            rs.next();
                            long seriesParameterPk = rs.getLong(1);

                            // seriesParameterValue
                            for (SeriesParameterValue seriesParameterValue : seriesParameter.getValue()) {
                                query = "INSERT INTO seriesParameterValue(value, incrementValue, incrementStatus, seriesParameter_fk) VALUES (?, ?, ?, ?)";
                                ps = connection.prepareStatement(query);
                                ps.setString(1, seriesParameterValue.getValue());
                                ps.setString(2, seriesParameterValue.getIncrementValue());
                                ps.setString(3, seriesParameterValue.getIncrementStatus());
                                ps.setLong(4, seriesParameterPk);
                            }
                        }
                    }

                    // mediaParameter
                    if ((experiment.getProcedure().getMediaParameter() != null) && ( ! experiment.getProcedure().getMediaParameter().isEmpty())) {
                        for (MediaParameter mediaParameter : experiment.getProcedure().getMediaParameter()) {
                            query = "INSERT INTO mediaParameter (parameterId, parameterStatus, filetype, URI, procedure_fk)"
                                    + " VALUES (?, ?, ?, ?, ?)";
                            ps = connection.prepareStatement(query);
                            ps.setString(1, mediaParameter.getParameterID());
                            ps.setString(2, mediaParameter.getParameterStatus());
                            ps.setString(3, mediaParameter.getFileType());
                            ps.setString(4, mediaParameter.getURI());
                            ps.setLong(5, procedurePk);
                            ps.execute();
                            rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                            rs.next();
                            long mediaParameterPk = rs.getLong(1);

                            // mediaParameter_parameterAssociation
                            if ((mediaParameter.getParameterAssociation() != null) && ( ! mediaParameter.getParameterAssociation().isEmpty())) {
                                for (ParameterAssociation parameterAssociation : mediaParameter.getParameterAssociation()) {
                                    long parameterAssociationPk = LoaderUtils.selectOrInsertParameterAssociation(connection, parameterAssociation).getHjid();
                                    query = "INSERT INTO mediaParameter_parameterAssociation(mediaParameter_fk, parameterAssociation_fk) VALUES (?, ?)";
                                    ps = connection.prepareStatement(query);
                                    ps.setLong(1, mediaParameterPk);
                                    ps.setLong(2, parameterAssociationPk);
                                    ps.execute();
                                }
                            }

                            // mediaParameter_procedureMetadata
                            if ((mediaParameter.getProcedureMetadata() != null) && ( ! mediaParameter.getProcedureMetadata().isEmpty())) {
                                for (ProcedureMetadata procedureMetadata : mediaParameter.getProcedureMetadata()) {
                                    long procedureMetadataPk = LoaderUtils.selectOrInsertProcedureMetadata(connection, procedureMetadata.getParameterID(), null).getHjid();
                                    query = "INSERT INTO mediaParameter_procedureMetadata(mediaParameter_fk, procedureMetadata_fk) VALUES (?, ?)";
                                    ps = connection.prepareStatement(query);
                                    ps.setLong(1, mediaParameterPk);
                                    ps.setLong(2, procedureMetadataPk);
                                    ps.execute();
                                }
                            }
                        }
                    }

                    // mediaSampleParameter
                    if ((experiment.getProcedure().getMediaSampleParameter() != null) && ( ! experiment.getProcedure().getMediaSampleParameter().isEmpty())) {
                        for (MediaSampleParameter mediaSampleParameter : experiment.getProcedure().getMediaSampleParameter()) {
                            query = "INSERT INTO mediaSampleParameter (parameterId, parameterStatus, procedure_fk)"
                                    + " VALUES (?, ?, ?)";
                            ps = connection.prepareStatement(query);
                            ps.setString(1, mediaSampleParameter.getParameterID());
                            ps.setString(2, mediaSampleParameter.getParameterStatus());
                            ps.setLong(5, procedurePk);
                            ps.execute();
                            rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                            rs.next();
                            long mediaSampleParameterPk = rs.getLong(1);

                            // mediaSample
                            for (MediaSample mediaSample : mediaSampleParameter.getMediaSample()) {
                                query = "INSERT INTO mediaSample (localId, mediaSampleParameter_fk) VALUES (?, ?, ?)";
                                ps = connection.prepareStatement(query);
                                ps.setString(1, mediaSample.getLocalId());
                                ps.setLong(2, mediaSampleParameterPk);
                                ps.execute();
                                rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                                rs.next();
                                long mediaSamplePk = rs.getLong(1);

                                // mediaSection
                                for (MediaSection mediaSection : mediaSample.getMediaSection()) {
                                    query = "INSERT INTO mediaSection (localId, mediaSample_fk) VALUES (?, ?)";
                                    ps = connection.prepareStatement(query);
                                    ps.setString(1, mediaSection.getLocalId());
                                    ps.setLong(2, mediaSamplePk);
                                    ps.execute();
                                    rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                                    rs.next();
                                    long mediaSectionPk = rs.getLong(1);

                                    // mediaFile
                                    for (MediaFile mediaFile : mediaSection.getMediaFile()) {
                                        query = "INSERT INTO mediaFile (localId, fileType, URI, mediaSection_fk) VALUES (?, ?, ?, ?)";
                                        ps = connection.prepareStatement(query);
                                        ps.setString(1, mediaFile.getLocalId());
                                        ps.setString(2, mediaFile.getFileType());
                                        ps.setString(3, mediaFile.getURI());
                                        ps.setLong(4, mediaSectionPk);
                                        ps.execute();
                                        rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                                        rs.next();
                                        long mediaFilePk = rs.getLong(1);

                                        // mediaFile_parameterAssociation
                                        if ((mediaFile.getParameterAssociation() != null) && ( ! mediaFile.getParameterAssociation().isEmpty())) {
                                            for (ParameterAssociation parameterAssociation : mediaFile.getParameterAssociation()) {
                                                long parameterAssociationPk = LoaderUtils.selectOrInsertParameterAssociation(connection, parameterAssociation).getHjid();
                                                query = "INSERT INTO mediaFile_parameterAssociation(mediaFile_fk, parameterAssociation_fk) VALUES (?, ?)";
                                                ps = connection.prepareStatement(query);
                                                ps.setLong(1, mediaFilePk);
                                                ps.setLong(2, parameterAssociationPk);
                                                ps.execute();
                                            }
                                        }

                                        // mediaFile_procedureMetadata
                                        if ((mediaFile.getProcedureMetadata() != null) && ( ! mediaFile.getProcedureMetadata().isEmpty())) {
                                            for (ProcedureMetadata procedureMetadata : mediaFile.getProcedureMetadata()) {
                                                long procedureMetadataPk = LoaderUtils.selectOrInsertProcedureMetadata(connection, procedureMetadata.getParameterID(), null).getHjid();

                                                query = "INSERT INTO mediaFile_procedureMetadata(mediaFile_fk, procedureMetadata_fk) VALUES (?, ?)";
                                                ps = connection.prepareStatement(query);
                                                ps.setLong(1, mediaFilePk);
                                                ps.setLong(2, procedureMetadataPk);
                                                ps.execute();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // seriesMediaParameter
                    if ((experiment.getProcedure().getSeriesMediaParameter() != null) && ( ! experiment.getProcedure().getSeriesMediaParameter().isEmpty())) {
                        for (SeriesMediaParameter seriesMediaParameter : experiment.getProcedure().getSeriesMediaParameter()) {
                            query = "INSERT INTO seriesMediaParameter (parameterId, parameterStatus, procedure_fk)"
                                    + " VALUES (?, ?, ?)";
                            ps = connection.prepareStatement(query);
                            ps.setString(1, seriesMediaParameter.getParameterID());
                            ps.setString(2, seriesMediaParameter.getParameterStatus());
                            ps.setLong(3, procedurePk);
                            ps.execute();
                            rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                            rs.next();
                            long seriesMediaParameterPk = rs.getLong(1);

                            // seriesMediaParameterValue
                            for (SeriesMediaParameterValue seriesMediaParameterValue : seriesMediaParameter.getValue()) {
                                query = "INSERT INTO seriesMediaParameterValue (fileType, incrementValue, URI, seriesMediaParameter_fk) VALUES (?, ?, ?, ?)";
                                ps = connection.prepareStatement(query);
                                ps.setString(1, seriesMediaParameterValue.getFileType());
                                ps.setString(2, seriesMediaParameterValue.getIncrementValue());
                                ps.setString(3, seriesMediaParameterValue.getURI());
                                ps.setLong(4, seriesMediaParameterPk);
                                ps.execute();
                                rs = ps.executeQuery("SELECT LAST_INSERT_ID();");
                                rs.next();
                                long seriesMediaParameterValuePk = rs.getLong(1);

                                // seriesMediaParameterValue_parameterAssociation
                                if ((seriesMediaParameterValue.getParameterAssociation() != null) && ( ! seriesMediaParameterValue.getParameterAssociation().isEmpty())) {
                                    for (ParameterAssociation parameterAssociation : seriesMediaParameterValue.getParameterAssociation()) {
                                        long parameterAssociationPk = LoaderUtils.selectOrInsertParameterAssociation(connection, parameterAssociation).getHjid();
                                        query = "INSERT INTO seriesMediaParameterValue_parameterAssociation(seriesMediaParameterValue_fk, parameterAssociation_fk) VALUES (?, ?)";
                                        ps = connection.prepareStatement(query);
                                        ps.setLong(1, seriesMediaParameterValuePk);
                                        ps.setLong(2, parameterAssociationPk);
                                        ps.execute();
                                    }
                                }

                                // seriesMediaParameterValue_procedureMetadata
                                if ((seriesMediaParameterValue.getProcedureMetadata() != null) && ( ! seriesMediaParameterValue.getProcedureMetadata().isEmpty())) {
                                    for (ProcedureMetadata procedureMetadata : seriesMediaParameterValue.getProcedureMetadata()) {
                                        long procedureMetadataPk = LoaderUtils.selectOrInsertProcedureMetadata(connection, procedureMetadata.getParameterID(), null).getHjid();

                                        query = "INSERT INTO seriesMediaParameterValue_procedureMetadata(seriesMediaParameterValue_fk, procedureMetadata_fk) VALUES (?, ?)";
                                        ps = connection.prepareStatement(query);
                                        ps.setLong(1, seriesMediaParameterValuePk);
                                        ps.setLong(2, procedureMetadataPk);
                                        ps.execute();
                                    }
                                }
                            }
                        }
                    }

                    connection.commit();
                }
            }
        }

        connection.close();
    }

    private String dumpSpecimen(long centerPk, long specimenPk) {
        String retVal = "";

        String query =
                "SELECT\n"
                    + "  cs.pk AS cs_pk\n"
                    + ", c.pk AS c_pk\n"
                    + ", s.pk AS s_pk\n"
                    + ", s.statuscode_fk AS s_statuscode_fk\n"
                    + ", c.centerId\n"
                    + ", c.pipeline\n"
                    + ", c.project\n"
                    + ", s.colonyId\n"
                    + ", s.gender\n"
                    + ", s.isBaseline\n"
                    + ", s.litterId\n"
                    + ", s.phenotypingCenter\n"
                    + ", s.pipeline\n"
                    + ", s.productionCenter\n"
                    + ", s.specimenId\n"
                    + ", s.strainId\n"
                    + ", s.zygosity\n"
                    + ", sc.dateOfStatuscode\n"
                    + ", sc.value\n"
                    + ", m.DOB\n"
                    + ", e.stage\n"
                    + ", e.stageUnit\n"
                    + "FROM center c\n"
                    + "JOIN center_specimen cs ON cs.center_fk = c.pk\n"
                    + "JOIN specimen s ON cs.specimen_fk = s.pk\n"
                    + "LEFT OUTER JOIN mouse m ON m.specimen_fk = cs.specimen_fk\n"
                    + "LEFT OUTER JOIN embryo e ON e.specimen_fk = cs.specimen_fk\n"
                    + "LEFT OUTER JOIN statuscode sc ON sc.pk = s.statuscode_fk\n"
                    + "WHERE c.pk = ? AND s.pk = ?;";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setLong(1, centerPk);
            ps.setLong(2, specimenPk);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                retVal += "{"
                        + "cs.pk=" + rs.getLong("cs_pk")
                        + ",c.pk=" + rs.getLong("c_pk")
                        + ",s.pk=" + rs.getLong("s_pk")
                        + ",s.statuscode_fk=" + (rs.getLong("s_statuscode_fk") == 0 ? "<null>" : rs.getLong("s_statuscode_fk"))
                        + ",centerId=" + rs.getString("c.centerId")
                        + ",pipeline=" + rs.getString("c.pipeline")
                        + ",project=" + rs.getString("c.project")
                        + ",colonyId=" + rs.getString("s.colonyId")
                        + ",gender=" + rs.getString("s.gender")
                        + ",isBaseline=" + rs.getInt("s.isBaseline")
                        + ",litterId=" + rs.getString("s.litterId")
                        + ",phenotypingCenter=" + rs.getString("s.phenotypingCenter")
                        + ",productionCenter=" + (rs.getString("s.productionCenter") == null ? "<null>" : rs.getString("s.productionCenter"))
                        + ",specimenId=" + rs.getString("s.specimenId")
                        + ",strainId=" + rs.getString("s.strainId")
                        + ",zygosity=" + rs.getString("s.zygosity");
                if (rs.getLong("s_statuscode_fk") != 0) {
                    retVal += ",sc.dateOfStatuscode=" + (rs.getDate("sc.dateOfStatuscode") == null ? "<null>" : rs.getDate("sc.dateOfStatuscode"))
                    + ",sc.value=" + rs.getString("sc.value");
                }
                retVal += (rs.getDate("m.DOB") == null ? " (EMBRYO)" : " (MOUSE)");
            }

        } catch (SQLException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }

        return retVal;
    }
}