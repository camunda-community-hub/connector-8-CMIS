/* ******************************************************************** */
/*  UploadDocumentWorker                                                */
/*                                                                      */
/*  Upload a document. It may replace an existing document or create    */
/* a version                                                            */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.connector.cmis.upload;

import io.camunda.connector.cmis.CmisCherryToolbox;
import io.camunda.cherry.definition.AbstractConnector;
import io.camunda.cherry.definition.BpmnError;
import io.camunda.cherry.definition.RunnerParameter;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UploadDocumentFacade extends AbstractConnector {

  public UploadDocumentFacade() {
    super(UploadDocumentFunction.TYPE_CMIS_UPLOADDOCUMENT,

        Arrays.asList(CmisCherryToolbox.CmisConnectionParameter,
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_SOURCE_FILE, "Source file", Object.class,
                RunnerParameter.Level.REQUIRED, "FileVariable uploaded in CMIS"),
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_CMIS_ABSOLUTE_FOLDER_PATH_NAME, "Parent Folder Path",
                String.class, RunnerParameter.Level.REQUIRED, "Folder path where document will be uploaded"),
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_DOCUMENT_NAME, "Document name", String.class,
                RunnerParameter.Level.OPTIONAL, "Document name. If not provided, the file name is used"),
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_DESCRIPTION, "Description", String.class,
                RunnerParameter.Level.OPTIONAL, "Description attached to the new document"),
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_CMIS_TYPE, "Cmis Type", String.class,
                RunnerParameter.Level.REQUIRED, "Cmis Type to the document")
                .setDefaultValue("cmis:document"),
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_IMPORT_POLICY, "Import policy", String.class,
                    RunnerParameter.Level.REQUIRED, "How to import the document")
                .addChoice(UploadDocumentInput.INPUT_IMPORT_POLICY_V_NEW_DOCUMENT, "Document (no version). Overwrite existing")
                .addChoice(UploadDocumentInput.INPUT_IMPORT_POLICY_V_NEW_VERSION, "New version"),
            RunnerParameter.getInstance(UploadDocumentInput.INPUT_VERSION_LABEL, "Version label", String.class,
                    RunnerParameter.Level.OPTIONAL, "label attached to the version")
                .addCondition(UploadDocumentInput.INPUT_IMPORT_POLICY,
                    List.of(UploadDocumentInput.INPUT_IMPORT_POLICY_V_NEW_VERSION))

        ),

        UploadDocumentInput.class,

        Arrays.asList(
            RunnerParameter.getInstance(UploadDocumentOutput.OUTPUT_CMIS_DOCUMENT, "CMIS Document", Object.class,
                RunnerParameter.Level.REQUIRED, "CMIS document")),

        UploadDocumentOutput.class,

        Arrays.asList(CmisCherryToolbox.NO_CONNECTION_TO_CMIS,
            BpmnError.getInstance(UploadDocumentFunction.ERROR_INVALID_PARENT,
                "Invalid parent - absolute path must target an existing folder"),
            BpmnError.getInstance(UploadDocumentFunction.LOAD_FILE_ERROR, "Load file error"),
            BpmnError.getInstance(UploadDocumentFunction.UPLOAD_TO_CMIS_ERROR, "Upload in the CMIS repository failed"),
            BpmnError.getInstance(UploadDocumentFunction.CANT_CHECKOUT_TO_CREATE_VERSION, "Can't create version"),
            BpmnError.getInstance(UploadDocumentFunction.CMIS_CONSTRAINT_EXCEPTION, "CMIS Constraint exception operation can't be executed"),
            BpmnError.getInstance(UploadDocumentFunction.CMISTYPE_NOT_VERSIONABLE, "CMIS Type os not versionable, the policy NEW_VERSION can't be used"))
    );
  }

  @Override
  public String getName() {
    return UploadDocumentFunction.NAME_CMIS_UPLOADDOCUMENT;
  }

  @Override
  public String getDescription() {
    return "Upload a new document, or a new version of a document";
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
   * @param context context of execution
   */
  @Override
  public UploadDocumentOutput execute(OutboundConnectorContext context) throws Exception {

    UploadDocumentFunction uploadDocumentFunction = new UploadDocumentFunction();
    try {
      return uploadDocumentFunction.execute(context);
    } catch (ConnectorException e) {
      throw new ZeebeBpmnError(e.getErrorCode(), e.getMessage());
    }
  }

}
