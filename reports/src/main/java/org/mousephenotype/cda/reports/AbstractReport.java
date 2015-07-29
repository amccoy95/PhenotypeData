/*******************************************************************************
 *  Copyright © 2013 - 2015 EMBL - European Bioinformatics Institute
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this targetFile except in compliance
 *  with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied. See the License for the specific
 *  language governing permissions and limitations under the
 *  License.
 ******************************************************************************/

package org.mousephenotype.cda.reports;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.mousephenotype.cda.reports.support.MpCSVWriter;
import org.mousephenotype.cda.reports.support.ReportException;
import org.mousephenotype.cda.reports.support.ReportParser;
import org.mousephenotype.cda.utilities.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for reports.
 *
 * Created by mrelac on 24/07/2015.
 */
public abstract class AbstractReport implements CommandLineRunner {
    protected PropertiesConfiguration applicationProperties;
    protected MpCSVWriter csvWriter;
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected File targetFile;
    protected String targetFilename;

    protected List<String> resources = Arrays.asList(new String[] {"IMPC", "3i"});

    @Autowired
    CommonUtils commonUtils;

    protected ReportParser parser = new ReportParser();

    public abstract String getDefaultFilename();

    public enum ReportFormat {
        csv(','),
        tsv('\t')
        ;

        char separator;

        ReportFormat(char separator) {
            this.separator = separator;
        }

        public char getSeparator() {
            return this.separator;
        }
    }

    public AbstractReport() {

    }

    public PropertiesConfiguration getApplicationProperties() {
        return applicationProperties;
    }

    public File getTargetFile() {
        return targetFile;
    }


// PROTECTED METHODS


    protected void initialise(String[] args) throws ReportException {
        List<String> errors = parser.validate(parser.parse(args));
        if ( ! errors.isEmpty()) {
            for (String error : errors) {
                System.out.println(error);
            }
            System.out.println();
            usage();
            System.exit(1);
        }

        if (parser.showHelp()) {
            usage();
            System.exit(0);
        }

        this.targetFilename =
                  parser.getPrefix()
                + (parser.getTargetFilename() != null ? parser.getTargetFilename() : getDefaultFilename())
                + "."
                + parser.getReportFormat();

        this.targetFile = new File(Paths.get(parser.getTargetDirectory(), targetFilename).toAbsolutePath().toString());
        try {
            FileWriter fileWriter = new FileWriter(targetFile.getAbsoluteFile());
            this.csvWriter = new MpCSVWriter(fileWriter, parser.getReportFormat().getSeparator());
        } catch (IOException e) {
            throw new ReportException("Exception opening FileWriter: " + e.getLocalizedMessage());
        }

        logInputParameters();
    }


    protected void usage() {
        String[] commands = {
                  "   [--" + ReportParser.TARGET_FILENAME_ARG + "=target_filename]"
                , "   [--" + ReportParser.TARGET_DIRECTORY_ARG + "=target_directory]"
                , "   [--" + ReportParser.REPORT_FORMAT_ARG    + "={csv | tsv}]"
                , "   [--" + ReportParser.PROPERTIES_FILE_ARG  + "=properties_file]"
                , "   [--" + ReportParser.PREFIX_ARG           + "=prefix]"
                , "   [--" + ReportParser.HELP_ARG             + "]"
        };
        String[] defaults = {
                  "Default is " + getDefaultFilename()
                , "Default is " + ReportParser.DEFAULT_TARGET_DIRECTORY
                , "Default is " + ReportParser.DEFAULT_REPORT_FORMAT
                , "Default is " + ReportParser.DEFAULT_PROPERTIES_FILE
                , "Default is none"
                , ""
        };
        System.out.println("Usage:");
        for (int i = 0; i < commands.length; i++) {
            System.out.println(String.format("%-50.50s %-30s", commands[i], defaults[i]));
        }
        System.out.println();
    }

    protected void logInputParameters() {
        log.info("Target filename:  " + targetFile.getAbsolutePath());
        log.info("Target directory: " + parser.getTargetDirectory());
        log.info("Report format:    " + parser.getReportFormat());
        log.info("Properties targetFile:  " + (parser.getApplicationProperties() == null ? "<omitted>" : parser.getApplicationProperties().getURL().toString()));
    }
}
