package io.camunda.connector.cmis;

import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.cmis.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

public class CmisOutput implements CherryOutput {
  public String getOutputFolderIdOutput;

  public static final String OUTPUT_FOLDER_ID = "folderId";
  public String folderId;

  public static final String OUTPUT_LIST_OBJECT_DELETED = "ListObjectsDeleted";

  public List<String> listObjectsDeleted;


              @Override
  public List<Map<String, Object>> getOutputParameters() {
    return ParameterToolbox.getOutputParameters();
  }

}
