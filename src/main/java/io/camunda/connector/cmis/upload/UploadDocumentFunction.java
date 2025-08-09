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

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UploadDocumentFunction implements CmisSubFunction {


    public static final String TYPE_CMIS_UPLOADDOCUMENT = "c-cmis-uploaddocument";
    public static final String NAME_CMIS_UPLOADDOCUMENT = "CMIS: Upload document";

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {

        try {
            FileVariableReference fileVariableReference = FileVariableReference.fromInput(cmisInput.getInputFileStorage());

            if (fileVariableReference == null) {
                throw new ConnectorException(CmisError.LOAD_FILE_ERROR, "source file is not a file reference");
            }
            FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
            FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference, context);

            if (fileVariable == null) {
                throw new ConnectorException(CmisError.LOAD_FILE_ERROR, "Can't load reference[" + fileVariableReference.toString());
            }
            Folder parentFolder = null;
            try {
                parentFolder = cmisConnection.getFolderByPath(cmisInput.getAbsoluteFolderName());
            } catch (Exception e) {
                // is managed after
            }
            if (parentFolder == null)
                throw new ConnectorException(CmisError.ERROR_INVALID_PARENT,
                        "Can't find [" + cmisInput.getAbsoluteFolderName() + "]");

            String documentName = cmisInput.getDocumentName();
            if (documentName == null || documentName.isEmpty()) {
                documentName = fileVariable.getName();
            }
            ByteArrayInputStream inputStreamFile = new ByteArrayInputStream(fileVariable.getValue());


            ContentStreamImpl contentStream = new ContentStreamImpl(documentName,
                    BigInteger.valueOf(fileVariable.getValue().length), fileVariable.getMimeType(), inputStreamFile);

            Document existingDocument;

            try {
                existingDocument = (Document) cmisConnection.getSession()
                        .getObjectByPath(parentFolder.getPath() + "/" + documentName);
            } catch (Exception var11) {
                existingDocument = null;
            }
            Map<String, Object> properties = new HashMap();
            properties.put("cmis:name", documentName);
            properties.put("cmis:objectTypeId", cmisInput.getCmisType());

            ObjectType cmisType = cmisConnection.getSession().getTypeDefinition(cmisInput.getCmisType());
            boolean typeIsVersionable = false;
            if (cmisType instanceof DocumentType) {
                typeIsVersionable = Boolean.TRUE.equals(((DocumentType) cmisType).isVersionable());
            }
            // ------------- Import a new Document
            if (CmisInput.INPUT_IMPORT_POLICY_V_NEW_DOCUMENT.equals(cmisInput.getImportPolicy())) {

                if (existingDocument != null) {
                    existingDocument.setContentStream(contentStream, true);
                } else {
                    existingDocument = parentFolder.createDocument(properties, contentStream,
                            typeIsVersionable ? VersioningState.MAJOR : VersioningState.NONE);
                }

                // ------------- Import a new version
            } else if (CmisInput.INPUT_IMPORT_POLICY_V_NEW_VERSION.equals(cmisInput.getImportPolicy())) {
                if (!typeIsVersionable) {
                    throw new ConnectorException(CmisError.CMISTYPE_NOT_VERSIONABLE,
                            "The cmisType[" + cmisInput.getCmisType() + "] is not versionable, you can't use this policy");
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
            return cmisOutput;
        } catch (CmisConstraintException ce) {
            throw new ConnectorException(CmisError.CMIS_CONSTRAINT_EXCEPTION,
                    "Can't upload the document due to a constraint " + ce.getCode() + " " + ce.getMessage());
        } catch (Exception e) {
            throw new ConnectorException(CmisError.UPLOAD_TO_CMIS_ERROR, "Can't upload the content file");
        }
    } // end function

    @Override
    public List<RunnerParameter> getInputsParameter() {
        //  public static final String INPUT_FILE_STORAGE = "inputFileStorage";
        //
        //  public static final String ABSOLUTE_FOLDER_NAME = "absoluteFolderName";
        //
        //  public static final String DOCUMENT_NAME = "documentName";
        //
        //  public static final String CMIS_TYPE = "cmisType";
        //
        //  public static final String INPUT_IMPORT_POLICY = "importPolicy";
        //  public static final String INPUT_IMPORT_POLICY_V_NEW_DOCUMENT = "NEW_DOCUMENT";
        //  public static final String INPUT_IMPORT_POLICY_V_NEW_VERSION = "NEW_VERSION";
        //
        //  public static final String INPUT_VERSION_LABEL = "versionLabel";
        //
        return Collections.emptyList();
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        // OUTPUT_CMIS_DOCUMENTID
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(CmisError.LOAD_FILE_ERROR, CmisError.LOAD_FILE_ERROR_EXPLANATION,
                CmisError.ERROR_INVALID_PARENT, CmisError.ERROR_INVALID_PARENT_EXPLANATION,
                CmisError.CMISTYPE_NOT_VERSIONABLE, CmisError.CMISTYPE_NOT_VERSIONABLE_EXPLANATION,
                CmisError.CANT_CHECKOUT_TO_CREATE_VERSION, CmisError.CANT_CHECKOUT_TO_CREATE_VERSION_EXPLANATION,
                CmisError.CMIS_CONSTRAINT_EXCEPTION, CmisError.CMIS_CONSTRAINT_EXCEPTION_EXPLANATION,
                CmisError.UPLOAD_TO_CMIS_ERROR, CmisError.UPLOAD_TO_CMIS_ERROR_EXPLANATION
        );
    }

    @Override
    public String getSubFunctionName() {
        return "UploadFile";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Upload a file to CMIS";
    }

    @Override
    public String getSubFunctionType() {
        return "upload-document";
    }
}