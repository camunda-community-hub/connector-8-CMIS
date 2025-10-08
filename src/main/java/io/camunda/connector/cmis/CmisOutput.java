package io.camunda.connector.cmis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.cmis.toolbox.ParameterToolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CmisOutput implements CherryOutput {
    public static final String FOLDER_ID = "folderId";
    public static final String LIST_OBJECT_DELETED = "ListObjectsDeleted";
    public static final String LIST_FOLDERS_CREATED = "listFoldersCreated";
    public final static String LIST_CMIS_OBJECT = "listCmisObject";
    public static final String DOCUMENTID = "documentId";
    public static final String FILE_LOADED = "fileLoaded";
    public static String DESCRIPTION_EXPLANATION = "`id`:CmisId, `name`: name of object, `description1 of the object, `isFolder` true if the object is a folder";
    public String getOutputFolderIdOutput;
    public String folderId;
    public List<String> listObjectsDeleted;
    public List<CmisOutput.CmisObjectDescription> listFoldersCreated;
    public List<CmisObjectDescription> listCmisObject = new ArrayList<>();
    public String documentId;
    public Object fileLoaded;

    public String getDocumentId() {
        return documentId;
    }

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getOutputParameters() {
        return ParameterToolbox.getOutputParameters();
    }

    public static class CmisObjectDescription {
        public String id;
        public String name;
        public String description;
        public boolean isFolder;
    }

}
