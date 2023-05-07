/* ******************************************************************** */
/*  DeleteFolder                                                                   */
/*                                                                      */
/*  Delete a folder                                                     */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.cherry.cmis.deletefolder;

import io.camunda.cherry.cmis.CmisCherryToolbox;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisFactoryConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import io.camunda.cherry.definition.AbstractWorker;
import io.camunda.cherry.definition.BpmnError;
import io.camunda.cherry.definition.RunnerParameter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class DeleteFolderWorker extends AbstractWorker {
    public static final String INPUT_FOLDER_PATH = "folderPath";
    public static final String INPUT_FOLDER_ID = "FolderId";
    public static final String INPUT_ERROR_IF_NOT_EXIST = "ErrorIfNotExist";

    public static final String OUTPUT_LIST_OBJECT_DELETED = "ListObjectsDeleted";


    public static final BpmnError ERROR_DOUBLE_FOLDER = BpmnError.getInstance("DOUBLE_OBJECT", "Folder path and Folder Id are fulfill. Only one must be set");
    public static final BpmnError ERROR_FOLDER_NOT_EXIST = BpmnError.getInstance("FOLDER_NOT_EXIST", "Folder does not exists");
    public static final BpmnError ERROR_OBJECT_IS_NOT_A_FOLDER = BpmnError.getInstance("OBJECT_IS_NOT_A_FOLDER", "Object must be a folder");


    public DeleteFolderWorker() {
        super("c-cmis-deletefolder",
                Arrays.asList(
                        CmisCherryToolbox.CmisConnectionParameter,
                        RunnerParameter.getInstance(INPUT_FOLDER_PATH,
                                "Folder to delete",
                                String.class, RunnerParameter.Level.OPTIONAL, "Folder path to delete. Contains '/' to select sub folder"),
                        RunnerParameter.getInstance(INPUT_FOLDER_ID,
                                "Folder to delete",
                                String.class, RunnerParameter.Level.OPTIONAL, "Folder Id to reference the folder to delete"),
                        RunnerParameter.getInstance(INPUT_ERROR_IF_NOT_EXIST,
                                        "Error if not exist",
                                        Boolean.class, RunnerParameter.Level.OPTIONAL, "Throw a BPMN Error if the object does not exist")
                                .setDefaultValue(Boolean.FALSE)
                                .setVisibleInTemplate()
                ),
                Collections.singletonList(
                        RunnerParameter.getInstance(OUTPUT_LIST_OBJECT_DELETED,
                                "Folder ID created",
                                String.class, RunnerParameter.Level.REQUIRED, "Folder ID created. In case of a recursive creation, ID of the last folder (deeper)")
                ),
                Arrays.asList(CmisCherryToolbox.NO_CONNECTION_TO_CMIS, ERROR_DOUBLE_FOLDER, ERROR_FOLDER_NOT_EXIST, ERROR_OBJECT_IS_NOT_A_FOLDER));
    }

    @Override
    public String getName() {
        return "CMIS: Delete folder";
    }

    @Override
    public String getDescription() {
        return "Delete a folder in the CMIS repository";
    }

    @Override
    public String getLogo() {
        return CmisCherryToolbox.getLogo();
    }
    @Override
    public String getCollectionName() {
        return CmisCherryToolbox.getCollectionName();
    }

    /**
     * @param jobClient        client
     * @param activatedJob     job activated
     * @param contextExecution context of this execution
     */
    @Override
    public void execute(final JobClient jobClient, final ActivatedJob activatedJob, AbstractWorker.ContextExecution contextExecution) {
        CmisConnection cmisConnection;
        try {
            CmisParameters cmisParameters = CmisParameters.getCodingConnection(getInputGsonValue(CmisCherryToolbox.INPUT_CMIS_CONNECTION, null, activatedJob));

            cmisConnection = CmisFactoryConnection.getInstance().getCmisConnection(cmisParameters);
        } catch (Exception e) {
            throw new ZeebeBpmnError(CmisCherryToolbox.NO_CONNECTION_TO_CMIS.getCode(), "No connection");
        }


        String folderPath = getInputStringValue(INPUT_FOLDER_PATH, null, activatedJob);
        String folderId = getInputStringValue(INPUT_FOLDER_ID, null, activatedJob);
        if (folderId != null && folderPath != null) {

            throw new ZeebeBpmnError(ERROR_DOUBLE_FOLDER.getCode(), "");

        }

        Boolean errorIfNotExist = getInputBooleanValue(INPUT_ERROR_IF_NOT_EXIST, Boolean.FALSE, activatedJob);

        CmisObject cmisObject = null;
        if (folderPath != null) {
            cmisObject = cmisConnection.getObjectByPath(folderId);
            if (folderId != null)
                cmisObject = cmisConnection.getObjectById(folderId);
            if (cmisObject == null) {
                if (errorIfNotExist)
                    throw new ZeebeBpmnError(ERROR_FOLDER_NOT_EXIST.getCode(), "Can't find FolderPath[" + folderPath + "] folderID[" + folderId + "]");
                setValue(OUTPUT_LIST_OBJECT_DELETED, Collections.emptyList(), contextExecution);
                return;
            }

            if (cmisObject instanceof Folder cmisFolder) {
                List<String> objectDeleted = cmisFolder.deleteTree(true, UnfileObject.DELETE, true);
                setValue(OUTPUT_LIST_OBJECT_DELETED, objectDeleted, contextExecution);

            } else
                throw new ZeebeBpmnError(ERROR_OBJECT_IS_NOT_A_FOLDER.getCode(), "Cmis Object is not a folder FolderPath[" + folderPath + "] folderID[" + folderId + "]");

        }

    }
}
