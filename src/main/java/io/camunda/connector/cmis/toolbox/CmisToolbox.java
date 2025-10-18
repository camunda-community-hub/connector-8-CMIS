package io.camunda.connector.cmis.toolbox;

import com.google.gson.Gson;
import io.camunda.connector.cmis.CmisOutput;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;

public class CmisToolbox {

    /**
     * Explore the String connection to return an object value.
     * Cmis connection should be a JSON like {"url":"http://localhost:8099/lightweightcmis/browser","userName":"cmisaccess","password":"demo"}
     *
     * @param cmisConnectionString the cmis connection
     * @return the object parsed by Gson
     * @throws Exception
     */
    public static Object getCmisConnection(String cmisConnectionString) throws Exception {
        String valueInJson = cmisConnectionString;
        valueInJson = valueInJson.replace("\\\"", "\"");

        try {
            Gson gson = new Gson();
            return gson.fromJson(valueInJson, Object.class);
        } catch (Exception var7) {
            throw new Exception("Can't decode the GSON on " + valueInJson);
        }
    }

    /**
     * Create a description from a CmisObject
     *
     * @param cmisObject the source object
     * @return the description
     */
    public static CmisOutput.CmisObjectDescription createDescription(CmisObject cmisObject) {
        CmisOutput.CmisObjectDescription description = new CmisOutput.CmisObjectDescription();

        description.id = cmisObject.getId();
        description.name = cmisObject.getName();
        description.description = cmisObject.getDescription();
        description.isFolder = cmisObject instanceof Folder;
        return description;
    }
}
