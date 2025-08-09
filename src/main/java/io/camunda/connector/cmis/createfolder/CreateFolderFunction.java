/* ******************************************************************** */
/*                                                                      */
/*  CreateFolderWorker                                                   */
/*                                                                      */
/*  Create a folder in CMIS      */
/* ******************************************************************** */
package io.camunda.connector.cmis.createfolder;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class CreateFolderFunction implements CmisSubFunction {

  public CreateFolderFunction() {
    // No special to add here
  }

  @Override
  public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                       CmisInput cmisInput,
                                       OutboundConnectorContext context) throws ConnectorException {

    String parentFolderPath = cmisInput.getFolderPath();
    Folder parentFolder = null;
    try {
      parentFolder = cmisConnection.getFolderByPath(parentFolderPath);
    } catch (Exception e) {
      // is managed after
    }
    if (parentFolder == null)
      throw new ConnectorException(CmisError.BPMNERROR_INVALID_PARENT, "Can't find [" + parentFolderPath + "] ");

    String cmisType = cmisInput.getCmisType();
    if (cmisType == null || cmisType.isEmpty())
      cmisType = "cmis:folder";
    Boolean recursiveName = cmisInput.getRecursiveName();

    String folderName = cmisInput.getFolderName();
    Folder folderCreated = null;
    if (Boolean.TRUE.equals(recursiveName)) {
      StringTokenizer st = new StringTokenizer(folderName, "/");
      while (st.hasMoreTokens()) {
        String currentFolderName = st.nextToken();
        final HashMap<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.NAME, currentFolderName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, cmisType);
        try {
          folderCreated = parentFolder.createFolder(properties);
          parentFolder = folderCreated;
        } catch (Exception e) {
          throw new ConnectorException(CmisError.BPMNERROR_FOLDER_CREATION,
              "Folder[" + currentFolderName + "] in [" + folderName + "] :" + e.getCause() + " " + e.getMessage());
        }
      }
    } else {

      final HashMap<String, Object> properties = new HashMap<>();
      properties.put(PropertyIds.NAME, folderName);
      properties.put(PropertyIds.OBJECT_TYPE_ID, cmisType);
      try {
        folderCreated = parentFolder.createFolder(properties);
      } catch (Exception e) {
        throw new ConnectorException(CmisError.BPMNERROR_FOLDER_CREATION,
            "Folder[" + folderName + "] :" + e.getCause() + " " + e.getMessage());
      }
    }
    if (folderCreated == null)
      throw new ConnectorException(CmisError.BPMNERROR_FOLDER_CREATION, "Folder name is empty");

    CmisOutput cmisOutput = new CmisOutput();
    cmisOutput.folderId = folderCreated.getId();
    return cmisOutput;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return List.of(new RunnerParameter(CmisInput.INPUT_FOLDER_PATH, "Parent Folder Path", String.class,
            RunnerParameter.Level.REQUIRED, "Folder path where folder will be created"),
        new RunnerParameter(CmisInput.CMIS_TYPE, "Folder CMIS Type", String.class,
            RunnerParameter.Level.OPTIONAL, "When an CMIS object is created, a type is assigned") //
            .setDefaultValue("cmis:folder"), //
        new RunnerParameter(CmisInput.RECURSIVE_NAME, "Recursive Name", Boolean.class,
            RunnerParameter.Level.OPTIONAL, "Recursive name: folder name can contains '/'") //
            .setVisibleInTemplate() //
            .setDefaultValue(Boolean.FALSE),//
        new RunnerParameter(CmisInput.FOLDER_NAME, "Folder Name", String.class, RunnerParameter.Level.REQUIRED,
            "Folder name to be created."));
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return List.of(RunnerParameter.getInstance(CmisOutput.OUTPUT_FOLDER_ID, "Folder ID created", String.class,
        RunnerParameter.Level.REQUIRED,
        "Folder ID created. In case of a recursive creation, ID of the last folder (deeper)"));
  }

  @Override
  public Map<String, String> getBpmnErrors() {
    return Map.of(CmisError.BPMNERROR_FOLDER_CREATION, CmisError.BPMNERROR_FOLDER_CREATION_EXPL,
            CmisError.BPMNERROR_INVALID_PARENT, CmisError.BPMNERROR_INVALID_PARENT_EXPL);

  }

  @Override
  public String getSubFunctionName() {
    return "CreateFolder";
  }


  @Override
  public String getSubFunctionDescription() {
    return "Create a folder in the CMIS repository, under the folder parent";
  }

  @Override
  public String getSubFunctionType() {
    return "create-folder";
  }

}
