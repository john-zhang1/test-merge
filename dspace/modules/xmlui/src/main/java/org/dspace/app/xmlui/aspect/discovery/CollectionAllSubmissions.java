/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.discovery;

import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.ReferenceSet;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.discovery.DiscoverResult;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders a list of recently submitted items for the collection by using
 * discovery
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Zhongda Zhang (zhongda at ou dot edu)
 */
public class CollectionAllSubmissions extends AbstractRecentSubmissionTransformer {

    private static final Message T_head_recent_submissions
            = message("xmlui.ArtifactBrowser.CollectionViewer.head_recent_submissions");

    /**
     * Displays the recent submissions for this collection
     */
    public void addBody(Body body) throws SAXException, WingException,
            SQLException, IOException, AuthorizeException {

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        // Set up the major variables
        Collection collection = (Collection) dso;
        if (!(dso instanceof Collection)) {
            return;
        }

        getSubmittedAllItems(collection);

        //Only attempt to render our result if we have one.
        if (queryResults == null) {
            return;
        }

        if (0 < queryResults.getDspaceObjects().size()) {
            // Build the data file
            getMetadataValue(queryResults);
        }
    }

    /**
     * Processing all collection metadata value and save into 4 lists
     */
    private void getMetadataValue(DiscoverResult queryResults) throws UnsupportedEncodingException {
        List<String> titles = new ArrayList<String>();
        List<String> handles = new ArrayList<String>();
        List<String> cities = new ArrayList<String>();
        List<String> coordinates = new ArrayList<String>();
        String homestate = null;
        String homecity = null;
        List<DSpaceObject> listobjs = queryResults.getDspaceObjects();

        for (int i = 0; i < listobjs.size(); i++) {
            DSpaceObject dsobj = listobjs.get(i);
            handles.add("/handle/" + dsobj.getHandle());
            Metadatum[] dcvs = dsobj.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
            for (Metadatum dcv : dcvs) {
                String qualifier = dcv.qualifier;
                if (qualifier.equals("sampleid")) {
                    titles.add(dcv.value);
                }
                if (qualifier.equals("homecity")) {
                    homecity = dcv.value;
                }
                if (qualifier.equals("homestate")) {
                    homestate = dcv.value;
                }
                if (qualifier.equals("spatial")) {
                    coordinates.add(dcv.value);
                }
            }
            cities.add(homecity + ", " + homestate.substring(0, 2));
        }
        String filename = "/srv/shareok/dspace/webapps/xmlui/themes/Research/scripts/points.js";
        WriteFile(titles, handles, coordinates, cities, filename);
    }

    /**
     * Write a file to host all data which will be used by map
     */
    private void WriteFile(List<String> titlelist, List<String> urllist, List<String> spatiallist, List<String> placelist, String filename) {

        try {
            File file = new File(filename);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);

            writer.write("var titlelist = [");
            for(int i=0;i<titlelist.size();i++){
                String str = titlelist.get(i);
                writer.write("\"" + str + "\"");
                if(i<titlelist.size()-1){
                    writer.write(",");
                }
            }
            writer.write("];");
            writer.write(String.format("%n"));

            writer.write("var spatiallist = [");
             for(int i=0;i<spatiallist.size();i++){
                String str = spatiallist.get(i);
                writer.write("\"" + str + "\"");
                if(i<spatiallist.size()-1){
                    writer.write(",");
                }
            }
            writer.write("];");
            writer.write(String.format("%n"));

            writer.write("var urllist = [");
             for(int i=0;i<urllist.size();i++){
                String str = urllist.get(i);
                writer.write("\"" + str + "\"");
                if(i<urllist.size()-1){
                    writer.write(",");
                }
            }
            writer.write("];");
            writer.write(String.format("%n"));

            writer.write("var placelist = [");
             for(int i=0;i<placelist.size();i++){
                String str = placelist.get(i);
                writer.write("\"" + str + "\"");
                if(i<placelist.size()-1){
                    writer.write(",");
                }
            }
            writer.write("];");
            writer.write(String.format("%n"));

            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(CollectionAllSubmissions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
