/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.AuthenticationUtil;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.*;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.http.HttpEnvironment;
/**
 * Display an item restricted message.
 *
 * @author Scott Phillips
 * @author Mark Diggory  mdiggory at atmire dot com
 * @author Fabio Bolognisi fabio at atmire dot com
 */
public class RestrictedItem extends AbstractDSpaceTransformer //implements CacheableProcessingComponent
{
    private static final Logger log = Logger.getLogger(RestrictedItem.class);

    /**
     * language strings
     */
    private static final Message T_title =
            message("xmlui.ArtifactBrowser.RestrictedItem.title");

    private static final Message T_dspace_home =
            message("xmlui.general.dspace_home");

    private static final Message T_trail =
            message("xmlui.ArtifactBrowser.RestrictedItem.trail");

    private static final Message T_head_resource =
            message("xmlui.ArtifactBrowser.RestrictedItem.head_resource");

    private static final Message T_head_community =
            message("xmlui.ArtifactBrowser.RestrictedItem.head_community");

    private static final Message T_head_collection =
            message("xmlui.ArtifactBrowser.RestrictedItem.head_collection");

    private static final Message T_head_item =
            message("xmlui.ArtifactBrowser.RestrictedItem.head_item");

    private static final Message T_head_item_withdrawn =
            message("xmlui.ArtifactBrowser.RestrictedItem.head_item_withdrawn");

    private static final Message T_head_bitstream =
            message("xmlui.ArtifactBrowser.RestrictedItem.head_bitstream");

    private static final Message T_para_resource =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_resource");

    private static final Message T_para_community =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_community");

    private static final Message T_para_collection =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_collection");

    private static final Message T_para_item =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_item");

    private static final Message T_para_bitstream =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_bitstream");


    // Item states
    private static final Message T_para_item_restricted_auth =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_item_restricted_auth");
    private static final Message T_para_item_restricted =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_item_restricted");
    private static final Message T_para_item_withdrawn =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_item_withdrawn");


    private static final Message T_para_login =
            message("xmlui.ArtifactBrowser.RestrictedItem.login");

    private static final Message T_head_item_embargo =
            message("xmlui.ArtifactBrowser.RestrictedItem.para_item_embargo");


    private static final Message T_head_item_restricted =
            message("xmlui.ArtifactBrowser.RestrictedItem.auth_restricted_message_item");


