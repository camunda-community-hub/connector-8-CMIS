/* ******************************************************************** */
/*                                                                      */
/*  CreateFolderWorker                                                   */
/*                                                                      */
/*  Create a folder in CMIS      */
/* ******************************************************************** */
package io.camunda.cherry.cmis.createfolder;

import io.camunda.cherry.cmis.CmisCherryToolbox;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisFactoryConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import io.camunda.cherry.definition.AbstractWorker;
import io.camunda.cherry.definition.BpmnError;
import io.camunda.cherry.definition.RunnerParameter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

@Component
public class CreateFolderWorker extends AbstractWorker {

    public static final String INPUT_FOLDER_PATH = "folderPath";
    public static final String INPUT_FOLDER_NAME = "folderName";
    public static final String INPUT_RECURSIVE_NAME = "recursiveName";
    public static final String INPUT_FOLDER_CMIS_TYPE = "folderCmisType";

    public static final String OUTPUT_FOLDER_ID_OUTPUT = "folderId";

    public static final BpmnError ERROR_INVALID_PARENT = BpmnError.getInstance("INVALID_PARENT", "Can't retrieve the parent");
    public static final BpmnError ERROR_FOLDER_CREATION = BpmnError.getInstance("FOLDER_CREATION", "Error during the creation of one object");

    public CreateFolderWorker() {
        super("c-cmis-createfolder",
                Arrays.asList(
                        CmisCherryToolbox.CmisConnectionParameter,
                        RunnerParameter.getInstance(INPUT_FOLDER_PATH,
                                "Parent Folder Path",
                                String.class, RunnerParameter.Level.REQUIRED, "Folder path where folder will be created"),
                        RunnerParameter.getInstance(INPUT_FOLDER_NAME,
                                "Folder Name",
                                String.class, RunnerParameter.Level.REQUIRED, "Folder name to be created."),
                        RunnerParameter.getInstance(INPUT_RECURSIVE_NAME,
                                        "Recursive Name",
                                        Boolean.class, RunnerParameter.Level.OPTIONAL, "Recursive name: folder name can contains '/'")
                                .setVisibleInTemplate()
                                .setDefaultValue(Boolean.FALSE),
                        RunnerParameter.getInstance(INPUT_FOLDER_CMIS_TYPE,
                                        "Folder CMIS Type",
                                        String.class, RunnerParameter.Level.OPTIONAL, "When an CMIS object is created, a type is assigned")
                                .setDefaultValue("cmis:folder")

                ),
                List.of(
                        RunnerParameter.getInstance(OUTPUT_FOLDER_ID_OUTPUT,
                                "Folder ID created",
                                String.class, RunnerParameter.Level.REQUIRED, "Folder ID created. In case of a recursive creation, ID of the last folder (deeper)")
                ),
                Arrays.asList(CmisCherryToolbox.NO_CONNECTION_TO_CMIS, ERROR_INVALID_PARENT));
    }

    @Override
    public String getName() {
        return "CMIS: Create folder";
    }

    @Override
    public String getDescription() {
        return "Create folder(s) in the CMIS repository";
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


        String parentFolderPath = getInputStringValue(INPUT_FOLDER_PATH, null, activatedJob);
        Folder parentFolder = null;
        try {
            parentFolder = cmisConnection.getFolderByPath(parentFolderPath);
        } catch (Exception e) {
            // is managed after
        }
        if (parentFolder == null)
            throw new ZeebeBpmnError(ERROR_INVALID_PARENT.getCode(), "Can't find [" + parentFolderPath + "] ");

        String cmisType = getInputStringValue(INPUT_FOLDER_CMIS_TYPE, "cmis:folder", activatedJob);
        Boolean recursiveName = getInputBooleanValue(INPUT_RECURSIVE_NAME, Boolean.TRUE, activatedJob);

        String folderName = getInputStringValue(INPUT_FOLDER_NAME, null, activatedJob);
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
                    throw new ZeebeBpmnError(ERROR_FOLDER_CREATION.getCode(), "Folder[" + currentFolderName + "] in [" + folderName + "] :"+e.getCause()+" "+e.getMessage());
                }
            }
        } else {

            final HashMap<String, Object> properties = new HashMap<>();
            properties.put(PropertyIds.NAME, folderName);
            properties.put(PropertyIds.OBJECT_TYPE_ID, cmisType);
            try {
                folderCreated = parentFolder.createFolder(properties);
            } catch (Exception e) {
                throw new ZeebeBpmnError(ERROR_FOLDER_CREATION.getCode(), "Folder[" + folderName + "] :"+e.getCause()+" "+e.getMessage());
            }
        }
        if (folderCreated == null)
            throw new ZeebeBpmnError(ERROR_FOLDER_CREATION.getCode(), "Folder name is empty");
        setOutputValue(OUTPUT_FOLDER_ID_OUTPUT, folderCreated.getId(), contextExecution);
    }

}
