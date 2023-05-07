/* ******************************************************************** */
/*  Download document                                                                   */
/*                                                                      */
/*  Download a document                                                 */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.cherry.cmis.download;

import io.camunda.cherry.cmis.CmisCherryToolbox;
import io.camunda.cherry.definition.AbstractConnector;
import io.camunda.cherry.definition.BpmnError;
import io.camunda.cherry.definition.RunnerParameter;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.filestorage.StorageDefinition;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DownloadDocumentFacade extends AbstractConnector {
  public static final String INPUT_SOURCE_DOCUMENT = "sourceDocument";
  public static final String INPUT_SOURCE_DOCUMENT_V_ID = "documentId";
  public static final String INPUT_SOURCE_DOCUMENT_V_NAME = "documentName";

  public static final String INPUT_DOCUMENT_ID = "documentId";
  public static final String INPUT_DOCUMENT_NAME = "documentName";
  public static final String INPUT_FOLDER_NAME = "folderName";

  public DownloadDocumentFacade() {
    super(DownloadDocumentFunction.TYPE_CMIS_DOWNLOAD_DOCUMENT, new ArrayList<RunnerParameter>() {{
          addAll(Arrays.asList(CmisCherryToolbox.CmisConnectionParameter));
          addAll(CmisCherryToolbox.CmisListIdentifyObject);
          addAll(List.of(RunnerParameter.getInstance(DownloadDocumentInput.INPUT_STORAGE_DEFINITION,
              "Storage definition", String.class, StorageDefinition.StorageDefinitionType.JSON.toString(),
                      RunnerParameter.Level.OPTIONAL,
                      "How to saved the FileVariable. "
                          + StorageDefinition.StorageDefinitionType.JSON + " to save in the engine (size is linited), "
                          + StorageDefinition.StorageDefinitionType.TEMPFOLDER + " to use the temporary folder of THIS machine"
                          + StorageDefinition.StorageDefinitionType.FOLDER + " to specify a folder to save it (to be accessible by multiple machine if you ruin it in a cluster"
                          + StorageDefinition.StorageDefinitionType.CMIS + " to specify a CMIS connection"
                  )
                  .addChoice("JSON", StorageDefinition.StorageDefinitionType.JSON.toString())
                  .addChoice("TEMPFOLDER", StorageDefinition.StorageDefinitionType.TEMPFOLDER.toString())
                  .addChoice("FOLDER", StorageDefinition.StorageDefinitionType.FOLDER.toString())
                  .addChoice("CMIS", StorageDefinition.StorageDefinitionType.CMIS.toString())
                  .setVisibleInTemplate()
                  .setDefaultValue(StorageDefinition.StorageDefinitionType.JSON.toString()))
              );
        }},

        DownloadDocumentInput.class,

        List.of(
            RunnerParameter.getInstance(DownloadDocumentOutput.OUTPUT_DOCUMENT_FILE, "Document downloaded", List.class,
                RunnerParameter.Level.REQUIRED, "Document uploaded").setVisibleInTemplate()),

        DownloadDocumentOutput.class,

        Arrays.asList(CmisCherryToolbox.NO_CONNECTION_TO_CMIS,
            BpmnError.getInstance(DownloadDocumentFunction.ERROR_DURING_READ, "Error during reading of document"),
            BpmnError.getInstance(DownloadDocumentFunction.ERROR_NOT_A_DOCUMENT,
                "Only Document contains content with CMIS"),
            BpmnError.getInstance(DownloadDocumentFunction.ERROR_TOO_MANY_OBJECTS,
                "Search must return one and only one object"))

    );
  }

  @Override
  public String getName() {
    return "CMIS: Download";
  }

  @Override
  public String getDescription() {
    return "Download a document in the process, or directly on the local file system";
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
   *
   */
  @Override
  public DownloadDocumentOutput execute(OutboundConnectorContext context) throws Exception {
    DownloadDocumentFunction downloadDocumentFunction = new DownloadDocumentFunction();
    try {
      return downloadDocumentFunction.execute(context);
    } catch (ConnectorException e) {
      throw new ZeebeBpmnError(e.getErrorCode(), e.getMessage());
    }

  }
}
