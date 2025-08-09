package io.camunda.connector.cmis.download;

import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.sourceobject.CmisSourceAccess;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DownloadDocumentFunction  implements CmisSubFunction {

  public static final String NAME_CMIS_DOWNLOAD_DOCUMENT = "CMIS: Download document";
  public static final String TYPE_CMIS_DOWNLOAD_DOCUMENT = "c-cmis-downloaddocument";


  @Override
  public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                       CmisInput cmisInput,
                                       OutboundConnectorContext context) throws ConnectorException {


    CmisOutput cmisOutput = new CmisOutput();
    List<CmisObject> listCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, cmisInput);
    if (listCmisObject.size() > 1) {
      throw new ConnectorException(CmisError.ERROR_TOO_MANY_OBJECTS, "The selection must return one and only one document");
    }
    DownloadDocumentOutput downloadDocumentOutput = new DownloadDocumentOutput();
    if (listCmisObject.isEmpty()) {
      return cmisOutput;

    }

    CmisObject cmisObject = listCmisObject.get(0);
    if (!(cmisObject instanceof Document)) {
      throw new ConnectorException(CmisError.ERROR_NOT_A_DOCUMENT, "The selection must return a Document, which contains a content");
    }
    Document cmisDocument = (Document) cmisObject;
    ContentStream contentStream = cmisDocument.getContentStream();
    FileVariable fileVariableOutput = cmisInput.initializeOutputFileVariable(cmisDocument.getName());
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int nRead;
      byte[] data = new byte[4000];

      while ((nRead = contentStream.getStream().read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }

      buffer.flush();
      fileVariableOutput.setValue(buffer.toByteArray());

      FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
      FileVariableReference fileVariableOutputReference = fileRepoFactory.saveFileVariable(fileVariableOutput, context);
      downloadDocumentOutput.documentFile = fileVariableOutputReference.toJson();

    } catch (Exception e) {
      throw new ConnectorException(CmisError.ERROR_DURING_READ, "Error during download the document " + e.getMessage());
    }
    return cmisOutput;
  }

  @Override
    public List<RunnerParameter> getInputsParameter() {
    return Collections.emptyList();
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Collections.emptyList();
  }

  @Override
  public Map<String, String> getBpmnErrors() {
    return Map.of();
  }

  @Override
  public String getSubFunctionName() {
    return "DownloadFile";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Download a file from the CMIS folder and store it on a FileStorage";
  }

  @Override
  public String getSubFunctionType() {
    return "download-file";
  }
}
