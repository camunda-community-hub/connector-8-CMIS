package io.camunda.connector.cmis.download;

import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisCherryToolbox;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.CmisToolbox;
import io.camunda.connector.cmis.listobjects.ListCmisObjectInput;
import io.camunda.connector.cmis.sourceobject.CmisSourceAccess;
import io.camunda.connector.cmis.sourceobject.CmisSourceObjectInt;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisFactoryConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@OutboundConnector(name = DownloadDocumentFunction.NAME_CMIS_DOWNLOAD_DOCUMENT, inputVariables = {
    ListCmisObjectInput.INPUT_CMIS_CONNECTION, CmisSourceObjectInt.INPUT_SOURCE_OBJECT,
    CmisSourceObjectInt.INPUT_CMIS_OBJECTID, CmisSourceObjectInt.INPUT_CMIS_ABSOLUTE_PATH_NAME,
    CmisSourceObjectInt.INPUT_FILTER, }, type = DownloadDocumentFunction.TYPE_CMIS_DOWNLOAD_DOCUMENT)
public class DownloadDocumentFunction  implements CmisSubFunction {

  public final static String NAME_CMIS_DOWNLOAD_DOCUMENT = "CMIS: Download document";
  public final static String TYPE_CMIS_DOWNLOAD_DOCUMENT = "c-cmis-downloaddocument";

  public static final String ERROR_DURING_READ = "ERROR_DURING_READ";
  public static final String ERROR_NOT_A_DOCUMENT = "NOT_A_DOCUMENT";
  public static final String ERROR_TOO_MANY_OBJECTS = "TOO_MANY_OBJECTS";

  public DownloadDocumentOutput execute(OutboundConnectorContext context) throws Exception {
    DownloadDocumentInput downloadDocumentInput = context.getVariablesAsType(DownloadDocumentInput.class);

    CmisConnection cmisConnection;
    try {
      CmisParameters cmisParameters = CmisParameters.getCodingConnection(
          CmisToolbox.getCmisConnection(downloadDocumentInput.cmisConnection));

      cmisConnection = CmisFactoryConnection.getInstance().getCmisConnection(cmisParameters);
    } catch (Exception e) {
      throw new ConnectorException(CmisCherryToolbox.NO_CONNECTION_TO_CMIS.getCode(), "No connection");
    }

    List<CmisObject> listCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, downloadDocumentInput);
    if (listCmisObject.size() > 1) {
      throw new ConnectorException(ERROR_TOO_MANY_OBJECTS, "The selection must return one and only one document");
    }
    DownloadDocumentOutput downloadDocumentOutput = new DownloadDocumentOutput();
    if (listCmisObject.isEmpty()) {
      return downloadDocumentOutput;

    }

    CmisObject cmisObject = listCmisObject.get(0);
    if (!(cmisObject instanceof Document)) {
      throw new ConnectorException(ERROR_NOT_A_DOCUMENT, "The selection must return a Document, which contains a content");
    }
    Document cmisDocument = (Document) cmisObject;
    ContentStream contentStream = cmisDocument.getContentStream();
    StorageDefinition storageDefinition = StorageDefinition.getFromString(downloadDocumentInput.getStorageDefinition());

    FileVariable fileVariable = new FileVariable();
    fileVariable.setName(cmisDocument.getName());
    fileVariable.setMimeType(contentStream.getMimeType());
    fileVariable.setStorageDefinition(storageDefinition);

    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int nRead;
      byte[] data = new byte[4000];

      while ((nRead = contentStream.getStream().read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }

      buffer.flush();
      fileVariable.setValue(buffer.toByteArray());

      FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
      FileVariableReference fileVariableReference = fileRepoFactory.saveFileVariable(fileVariable);
      downloadDocumentOutput.documentFile = fileVariableReference.toJson();

    } catch (Exception e) {
      throw new ConnectorException(ERROR_DURING_READ, "Error during download the document " + e.getMessage());
    }
    return downloadDocumentOutput;
  }

  @Override
  public CmisOutput executeSubFunction(CmisConnection cmisConnection, CmisInput pdfInput, OutboundConnectorContext context) throws ConnectorException {
    return null;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return List.of();
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return List.of();
  }

  @Override
  public Map<String, String> getBpmnErrors() {
    return Map.of();
  }

  @Override
  public String getSubFunctionName() {
    return "";
  }

  @Override
  public String getSubFunctionDescription() {
    return "";
  }

  @Override
  public String getSubFunctionType() {
    return "";
  }
}