    private static final String AUTH_REQUIRED_HEADER = "xmlui.ArtifactBrowser.RestrictedItem.auth_header";
    private  String AUTH_REQUIRED_EMBARGO_MESSAGE = "xmlui.RestrictedItem.auth_embargo_message";
    private  String AUTH_REQUIRED_RESTRICTED_MESSAGE = "xmlui.RestrictedItem.auth_restricted_message";


    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        pageMeta.addMetadata("title").addContent(T_title);

        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        if (dso != null) {
            HandleUtil.buildHandleTrail(dso, pageMeta, contextPath);
        }
        pageMeta.addTrail().addContent(T_trail);

    }


    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException,
            ResourceNotFoundException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        Division unauthorized = null;
        boolean isWithdrawn = false;

        if (dso == null)
        {
            unauthorized = body.addDivision("unauthorized-resource", "primary");
            unauthorized.setHead(T_head_resource);
            unauthorized.addPara(T_para_resource);
        }
        else if (dso instanceof Community)
        {
            Community community = (Community) dso;
            unauthorized = body.addDivision("unauthorized-resource", "primary");
            unauthorized.setHead(T_head_community);
            unauthorized.addPara(T_para_community.parameterize(community.getMetadata("name")));
        }
        else if (dso instanceof Collection)
        {
            Collection collection = (Collection) dso;
            unauthorized = body.addDivision("unauthorized-resource", "primary");
            unauthorized.setHead(T_head_collection);
            unauthorized.addPara(T_para_collection.parameterize(collection.getMetadata("name")));
        }
        else if (dso instanceof Item)
        {
            // The dso may be an item but it could still be an item's bitstream. So let's check for the parameter.
            if (request.getParameter("bitstreamId") != null) {
                String identifier = "unknown";
                try {
                    Bitstream bit = Bitstream.find(context, new Integer(request.getParameter("bitstreamId")));
                    if (bit != null) {
                        identifier = bit.getName();
                    }
                } catch (Exception e) {
                    // just forget it - and display the restricted message.
                    log.trace("Caught exception", e);
                }
                unauthorized = body.addDivision("unauthorized-resource", "primary");
                unauthorized.setHead(T_head_bitstream);
                unauthorized.addPara(T_para_bitstream.parameterize(identifier));

            } else {

                String identifier = "unknown";
                String handle = dso.getHandle();
                if (handle == null || "".equals(handle)) {
                    identifier = "internal ID: " + dso.getID();
                } else {
                    identifier = "hdl:" + handle;
                }

                // check why the item is restricted.
                String divID = "restricted";
                Message title = T_head_item;
                Message status = T_para_item_restricted;
                //if item is withdrawn, display withdrawn status info
                if (((Item) dso).isWithdrawn())
                {
                    divID = "withdrawn";
                    title = T_head_item_withdrawn;
                    status = T_para_item_withdrawn;
                    isWithdrawn = true;
                }//if user is not authenticated, display info to authenticate
                else if (context.getCurrentUser() == null)
                {
                    //status = T_para_item_restricted_auth;
                }
                unauthorized = body.addDivision(divID, "primary");
                unauthorized.setHead(title);
                //unauthorized.addPara(T_para_item.parameterize(identifier));
                //unauthorized.addPara("item_status", status.getKey()).addContent(status);

                Item item = (Item) dso;
                String startDate = "";
                String group_name = "";
                String message_embargo = "";
                String message_restricted = "";

                Map<String,String> date_group = new HashMap<String,String>();
                ArrayList groups = new ArrayList();

                for (ResourcePolicy rp : AuthorizeManager.getPoliciesActionFilter(context, item, Constants.READ))
                {
                    if(rp.getStartDate() != null){
                        startDate = DateFormatUtils.format(rp.getStartDate(), "yyyy-MM-dd");
                    }
                    if(rp.getGroup() != null){
                        group_name= rp.getGroup().getName();
                    }

                    if(date_group != null && date_group.size() > 0)
                    {
                        if(date_group.containsKey(startDate))
                        {
                            if(!date_group.get(startDate).equals(group_name))
                            {
                                date_group.put(startDate,group_name);

                            }
                        }
                        else
                            date_group.put(startDate,group_name);
                    }
                    else
                        date_group.put(startDate,group_name);


                    if(groups != null && groups.size() > 0)
                    {
                        if(!groups.contains(group_name))
                        {
                            groups.add(group_name);
                        }
                    }
                    else
                        groups.add(group_name);

                }

                int k=0;
                for (String key : date_group.keySet()) {
                    if(k == (date_group.size()-1))
                        message_embargo += " until " +key+" to "+ date_group.get(key) + " users. ";
                    else
                        message_embargo += " until " +key+" to "+ date_group.get(key) + " users, ";
                    k++;
                }

                for (int i = 0; i < groups.size(); i++) {
                    if( i == (groups.size()-1))
                        message_restricted += groups.get(i) + " users.";
                    else
                        message_restricted += groups.get(i) + " users, ";
                }

                if(startDate.length()>1)
                     //unauthorized.addPara(T_head_item_embargo.parameterize(message_embargo));
                    AuthenticationUtil.interruptRequest(objectModel, AUTH_REQUIRED_HEADER, AUTH_REQUIRED_EMBARGO_MESSAGE, message_embargo);
                 else
                    //unauthorized.addPara(T_head_item_restricted.parameterize(message_restricted));
                    AuthenticationUtil.interruptRequest(objectModel, AUTH_REQUIRED_HEADER, AUTH_REQUIRED_RESTRICTED_MESSAGE, message_restricted);

            }
        } // end if Item
        else
        {
            // This case should not occur, but if it does just fall back to the resource message.
            unauthorized = body.addDivision("unauthorized-resource", "primary");
            unauthorized.setHead(T_head_resource);
            unauthorized.addPara(T_para_resource);
        }

        // add a login link if !loggedIn & not withdrawn
        if (!isWithdrawn && context.getCurrentUser() == null)
        {
            unauthorized.addPara().addXref(contextPath+"/login", T_para_login);


            /*String header = parameters.getParameter("header", null);
            String message = parameters.getParameter("message", null);
            String characters = parameters.getParameter("characters", null);



            AuthenticationUtil.interruptRequest(objectModel, header, message, characters);     */
        }


        HttpServletResponse response = (HttpServletResponse)objectModel
                .get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
