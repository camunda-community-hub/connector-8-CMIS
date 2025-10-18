package io.camunda.connector.cmis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.ParameterToolbox;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.storage.StorageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * the JsonIgnoreProperties is mandatory: the template may contain additional widget to help the designer, especially on the OPTIONAL parameters
 * This avoids the MAPPING Exception
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmisInput implements CherryInput {
    public static final String GROUP_STORAGE_DEFINITION = "Storage definition";
    /**
     * Attention, each Input here must be added in the function, list of InputVariables
     */
    public static final String CMIS_FUNCTION = "cmisFunction";
    public static final String CMIS_CONNECTION = "cmisConnection";
    public static final String FOLDER_PATH = "folderPath";
    public static final String FOLDER_NAME = "folderName";
    public static final String RECURSIVE_NAME = "recursiveName";
    public static final String JSON_STORAGE_DEFINITION = "jsonStorageDefinition";
    public static final String STORAGE_DEFINITION = "storageDefinition";
    public static final String STORAGE_DEFINITION_FOLDER_COMPLEMENT = "storageDefinitionComplement";
    public static final String STORAGE_DEFINITION_CMIS_COMPLEMENT = "storageDefinitionCmis";
    public static final String ERROR_IF_NOT_EXIST = "errorIfNotExist";
    public static final String FOLDER_ID = "folderId";


    /**
     * Upload file
     * The file maybe in a FileStorage, or a Camunda document
     */
    public static final String SOURCE_FILE = "sourceFile";
    public static final String ABSOLUTE_FOLDER_NAME = "absoluteFolderName";
    public static final String UPLOAD_FOLDER_NAME = "uploadFolderName";
    public static final String DOCUMENT_NAME = "documentName";
    public static final String CMIS_TYPE = "cmisType";
    public static final String CMIS_TYPE_V_CMIS_DOCUMENT = "cmis:document";
    public static final String CMIS_TYPE_V_CMIS_FOLDER = "cmis:folder";
    public static final String IMPORT_POLICY = "importPolicy";
    public static final String IMPORT_POLICY_V_NEW_DOCUMENT = "NEW_DOCUMENT";
    public static final String IMPORT_POLICY_V_NEW_VERSION = "NEW_VERSION";
    public static final String VERSION_LABEL = "versionLabel";
    /**
     * Download file
     */
    public static final String SOURCE_OBJECT = "sourceObject";
    public static final String SOURCE_OBJECT_V_ID = "objectId";
    public static final String SOURCE_OBJECT_V_ABSOLUTEPATHNAME = "absolutePathName";
    public static final String SOURCE_OBJECT_V_FOLDERCONTENT = "folderContent";
    public static final String SOURCE_OBJECT_V_ID_LABEL = "objectId";
    public static final String SOURCE_OBJECT_V_ABSOLUTEPATHNAME_LABEL = "Absolute PathName";
    public static final String SOURCE_OBJECT_V_FOLDERCONTENT_LABEL = "Folder Content";
    public static final String CMIS_OBJECT_ID = "cmisObjectId";
    public static final String FILTER = "filter";

    public static final String FOLDER_IDENTIFICATION = "folderIdentification";
    public static final String FOLDER_IDENTIFICATION_V_PATH = "FOLDER";
    public static final String FOLDER_IDENTIFICATION_V_ID = "ID";
    public static final String FOLDER_IDENTIFICATION_PATH = "folderIdentificationPath";
    public static final String FOLDER_IDENTIFICATION_ID = "folderIdentificationId";

    private final Logger logger = LoggerFactory.getLogger(CmisInput.class.getName());
    public Object cmisConnection;
    public String folderPath;
    public String folderName;
    public Boolean recursiveName;
    public Object jsonStorageDefinition;
    public String storageDefinition;
    public String storageDefinitionComplement;
    public String storageDefinitionCmis;
    public Boolean errorIfNotExist;
    public String folderId;
    public Object sourceFile;
    public String absoluteFolderName;
    public String uploadFolderName;
    public String documentName;
    public String cmisType;
    public String importPolicy;
    public String versionLabel;
    public String sourceObject;
    public String filter;
    public String folderIdentification;

    public String folderIdentificationPath;
    public String folderIdentificationId;

    //   public static final String ABSOLUTE_FOLDER_NAME = "absoluteFolderName";
    String cmisObjectId;
    private String cmisFunction;

    public String getSourceObject() {
        return sourceObject;
    }

    public String getFilter() {
        return filter;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public Object getSourceFile() {
        return sourceFile;
    }

    public String getImportPolicy() {
        return importPolicy;
    }

    public String getAbsoluteFolderName() {
        return absoluteFolderName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getCmisType(String defaultValue) {
        return cmisType == null || cmisType.trim().isEmpty() ? defaultValue : cmisType;
    }

    public String getCmisFunction() {
        return cmisFunction;
    }

    public Object getCmisConnection() {
        return cmisConnection;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getFolderName() {
        return folderName;
    }

    public Boolean getRecursiveName() {
        return recursiveName;
    }

    public String getUploadFolderName() {
        return uploadFolderName;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getCmisObjectId() {
        return cmisObjectId;
    }


    public Boolean getErrorIfNotExist() {
        return errorIfNotExist;
    }

    public Object getJsonStorageDefinition() {
        return jsonStorageDefinition;
    }

    public String getStorageDefinition() {
        return storageDefinition;
    }

    public String getStorageDefinitionComplement() {
        return storageDefinitionComplement;
    }

    public String getStorageDefinitionCmis() {
        return storageDefinitionCmis;
    }

    public String getFolderIdentification() {
        return folderIdentification;
    }

    public String getFolderIdentificationId() {
        return folderIdentificationId;
    }

    public String getFolderIdentificationPath() {
        return folderIdentificationPath;
    }

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();


    }


    public FileVariable initializeOutputFileVariable(String fileName) throws ConnectorException {
        StorageDefinition storageOutputDefinition = getStorageDefinitionObject();

        FileVariable fileVariable = new FileVariable();
        fileVariable.setStorageDefinition(storageOutputDefinition);
        fileVariable.setName(fileName);
        return fileVariable;
    }

    /**
     * Return a Storage definition
     *
     * @return the storage definition
     * @throws ConnectorException if the connection
     */
    @JsonIgnore
    public StorageDefinition getStorageDefinitionObject() throws ConnectorException {
        try {

            StorageDefinition storageDefinitionObj = null;
            // Attention, it may be an empty string due to the modeler which not like null value
            if (jsonStorageDefinition != null && !jsonStorageDefinition.toString().trim().isEmpty()) {
                storageDefinitionObj = StorageDefinition.getFromObject(jsonStorageDefinition);
                return storageDefinitionObj;
            }

            storageDefinitionObj = StorageDefinition.getFromStorageDefinition(getStorageDefinition());
            storageDefinitionObj.complement = getStorageDefinitionComplement();
            if (storageDefinitionObj.complement != null && storageDefinitionObj.complement.isEmpty())
                storageDefinitionObj.complement = null;

            storageDefinitionObj.complementInObject = getStorageDefinitionCmis();
            return storageDefinitionObj;
        } catch (Exception e) {
            logger.error("Can't get the FileStorage - bad Gson value :" + getStorageDefinition());
            throw new ConnectorException(CmisError.INCORRECT_STORAGEDEFINITION,
                    "FileStorage information" + getStorageDefinition());
        }
    }

}
