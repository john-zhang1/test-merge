package org.dspace.itemimport;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.Curator;
import org.dspace.event.Consumer;
import org.dspace.event.Event;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * On item creation, the item is check for osu.filename.
 * This consumer adds the file as a bitstream to the item
 * when it is found in the configured directory.
 *
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 13/09/13
 */
public class CSVImport implements Consumer {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(CSVImport.class);

    @Override
    public void initialize() throws Exception {
    }

    @Override
    public void consume(Context context, Event event) throws Exception {

        int st = event.getSubjectType();
        int et = event.getEventType();

        if (st == Constants.ITEM && et == Event.CREATE) {
            Item item = (Item) event.getSubject(context);

            if (item != null && item.isArchived()) {
                Metadatum[] filename = item.getMetadata("osu", "filename", Item.ANY, Item.ANY);

                if (filename.length > 0) {
                    String filedir = ConfigurationManager.getProperty("event.consumer.csvimport.filedir");
                    String file = filename[0].value;
                    InputStream fileInputStream = new FileInputStream(filedir + "/" + file);

                    //Get or create the bundle
                    Bundle[] bundles = item.getBundles("ORIGINAL");
                    Bitstream bitstream;

                    if (bundles.length < 1) {
                        // set bundle's name to ORIGINAL
                        bitstream = item.createSingleBitstream(fileInputStream, "ORIGINAL");
                    } else {
                        // we have a bundle already, just add bitstream
                        bitstream = bundles[0].createBitstream(fileInputStream);
                    }

                    bitstream.setName(file);
                    bitstream.setSource(filedir);
                    BitstreamFormat bitstreamFormat = FormatIdentifier.guessFormat(context, bitstream);
                    bitstream.setFormat(bitstreamFormat);

                    // Update to DB
                    bitstream.update();
                    item.update();

                    bitstreamChecks(bitstreamFormat, bitstream, item);
                }
            }
        }
    }

    private void bitstreamChecks(BitstreamFormat bitstreamFormat, Bitstream bitstream, Item item) throws IOException, SQLException, AuthorizeException {

        if ((bitstreamFormat != null) && (bitstreamFormat.isInternal())) {
            log.warn("Attempt to upload file format marked as internal system use only");
            backoutBitstream(bitstream, item);
        }

        // Check for virus
        if (ConfigurationManager.getBooleanProperty("submission-curation", "virus-scan")) {
            Curator curator = new Curator();
            curator.addTask("vscan").curate(item);
            int status = curator.getStatus("vscan");
            if (status == Curator.CURATE_ERROR) {
                backoutBitstream(bitstream, item);
            } else if (status == Curator.CURATE_FAIL) {
                backoutBitstream(bitstream, item);
            }
        }
    }

    /**
     * If we created a new Bitstream but now realised there is a problem then remove it.
     */
    private void backoutBitstream(Bitstream b, Item item) throws SQLException, AuthorizeException, IOException {
        // remove bitstream from bundle..
        // delete bundle if it's now empty
        Bundle[] bnd = b.getBundles();

        bnd[0].removeBitstream(b);

        Bitstream[] bitstreams = bnd[0].getBitstreams();

        // remove bundle if it's now empty
        if (bitstreams.length < 1) {
            item.removeBundle(bnd[0]);
            item.update();
        }

    }

    @Override
    public void end(Context ctx) throws Exception {
    }

    @Override
    public void finish(Context ctx) throws Exception {
    }
}
