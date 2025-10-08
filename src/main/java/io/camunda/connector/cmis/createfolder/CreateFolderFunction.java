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
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.connector.cmis.toolbox.CmisToolbox;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CreateFolderFunction implements CmisSubFunction {
    public static final String NAME = "name";
    public static final String FOLDER_ID = "folderId";
    public static final String TYPE_CMIS_CREATE_FOLDER = "create-folder";
    private final Logger logger = LoggerFactory.getLogger(CreateFolderFunction.class.getName());

    public CreateFolderFunction() {
        // No special to add here
    }

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {
        CmisOutput cmisOutput = new CmisOutput();
        cmisOutput.listFoldersCreated = new ArrayList<>();

        String parentFolderPath = cmisInput.getFolderPath();
        Folder parentFolder = null;
        try {
            parentFolder = cmisConnection.getFolderByPath(parentFolderPath);
        } catch (Exception e) {
            // is managed after
        }
        if (parentFolder == null)
            throw new ConnectorException(CmisError.INVALID_PARENT, "Can't find [" + parentFolderPath + "] ");

        String cmisType = cmisInput.getCmisType(CmisInput.CMIS_TYPE_V_CMIS_FOLDER);
        Boolean recursiveName = cmisInput.getRecursiveName();

        StringBuilder listFoldersCreatedToLog = new StringBuilder();

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
                    cmisOutput.listFoldersCreated.add(CmisToolbox.createDescription(folderCreated));
                    listFoldersCreatedToLog.append(currentFolderName).append("[").append(folderCreated.getId()).append("],");
                    parentFolder = folderCreated;
                } catch (Exception e) {
                    throw new ConnectorException(CmisError.FOLDER_CREATION,
                            "Folder[" + currentFolderName + "] in [" + folderName + "] :" + e.getCause() + " " + e.getMessage());
                }
            }
        } else {

            final HashMap<String, Object> properties = new HashMap<>();
            properties.put(PropertyIds.NAME, folderName);
            properties.put(PropertyIds.OBJECT_TYPE_ID, cmisType);
            try {
                folderCreated = parentFolder.createFolder(properties);
                cmisOutput.listFoldersCreated.add(CmisToolbox.createDescription(folderCreated));

                listFoldersCreatedToLog.append(folderName).append("[").append(folderCreated.getId()).append("],");
            } catch (Exception e) {
                throw new ConnectorException(CmisError.FOLDER_CREATION,
                        "Folder[" + folderName + "] :" + e.getCause() + " " + e.getMessage());
            }
        }
        if (folderCreated == null)
            throw new ConnectorException(CmisError.FOLDER_CREATION, "Folder name is empty");

        cmisOutput.folderId = folderCreated.getId();
        logger.info("Folder Parent[{}] listIdFolderCreated[{}]", parentFolderPath, listFoldersCreatedToLog);
        return cmisOutput;
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return List.of(
                new RunnerParameter(CmisInput.CMIS_CONNECTION, "CMIS Connection", String.class,
                        RunnerParameter.Level.REQUIRED, "Cmis Connection. JSON like {\"url\":\"http://localhost:8099/cmis/browser\",\"userName\":\"test\",\"password\":\"test\"}"),
                new RunnerParameter(CmisInput.FOLDER_PATH, "Parent Folder Path", String.class,
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
        return List.of(
                RunnerParameter.getInstance(CmisOutput.FOLDER_ID, "Folder ID created", String.class,
                        RunnerParameter.Level.OPTIONAL,
                        "Folder ID created. In case of a recursive creation, ID of the last folder (deeper)"),
                RunnerParameter.getInstance(CmisOutput.LIST_FOLDERS_CREATED, "List folders", List.class,
                        RunnerParameter.Level.OPTIONAL,
                        "List of " + CmisOutput.DESCRIPTION_EXPLANATION + " for each folder created or existing in the path"));
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(CmisError.FOLDER_CREATION, CmisError.FOLDER_CREATION_EXPLANATION,
                CmisError.INVALID_PARENT, CmisError.INVALID_PARENT_EXPLANATION);

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
        return TYPE_CMIS_CREATE_FOLDER;
    }

}
