/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.cli.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Metadata importer to allow the batch import of metadata from a file
 */
public class OsuImport {


    /** Logger */
    private static final Logger log = Logger.getLogger(OsuImport.class);

    private static void printHelp(Options options, int exitCode) {
        // print the help message
        HelpFormatter myhelp = new HelpFormatter();
        myhelp.printHelp("OsuImport\n", options);
        System.exit(exitCode);
    }

    /**
     * main method to run the metadata exporter
     *
     * @param argv the command line arguments given
     */
    public static void main(String[] argv) {

        // Create a context
        Context c;
        try {
            c = new Context();
            c.turnOffAuthorisationSystem();
        } catch (Exception e) {
            System.err.println("Unable to create a new DSpace Context: " + e.getMessage());
            System.exit(1);
            return;
        }


        // Create an options object and populate it
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "file", true, "source file");
        options.addOption("e", "email", true, "email address or user id of user");
        options.addOption("t", "type", true, "type of file [tab|csv] (default is 'tab')");

        // Parse the command line arguments
        CommandLine line;
        try {
            line = parser.parse(options, argv);
        } catch (ParseException pe) {
            System.err.println("Error parsing command line arguments: " + pe.getMessage());
            System.exit(1);
            return;
        }

        // Check an email is given
        if (!line.hasOption('e')) {
            System.err.println("Required parameter -e missing!");
            printHelp(options, 1);
        }

        // Check a filename is given
        if (!line.hasOption('f')) {
            System.err.println("Required parameter -f missing!");
            printHelp(options, 1);
        }

        String filename = line.getOptionValue('f');

        String type = line.getOptionValue('t');

        // Transform the csv that osu provides to a csv that the Metadata import can handle
        try {

            filename = transformCsv(c, type, filename);

            int indexF = ArrayUtils.indexOf(argv, "-f") + 1;
            argv[indexF] = filename;

            MetadataImport.main(argv);

            // Finsh off and tidy up
            c.restoreAuthSystemState();
            c.complete();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            log.error(e.getMessage(), e);
            c.abort();
        }
    }

    private static String transformCsv(Context c, String type, String filename) throws IOException, SQLException {

        // read
        InputStream input = new FileInputStream(new File(filename));
        Reader csvReader = new InputStreamReader(input);
        List<String[]> rows = new CSVReader(csvReader, "tab".equals(type) || type == null ? '\t' : ',').readAll();
        String[][] csvParsed = rows.toArray(new String[rows.size()][]);
        String[] header = csvParsed[0];

        String[][] csvExtended = new String[csvParsed.length][header.length + 2];
        String[] columnsToAdd = {"id", "collection"};

        header = mapHeaders(header);
        int indexOfDepartment = ArrayUtils.indexOf(header, "osu.department");
        Collection[] collections = Collection.findAll(c);

        csvExtended[0] = (String[]) ArrayUtils.addAll(columnsToAdd, header);
        for (int i = 1; i < csvParsed.length; i++) {
            String[] row = csvParsed[i];
            String[] valuesToAdd = new String[columnsToAdd.length];

            // add to column "id"
            valuesToAdd[0] = "+";

            // add to column "collection"
            valuesToAdd[1] = findCollectionHandle(collections, row[indexOfDepartment]);

            csvExtended[i] = (String[]) ArrayUtils.addAll(valuesToAdd, row);
        }

        // save
        File tempDirectory = new File(ConfigurationManager.getProperty("dspace.dir") + File.separator + "temp" + File.separator);
        tempDirectory.mkdirs();
        File tempCsv = new File(tempDirectory.getPath() + File.separatorChar + "temp.csv");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(tempCsv));


        for (int i = 0; i < csvExtended.length; i++) {
            String[] row = csvExtended[i];
            csvWriter.writeNext(row);
        }

        csvWriter.flush();
        csvWriter.close();

        return tempCsv.getAbsolutePath();
    }

    private static String[] mapHeaders(String[] header) {

        String[] newHeaders = new String[header.length];

        HashMap<String, String> osu_map = new HashMap<String, String>();
        osu_map.put("title", "dc.title");
        osu_map.put("publication-date", "dc.date.issued");
        osu_map.put("authors", "dc.contributor.author");
        osu_map.put("keywords", "dc.subject");
        osu_map.put("document-type", "dc.format.mimetype");
        osu_map.put("abstract", "dc.description.abstract");
        osu_map.put("note", "dc.type");
        osu_map.put("rights", "dc.rights");

        osu_map.put("department", "osu.department");
        osu_map.put("college", "osu.college");
        osu_map.put("order number", "osu.order.number");
        osu_map.put("filename", "osu.filename");
        osu_map.put("ft_type", "osu.accesstype");


        for (int i = 0; i < header.length; i++) {
            String schema = osu_map.get(header[i]);
            if (StringUtils.isNotBlank(schema)) {
                newHeaders[i] = schema;
            } else {
                newHeaders[i] = header[i];
                log.info("No metadata schema found for column heading " + header[i]);
            }
        }

        return newHeaders;
    }

    private static String findCollectionHandle(Collection[] collections, String departmentName) {

        for (Collection collection : collections) {
            if (collection.getName().equals(departmentName)) {
                return collection.getHandle();
            }
        }

        System.err.println("Collection not found. department=" + departmentName);
        log.error("Collection not found. department=" + departmentName);

        return null;
    }
}
