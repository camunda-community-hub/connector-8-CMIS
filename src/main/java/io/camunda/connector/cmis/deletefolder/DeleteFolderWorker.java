/* ******************************************************************** */
/*  DeleteFolder                                                                   */
/*                                                                      */
/*  Delete a folder                                                     */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.connector.cmis.deletefolder;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisFunction;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeleteFolderWorker implements CmisSubFunction {

  @Override
  public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                       CmisInput cmisInput,
                                       OutboundConnectorContext context) throws ConnectorException {
    CmisOutput cmisOutput = new CmisOutput();
    String folderPath = cmisInput.getFolderPath();
    String folderId = cmisInput.getFolderId();
    if (folderId != null && folderPath != null) {

      throw new ConnectorException(CmisFunction.BPMNERROR_ERROR_DOUBLE_FOLDER, "");

    }

    Boolean errorIfNotExist = cmisInput.getErrorIfNotExist();

    CmisObject cmisObject = null;
    if (folderPath != null) {
      cmisObject = cmisConnection.getObjectByPath(folderId);
      if (folderId != null)
        cmisObject = cmisConnection.getObjectById(folderId);
      if (cmisObject == null) {
        if (errorIfNotExist)
          throw new ConnectorException(CmisFunction.BPMNERROR_ERROR_FOLDER_NOT_EXIST,
              "Can't find FolderPath[" + folderPath + "] folderID[" + folderId + "]");
        cmisOutput.listObjectsDeleted = Collections.emptyList();
        return cmisOutput;
      }

      if (cmisObject instanceof Folder cmisFolder) {
        cmisOutput.listObjectsDeleted = cmisFolder.deleteTree(true, UnfileObject.DELETE, true);

      } else
        throw new ConnectorException(CmisFunction.BPMNERROR_ERROR_OBJECT_IS_NOT_A_FOLDER,
            "Cmis Object is not a folder FolderPath[" + folderPath + "] folderID[" + folderId + "]");

    }

  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(CmisInput.INPUT_FOLDER_PATH, "Folder to delete", String.class,
            RunnerParameter.Level.OPTIONAL, "Folder path to delete. Contains '/' to select sub folder"), //
        RunnerParameter.getInstance(CmisInput.INPUT_FOLDER_ID, "Folder to delete", String.class,
            RunnerParameter.Level.OPTIONAL, "Folder Id to reference the folder to delete"), //
        RunnerParameter.getInstance(CmisInput.INPUT_ERROR_IF_NOT_EXIST, "Error if not exist", Boolean.class,
                RunnerParameter.Level.OPTIONAL, "Throw a BPMN Error if the object does not exist") //
            .setDefaultValue(Boolean.FALSE) //
            .setVisibleInTemplate());
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(
        RunnerParameter.getInstance(CmisOutput.OUTPUT_LIST_OBJECT_DELETED, "Folder ID created", String.class,
            RunnerParameter.Level.REQUIRED,
            "Folder ID created. In case of a recursive creation, ID of " + "the last folder (deeper)"));
  }

  @Override
  public Map<String, String> getBpmnErrors() {
    Map.of(CmisFunction.BPMNERROR_ERROR_DOUBLE_FOLDER, CmisFunction.BPMNERROR_ERROR_DOUBLE_FOLDER_EXPL,
        CmisFunction.BPMNERROR_ERROR_FOLDER_NOT_EXIST, CmisFunction.BPMNERROR_ERROR_FOLDER_NOT_EXIST_EXPL,
        CmisFunction.BPMNERROR_ERROR_OBJECT_IS_NOT_A_FOLDER, CmisFunction.BPMNERROR_ERROR_OBJECT_IS_NOT_A_FOLDER_EXPL);
  }

  @Override
  public String getSubFunctionName() {
    return "DeleteFolder";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Delete a folder in the CMIS repository";
  }

  @Override
  public String getSubFunctionType() {
    return "delete-folder";
  }
}
