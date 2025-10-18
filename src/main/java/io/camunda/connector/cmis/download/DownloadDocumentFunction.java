package io.camunda.connector.cmis.download;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.sourceobject.CmisSourceAccess;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import io.camunda.filestorage.storage.StorageDefinition;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DownloadDocumentFunction implements CmisSubFunction {
    public static final String NAME_CMIS_DOWNLOAD_DOCUMENT = "CMIS: Download document";
    public static final String TYPE_CMIS_DOWNLOAD_DOCUMENT = "download-document";
    private final Logger logger = LoggerFactory.getLogger(DownloadDocumentFunction.class.getName());

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection, CmisInput cmisInput, OutboundConnectorContext context) throws ConnectorException {


        CmisOutput cmisOutput = new CmisOutput();
        // ----------- get object to download
        List<CmisObject> listCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, cmisInput);
        if (listCmisObject.size() > 1) {
            throw new ConnectorException(CmisError.TOO_MANY_OBJECTS, "The selection must return one and only one document");
        }
        if (listCmisObject.isEmpty()) {
            return cmisOutput;
        }
        CmisObject cmisObject = listCmisObject.get(0);
        if (!(cmisObject instanceof Document cmisDocument)) {
            throw new ConnectorException(CmisError.NOT_A_DOCUMENT, "The selection must return a Document, which contains a content");
        }

        // ----- get the FileOutput in the correct Storage content
        StorageDefinition storageOutputDefinition = cmisInput.getStorageDefinitionObject();
        FileVariable fileVariableOutput = new FileVariable();
        fileVariableOutput.setStorageDefinition(storageOutputDefinition);
        fileVariableOutput.setName(cmisDocument.getName());
        fileVariableOutput.setValueStream(cmisDocument.getContentStream().getStream());
        fileVariableOutput.setMimeType(cmisDocument.getContentStreamMimeType());

        // ------- Write it now
        try {
            FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
            FileVariableReference fileVariableOutputReference = fileRepoFactory.saveFileVariable(fileVariableOutput, context);
            cmisOutput.fileLoaded = fileVariableOutputReference;

        } catch (Exception e) {
            throw new ConnectorException(CmisError.ERROR_DURING_READ, "Error during download the document " + e.getMessage());
        }
        logger.info("DocumentUpload [{}] to FileStorage [{}]", cmisDocument.getId(), storageOutputDefinition.type);
        return cmisOutput;
    }


    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                new RunnerParameter(CmisInput.CMIS_CONNECTION, "CMIS Connection", String.class,
                        RunnerParameter.Level.REQUIRED, "Cmis Connection. JSON like {\"url\":\"http://localhost:8099/cmis/browser\",\"userName\":\"test\",\"password\":\"test\"}"),

                RunnerParameter.getInstance(CmisInput.SOURCE_OBJECT, "Type Cmis Object", String.class, // class
                                RunnerParameter.Level.REQUIRED, // level
                                "Type of Cmis Object to access")// explanation
                        .addChoice(CmisInput.SOURCE_OBJECT_V_ID, CmisInput.SOURCE_OBJECT_V_ID_LABEL) //
                        .addChoice(CmisInput.SOURCE_OBJECT_V_ABSOLUTEPATHNAME, CmisInput.SOURCE_OBJECT_V_ABSOLUTEPATHNAME_LABEL) //
                        .addChoice(CmisInput.SOURCE_OBJECT_V_FOLDERCONTENT, CmisInput.SOURCE_OBJECT_V_FOLDERCONTENT_LABEL),


                RunnerParameter.getInstance(CmisInput.CMIS_OBJECT_ID, "Cmis Object", String.class, // class
                                RunnerParameter.Level.REQUIRED, // level
                                "ObjectId")// explanation
                        .addCondition(CmisInput.SOURCE_OBJECT, Collections.singletonList(CmisInput.SOURCE_OBJECT_V_ID)),

                RunnerParameter.getInstance(CmisInput.ABSOLUTE_FOLDER_NAME, "Folder CMIS path", String.class, // class
                                RunnerParameter.Level.REQUIRED, // level
                                "ObjectId")// explanation
                        .addCondition(CmisInput.SOURCE_OBJECT, Arrays.asList(CmisInput.SOURCE_OBJECT_V_ABSOLUTEPATHNAME, CmisInput.SOURCE_OBJECT_V_FOLDERCONTENT)),

                RunnerParameter.getInstance(CmisInput.FILTER, "Filter to select files", String.class, // class
                        RunnerParameter.Level.OPTIONAL, // level
                        "Give a filter to select a dedicated file")// explanation
                ,


                RunnerParameter.getInstance(CmisInput.STORAGE_DEFINITION, "Storage definition", String.class, RunnerParameter.Level.OPTIONAL,
                                // level
                                "How to saved the FileVariable. " + StorageDefinition.StorageDefinitionType.JSON + " to save in the engine (size is linited), " + StorageDefinition.StorageDefinitionType.TEMPFOLDER + " to use the temporary folder of THIS machine" + StorageDefinition.StorageDefinitionType.FOLDER + " to specify a folder to save it (to be accessible by multiple machine if you ruin it in a cluster" + StorageDefinition.StorageDefinitionType.CMIS + " to specify a CMIS connection") //
                        .addChoice("JSON", StorageDefinition.StorageDefinitionType.JSON.toString()).addChoice(StorageDefinition.StorageDefinitionType.TEMPFOLDER.toString(), StorageDefinition.StorageDefinitionType.TEMPFOLDER.toString()).addChoice(StorageDefinition.StorageDefinitionType.FOLDER.toString(), StorageDefinition.StorageDefinitionType.FOLDER.toString()).addChoice(StorageDefinition.StorageDefinitionType.CMIS.toString(), StorageDefinition.StorageDefinitionType.CMIS.toString()).setVisibleInTemplate().setDefaultValue(StorageDefinition.StorageDefinitionType.JSON.toString()).setGroup(CmisInput.GROUP_STORAGE_DEFINITION),

                RunnerParameter.getInstance(CmisInput.STORAGE_DEFINITION_FOLDER_COMPLEMENT, "FOLDER Storage definition Complement", String.class, // class
                                RunnerParameter.Level.REQUIRED, // level
                                "Provide the FOLDER path on the server")// explanation
                        .addCondition(CmisInput.STORAGE_DEFINITION, Collections.singletonList(StorageDefinition.StorageDefinitionType.FOLDER.toString())).setGroup(CmisInput.GROUP_STORAGE_DEFINITION),

                RunnerParameter.getInstance(CmisInput.STORAGE_DEFINITION_CMIS_COMPLEMENT, // name
                                "CMIS Storage definition Complement", // label
                                Object.class, // type
                                RunnerParameter.Level.REQUIRED, // level
                                "Complement to the Storage definition, if needed. " + StorageDefinition.StorageDefinitionType.FOLDER + ": please provide the folder to save the file") // parameter
                        .setGsonTemplate(CmisParameters.getGsonTemplate()) // add Gson Template
                        .addCondition(CmisInput.STORAGE_DEFINITION, Collections.singletonList(StorageDefinition.StorageDefinitionType.CMIS.toString())).setGroup(CmisInput.GROUP_STORAGE_DEFINITION),

                RunnerParameter.getInstance(CmisInput.JSON_STORAGE_DEFINITION, // name
                                "Storage definition in JSON", // label
                                Object.class, // type
                                RunnerParameter.Level.OPTIONAL, // level
                                "Give a JSON information to access the storage definition") // parameter
                        .setGroup(CmisInput.GROUP_STORAGE_DEFINITION));


    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(RunnerParameter.getInstance(CmisOutput.FILE_LOADED, //
                "File loaded", //
                Object.class, //
                RunnerParameter.Level.REQUIRED, //
                "Name of the variable to save the file loaded.Content depend of the storage definition"));
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(
                CmisError.UNKNOWN_TYPE, CmisError.UNKNOWN_TYPE_EXPLANATION,
                CmisError.NOT_A_FOLDER, CmisError.NOT_A_FOLDER_EXPLANATION,
                CmisError.BAD_EXPRESSION, CmisError.BAD_EXPRESSION_EXPLANATION,
                CmisError.TOO_MANY_OBJECTS, CmisError.TOO_MANY_OBJECTS_EXPLANATION,
                CmisError.NOT_A_DOCUMENT, CmisError.NOT_A_DOCUMENT_EXPLANATION,
                CmisError.ERROR_DURING_READ, CmisError.ERROR_DURING_READ_EXPLANATION,
                CmisError.FOLDER_NOT_EXIST, CmisError.FOLDER_NOT_EXIST_EXPLANATION,
                CmisError.DOCUMENT_NOT_EXIST, CmisError.DOCUMENT_NOT_EXIST_EXPLANATION

        );

    }

    @Override
    public String getSubFunctionName() {
        return "DownloadDocument";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Download a file from the CMIS folder and store it on a FileStorage. See the FileStorage library. The FileStorage maybe the Camunda file storage.";
    }

    @Override
    public String getSubFunctionType() {
        return TYPE_CMIS_DOWNLOAD_DOCUMENT;
    }
}
