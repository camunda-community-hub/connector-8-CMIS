package io.camunda.connector.cmis.upload;

import io.camunda.connector.cmis.CmisCherryToolbox;
import io.camunda.connector.cmis.CmisToolbox;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisFactoryConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@OutboundConnector(name = UploadDocumentFunction.NAME_CMIS_UPLOADDOCUMENT, inputVariables = {
    UploadDocumentInput.INPUT_CMIS_CONNECTION, UploadDocumentInput.INPUT_DOCUMENT_NAME,
    UploadDocumentInput.INPUT_SOURCE_FILE, UploadDocumentInput.INPUT_CMIS_ABSOLUTE_FOLDER_PATH_NAME,
    UploadDocumentInput.INPUT_DOCUMENT_NAME, UploadDocumentInput.INPUT_DESCRIPTION,
    UploadDocumentInput.INPUT_IMPORT_POLICY }, type = UploadDocumentFunction.TYPE_CMIS_UPLOADDOCUMENT)

public class UploadDocumentFunction implements CmisSubFunction {

  public static final String ERROR_INVALID_PARENT = "INVALID_PARENT";
  public static final String LOAD_FILE_ERROR = "LOAD_FILE_ERROR";
  public static final String UPLOAD_TO_CMIS_ERROR = "UPLOAD_TO_CMIS_ERROR";
  public static final String CMIS_CONSTRAINT_EXCEPTION = "CMIS_CONSTRAINT_EXCEPTION";
  public static final String CANT_CHECKOUT_TO_CREATE_VERSION = "CANT_CHECKOUT_TO_CREATE_VERSION";
  public static final String CMISTYPE_NOT_VERSIONABLE = "CMISTYPE_NOT_VERSIONABLE";

  public static final String TYPE_CMIS_UPLOADDOCUMENT = "c-cmis-uploaddocument";
  public static final String NAME_CMIS_UPLOADDOCUMENT = "CMIS: Upload document";

  public UploadDocumentOutput execute(OutboundConnectorContext context) throws Exception {
    UploadDocumentInput uploadDocumentInput = context.getVariablesAsType(UploadDocumentInput.class);

    CmisConnection cmisConnection;
    try {
      CmisParameters cmisParameters = CmisParameters.getCodingConnection(
          CmisToolbox.getCmisConnection(uploadDocumentInput.getCmisConnection()));

      cmisConnection = CmisFactoryConnection.getInstance().getCmisConnection(cmisParameters);
    } catch (Exception e) {
      throw new ZeebeBpmnError(CmisCherryToolbox.NO_CONNECTION_TO_CMIS.getCode(), "No connection");
    }
    try {
      FileVariableReference fileVariableReference = FileVariableReference.fromJson(uploadDocumentInput.getSourceFile());

      if (fileVariableReference == null) {
        throw new ConnectorException(LOAD_FILE_ERROR, "source file is not a file reference");
      }
      FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
      FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference);

      if (fileVariable == null) {
        throw new ConnectorException(LOAD_FILE_ERROR, "Can't load reference[" + fileVariableReference.toString());
      }
      Folder parentFolder = null;
      try {
        parentFolder = cmisConnection.getFolderByPath(uploadDocumentInput.getCmisAbsoluteFolderPathName());
      } catch (Exception e) {
        // is managed after
      }
      if (parentFolder == null)
        throw new ConnectorException(ERROR_INVALID_PARENT,
            "Can't find [" + uploadDocumentInput.getCmisAbsoluteFolderPathName() + "]");

      String documentName = uploadDocumentInput.getDocumentName();
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
      properties.put("cmis:objectTypeId", uploadDocumentInput.getCmisType());

      ObjectType cmisType = cmisConnection.getSession().getTypeDefinition(uploadDocumentInput.getCmisType());
      boolean typeIsVersionable = false;
      if (cmisType instanceof DocumentType) {
        typeIsVersionable = Boolean.TRUE.equals(((DocumentType) cmisType).isVersionable());
      }
      // ------------- Import a new Document
      if (UploadDocumentInput.INPUT_IMPORT_POLICY_V_NEW_DOCUMENT.equals(uploadDocumentInput.getImportPolicy())) {

        if (existingDocument != null) {
          existingDocument.setContentStream(contentStream, true);
        } else {
          existingDocument = parentFolder.createDocument(properties, contentStream,
              typeIsVersionable ? VersioningState.MAJOR : VersioningState.NONE);
        }

        // ------------- Import a new version
      } else if (UploadDocumentInput.INPUT_IMPORT_POLICY_V_NEW_VERSION.equals(uploadDocumentInput.getImportPolicy())) {
if (! typeIsVersionable) {
  throw new ConnectorException(CMISTYPE_NOT_VERSIONABLE,
      "The cmisType[" + uploadDocumentInput.getCmisType() + "] is not versionable, you can't use this policy");
}
        if (existingDocument == null) {
          existingDocument = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

        } else {
          boolean isCheckedOut = Boolean.TRUE.equals(existingDocument.isVersionSeriesCheckedOut());
          if (isCheckedOut) {
            throw new ConnectorException(CANT_CHECKOUT_TO_CREATE_VERSION, "The document is already checkout.");
          }
          ObjectId pwcId = existingDocument.checkOut();
          Document pwc = (Document) cmisConnection.getSession().getObject(pwcId); // get PWC document
          ObjectId newVersionId = pwc.checkIn(true, null, contentStream,
              uploadDocumentInput.getVersionLabel() == null ? "new version" : uploadDocumentInput.getVersionLabel());
          existingDocument = (Document) cmisConnection.getSession().getObject(newVersionId);
        }

      }
      UploadDocumentOutput uploadDocumentOutput = new UploadDocumentOutput();
      if (existingDocument != null)
        uploadDocumentOutput.documentId = existingDocument.getId();
      return uploadDocumentOutput;
    } catch (CmisConstraintException ce) {
      throw new ConnectorException(CMIS_CONSTRAINT_EXCEPTION,
          "Can't upload the document due to a constraint " + ce.getCode() + " " + ce.getMessage());
    } catch (Exception e) {
      throw new ConnectorException(UPLOAD_TO_CMIS_ERROR, "Can't upload the content file");
    }
  } // end function

}