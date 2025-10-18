package io.camunda.connector.cmis.upload;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UploadDocumentFunction implements CmisSubFunction {

    public static final String TYPE_CMIS_UPLOAD_DOCUMENT = "upload-document";
    private final Logger logger = LoggerFactory.getLogger(UploadDocumentFunction.class.getName());

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {

        try {
            FileVariableReference fileVariableReference = FileVariableReference.fromObject(cmisInput.getSourceFile());

            if (fileVariableReference == null) {
                throw new ConnectorException(CmisError.LOAD_FILE_ERROR, "source file is not a file reference");
            }
            FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
            FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference, context);

            if (fileVariable == null) {
                throw new ConnectorException(CmisError.LOAD_FILE_ERROR, "Can't load reference[" + fileVariableReference);
            }
            Folder parentFolder = null;
            try {
                parentFolder = cmisConnection.getFolderByPath(cmisInput.getUploadFolderName());
            } catch (Exception e) {
                // is managed after
            }
            if (parentFolder == null)
                throw new ConnectorException(CmisError.INVALID_PARENT,
                        "Can't find [" + cmisInput.getAbsoluteFolderName() + "]");

            String documentName = cmisInput.getDocumentName();
            if (documentName == null || documentName.isEmpty()) {
                documentName = fileVariable.getName();
            }

            // We must keep it as a ByteArray: we need the length
            ByteArrayInputStream inputStreamFile = new ByteArrayInputStream(fileVariable.getValue());


            ContentStreamImpl contentStream = new ContentStreamImpl(documentName,
                    BigInteger.valueOf(fileVariable.getValue().length), fileVariable.getMimeType(), inputStreamFile);

            Document existingDocument;

            try {
                existingDocument = (Document) cmisConnection.getSession()
                        .getObjectByPath(parentFolder.getPath() + "/" + documentName);
            } catch (Exception var11) {
                logger.debug("Error getting document Path[{}] Document[{}]", parentFolder.getPath(), documentName);
                existingDocument = null;
            }
            Map<String, Object> properties = new HashMap();
            properties.put("cmis:name", documentName);
            properties.put("cmis:objectTypeId", cmisInput.getCmisType(CmisInput.CMIS_TYPE_V_CMIS_DOCUMENT));

            ObjectType cmisType = cmisConnection.getSession().getTypeDefinition(cmisInput.getCmisType(CmisInput.CMIS_TYPE_V_CMIS_DOCUMENT));
            boolean typeIsVersionable = false;
            if (cmisType instanceof DocumentType) {
                typeIsVersionable = Boolean.TRUE.equals(((DocumentType) cmisType).isVersionable());
            }
            // ------------- Import a new Document
            if (CmisInput.IMPORT_POLICY_V_NEW_DOCUMENT.equals(cmisInput.getImportPolicy())) {

                if (existingDocument != null) {
                    existingDocument.setContentStream(contentStream, true);
                } else {
                    existingDocument = parentFolder.createDocument(properties, contentStream,
                            typeIsVersionable ? VersioningState.MAJOR : VersioningState.NONE);
                }

                // ------------- Import a new version
            } else if (CmisInput.IMPORT_POLICY_V_NEW_VERSION.equals(cmisInput.getImportPolicy())) {
                if (!typeIsVersionable) {
                    throw new ConnectorException(CmisError.CMISTYPE_NOT_VERSIONABLE,
                            "The cmisType[" + cmisInput.getCmisType(CmisInput.CMIS_TYPE_V_CMIS_DOCUMENT) + "] is not version able, you can't use this policy");
                }
                if (existingDocument == null) {
                    existingDocument = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

                } else {
                    boolean isCheckedOut = Boolean.TRUE.equals(existingDocument.isVersionSeriesCheckedOut());
                    if (isCheckedOut) {
                        throw new ConnectorException(CmisError.CANT_CHECKOUT_TO_CREATE_VERSION, "The document is already checkout.");
                    }
                    ObjectId pwcId = existingDocument.checkOut();
                    Document pwc = (Document) cmisConnection.getSession().getObject(pwcId); // get PWC document
                    ObjectId newVersionId = pwc.checkIn(true, null, contentStream,
                            cmisInput.getVersionLabel() == null ? "new version" : cmisInput.getVersionLabel());
                    existingDocument = (Document) cmisConnection.getSession().getObject(newVersionId);
                }

            }
            CmisOutput cmisOutput = new CmisOutput();
            if (existingDocument != null)
                cmisOutput.documentId = existingDocument.getId();
            logger.info("DocumentUpload [{}]", cmisOutput.documentId);
            return cmisOutput;

        } catch (CmisConstraintException ce) {
            logger.error("DocumentUpload constraint violated", ce);
            throw new ConnectorException(CmisError.CMIS_CONSTRAINT_EXCEPTION,
                    "Can't upload the document due to a constraint " + ce.getCode() + " " + ce.getMessage());
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Upload error", e);
            throw new ConnectorException(CmisError.UPLOAD_TO_CMIS_ERROR, "Can't upload the content file");
        }
    } // end function

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                new RunnerParameter(CmisInput.CMIS_CONNECTION, "CMIS Connection", String.class,
                        RunnerParameter.Level.REQUIRED, "Cmis Connection. JSON like {\"url\":\"http://localhost:8099/cmis/browser\",\"userName\":\"test\",\"password\":\"test\"}"),

                RunnerParameter.getInstance(CmisInput.SOURCE_FILE, "Source file", String.class, // class
                        RunnerParameter.Level.REQUIRED, // level
                        "Source file to upload? This is a FileReference using ZeebeStorage, or directly a Camunda 8 Document"), // explanation

                RunnerParameter.getInstance(CmisInput.UPLOAD_FOLDER_NAME, "Cmis Folder", String.class, // class
                        RunnerParameter.Level.REQUIRED, // level
                        "Folder (absolute path) to load the file"),// explanation
                RunnerParameter.getInstance(CmisInput.DOCUMENT_NAME, "Document name", String.class, // class
                        RunnerParameter.Level.OPTIONAL, // level
                        "CMIS document name. If not provided, file name is used"),// explanation
                RunnerParameter.getInstance(CmisInput.CMIS_TYPE, "CMIS Type", String.class, // class
                        RunnerParameter.Level.OPTIONAL, // level
                        "CMIS type. " + CmisInput.CMIS_TYPE_V_CMIS_DOCUMENT + " if not provide"),// explanation
                RunnerParameter.getInstance(CmisInput.IMPORT_POLICY, "Import policy", String.class, // class
                                RunnerParameter.Level.OPTIONAL, // level
                                "Import Policy. " + CmisInput.IMPORT_POLICY_V_NEW_DOCUMENT + " to create new document, " + CmisInput.IMPORT_POLICY_V_NEW_VERSION + " to create a new version in an existing document")
                        .addChoice(CmisInput.IMPORT_POLICY_V_NEW_DOCUMENT, "New document")
                        .addChoice(CmisInput.IMPORT_POLICY_V_NEW_VERSION, "New version")
                        .setDefaultValue(CmisInput.IMPORT_POLICY_V_NEW_DOCUMENT)
                        .setVisibleInTemplate()
                // explanation
        );

    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(
                RunnerParameter.getInstance(CmisOutput.DOCUMENTID, "Document ID uploaded", String.class,
                        RunnerParameter.Level.REQUIRED,
                        "Dpocument ID uploaded"));
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(CmisError.LOAD_FILE_ERROR, CmisError.LOAD_FILE_ERROR_EXPLANATION,
                CmisError.INVALID_PARENT, CmisError.INVALID_PARENT_EXPLANATION,
                CmisError.CMISTYPE_NOT_VERSIONABLE, CmisError.CMISTYPE_NOT_VERSIONABLE_EXPLANATION,
                CmisError.CANT_CHECKOUT_TO_CREATE_VERSION, CmisError.CANT_CHECKOUT_TO_CREATE_VERSION_EXPLANATION,
                CmisError.CMIS_CONSTRAINT_EXCEPTION, CmisError.CMIS_CONSTRAINT_EXCEPTION_EXPLANATION,
                CmisError.UPLOAD_TO_CMIS_ERROR, CmisError.UPLOAD_TO_CMIS_ERROR_EXPLANATION
        );
    }

    @Override
    public String getSubFunctionName() {
        return "UploadDocument";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Upload a document to CMIS. It use FileStorage library for the sourceFile. It maybe directly a Camunda Document.";
    }

    @Override
    public String getSubFunctionType() {
        return TYPE_CMIS_UPLOAD_DOCUMENT;
    }
}