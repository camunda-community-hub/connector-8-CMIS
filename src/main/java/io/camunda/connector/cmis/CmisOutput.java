package io.camunda.connector.cmis;

import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.cmis.toolbox.ParameterToolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CmisOutput implements CherryOutput {
  public String getOutputFolderIdOutput;

  public static final String OUTPUT_FOLDER_ID = "folderId";
  public String folderId;

  public static final String OUTPUT_LIST_OBJECT_DELETED = "ListObjectsDeleted";

  public List<String> listObjectsDeleted;
  public final static String OUTPUT_LIST_OBJECT = "listCmisObject";
  public List<CmisObjectDescription> listCmisObject = new ArrayList<>();

  public static final String OUTPUT_CMIS_DOCUMENTID = "documentId";

  public String documentId;

  public String getdocumentId() {
    return documentId;
  }


  public static class CmisObjectDescription {
    public String id;
    public String name;
    public String description;
    public boolean isFolder;
  }

              @Override
  public List<Map<String, Object>> getOutputParameters() {
    return ParameterToolbox.getOutputParameters();
  }

}
