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
        String folderPath = cmisInput.getFolderIdentificationPath();
        String folderId = cmisInput.getFolderIdentificationId();
        String folderIdentification = cmisInput.getFolderIdentification();
        CmisObject cmisObject = null;
        if (CmisInput.FOLDER_IDENTIFICATION_V_PATH.equals(folderIdentification)) {
            if (folderPath != null)
                cmisObject = cmisConnection.getObjectByPath(folderPath);
        } else if (CmisInput.FOLDER_IDENTIFICATION_V_ID.equals(folderIdentification)) {
            if (folderId != null)
                cmisObject = cmisConnection.getObjectById(folderId);
        }

        Boolean errorIfNotExist = cmisInput.getErrorIfNotExist();

        if (cmisObject == null) {
            if (Boolean.TRUE.equals(errorIfNotExist))
                throw new ConnectorException(CmisError.FOLDER_NOT_EXIST,
                        "Can't find FolderPath[" + folderPath + "] folderID[" + folderId + "]");
            cmisOutput.ListObjectsNotDeleted = Collections.emptyList();
            return cmisOutput;
        }

        if (cmisObject instanceof Folder cmisFolder) {
            cmisOutput.ListObjectsNotDeleted = cmisFolder.deleteTree(true, UnfileObject.DELETE, true);
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

                RunnerParameter.getInstance(CmisInput.FOLDER_IDENTIFICATION, "Identify the folder to delete", String.class,
                                RunnerParameter.Level.REQUIRED, "Identify the cluster")
                        .addChoice(CmisInput.FOLDER_IDENTIFICATION_V_PATH, "Folder Path")
                        .addChoice(CmisInput.FOLDER_IDENTIFICATION_V_ID, "Folder Id")
                ,

                RunnerParameter.getInstance(CmisInput.FOLDER_IDENTIFICATION_PATH, "Folder Path to delete", String.class,
                                RunnerParameter.Level.REQUIRED, "Folder path to delete. Contains '/' to select sub folder") //
                        .addCondition(CmisInput.FOLDER_IDENTIFICATION, List.of(CmisInput.FOLDER_IDENTIFICATION_V_PATH)),

                RunnerParameter.getInstance(CmisInput.FOLDER_IDENTIFICATION_ID, "Folder ID to delete", String.class,
                                RunnerParameter.Level.REQUIRED, "Folder Id to reference the folder to delete") //
                        .addCondition(CmisInput.FOLDER_IDENTIFICATION, List.of(CmisInput.FOLDER_IDENTIFICATION_V_ID)),

                RunnerParameter.getInstance(CmisInput.ERROR_IF_NOT_EXIST, "Error if not exist", Boolean.class,
                                RunnerParameter.Level.OPTIONAL, "Throw a BPMN Error if the object does not exist") //
                        .setDefaultValue(Boolean.FALSE) //
                        .setVisibleInTemplate());
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(
                RunnerParameter.getInstance(CmisOutput.LIST_OBJECT_NOTDELETED, "Folders ID NOT deleted", List.class,
                        RunnerParameter.Level.REQUIRED,
                        "List of Folder ID Not deleted, which cannot be deleted by the CMIS engine."));
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
        return "Delete a folder in the CMIS repository. This is a recursive deletion: folder and all the content of the folder will be deleted. It returns the objects it could not destroy.";
    }

    @Override
    public String getSubFunctionType() {
        return TYPE_CMIS_DELETE_FOLDER;
    }
}
