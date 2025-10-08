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
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeleteFolderFunction implements CmisSubFunction {
    public static final String TYPE_CMIS_DELETE_FOLDER = "delete-folder";
    private final Logger logger = LoggerFactory.getLogger(DeleteFolderFunction.class.getName());

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {
        CmisOutput cmisOutput = new CmisOutput();
        String folderPath = cmisInput.getFolderPath();
        String folderId = cmisInput.getFolderId();
        if (folderId != null && folderPath != null) {
            throw new ConnectorException(CmisError.ERROR_DOUBLE_FOLDER, "");
        }

        Boolean errorIfNotExist = cmisInput.getErrorIfNotExist();

        CmisObject cmisObject = null;
        if (folderPath != null)
            cmisObject = cmisConnection.getObjectByPath(folderPath);
        if (folderId != null)
            cmisObject = cmisConnection.getObjectById(folderId);
        if (cmisObject == null) {
            if (errorIfNotExist)
                throw new ConnectorException(CmisError.FOLDER_NOT_EXIST,
                        "Can't find FolderPath[" + folderPath + "] folderID[" + folderId + "]");
            cmisOutput.listObjectsDeleted = Collections.emptyList();
            return cmisOutput;
        }

        if (cmisObject instanceof Folder cmisFolder) {
            cmisOutput.listObjectsDeleted = cmisFolder.deleteTree(true, UnfileObject.DELETE, true);

        } else
            throw new ConnectorException(CmisError.NOT_A_FOLDER,
                    "Cmis Object is not a folder FolderPath[" + folderPath + "] folderID[" + folderId + "]");

        logger.info("Folder path[{}] Id[{}] CmisId[{}] deleted", folderPath, folderId, cmisObject.getId());
        return cmisOutput;

    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                new RunnerParameter(CmisInput.CMIS_CONNECTION, "CMIS Connection", String.class,
                        RunnerParameter.Level.REQUIRED, "Cmis Connection. JSON like {\"url\":\"http://localhost:8099/cmis/browser\",\"userName\":\"test\",\"password\":\"test\"}"),

                RunnerParameter.getInstance(CmisInput.FOLDER_PATH, "Folder to delete", String.class,
                        RunnerParameter.Level.OPTIONAL, "Folder path to delete. Contains '/' to select sub folder"), //
                RunnerParameter.getInstance(CmisInput.FOLDER_ID, "Folder to delete", String.class,
                        RunnerParameter.Level.OPTIONAL, "Folder Id to reference the folder to delete"), //
                RunnerParameter.getInstance(CmisInput.ERROR_IF_NOT_EXIST, "Error if not exist", Boolean.class,
                                RunnerParameter.Level.OPTIONAL, "Throw a BPMN Error if the object does not exist") //
                        .setDefaultValue(Boolean.FALSE) //
                        .setVisibleInTemplate());
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(
                RunnerParameter.getInstance(CmisOutput.LIST_OBJECT_DELETED, "Folder ID created", String.class,
                        RunnerParameter.Level.REQUIRED,
                        "Folder ID created. In case of a recursive creation, ID of " + "the last folder (deeper)"));
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(CmisError.ERROR_DOUBLE_FOLDER, CmisError.ERROR_DOUBLE_FOLDER_EXPLANATION,
                CmisError.FOLDER_NOT_EXIST, CmisError.FOLDER_NOT_EXIST_EXPLANATION,
                CmisError.NOT_A_FOLDER, CmisError.NOT_A_FOLDER_EXPLANATION);
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
        return TYPE_CMIS_DELETE_FOLDER;
    }
}
