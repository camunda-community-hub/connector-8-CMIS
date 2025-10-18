/* ******************************************************************** */
/*   DeleteDocumentFunction                                                                   */
/*                                                                      */
/*  Delete a document, or a version in a document                                                   */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.connector.cmis.deletedocument;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.sourceobject.CmisSourceAccess;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.connector.cmis.toolbox.CmisToolbox;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeleteDocumentFunction implements CmisSubFunction {
    private final Logger logger = LoggerFactory.getLogger(DeleteDocumentFunction.class.getName());

    public DeleteDocumentFunction() {
        // No special to add here
    }

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {
        CmisOutput cmisOutput = new CmisOutput();
        List<CmisObject> listCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, cmisInput);
        Boolean errorIfNotExist = cmisInput.getErrorIfNotExist();

        StringBuilder documentDeletedToLog = new StringBuilder();
        // First, check that all objects get a path
        for (CmisObject cmisObject : listCmisObject) {
            boolean deletionOk = cmisConnection.deleteObjectById(cmisObject.getId());
            documentDeletedToLog.append(cmisObject.getId()).append(": Deletion? ").append(deletionOk).append(",");
            if (!deletionOk && errorIfNotExist) {
                throw new ConnectorException(CmisError.DOCUMENT_NOT_EXIST,
                        "Document with id[" + cmisObject.getId() + "] does not exist");

            }
            if (deletionOk) {
                cmisOutput.listCmisObject.add(CmisToolbox.createDescription(cmisObject));
            }
        }
        logger.info("Documents deleted [{}]", documentDeletedToLog);
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

                RunnerParameter.getInstance(CmisInput.ERROR_IF_NOT_EXIST, "Error if not exist", Boolean.class,
                                RunnerParameter.Level.OPTIONAL, "Throw a BPMN Error if the object does not exist") //
                        .setDefaultValue(Boolean.FALSE) //
                        .setVisibleInTemplate());

    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(
                RunnerParameter.getInstance(CmisOutput.LIST_CMIS_OBJECT, "List CMIS Object deleted", List.class,
                        RunnerParameter.Level.REQUIRED,
                        "List of ID of CMIS Object deleted"));
    }

    public Map<String, String> getBpmnErrors() {
        return Map.of(CmisError.DOCUMENT_NOT_EXIST, CmisError.DOCUMENT_NOT_EXIST_EXPLANATION,
                CmisError.BAD_EXPRESSION, CmisError.BAD_EXPRESSION_EXPLANATION,
                CmisError.NOT_A_FOLDER, CmisError.NOT_A_FOLDER_EXPLANATION,
                CmisError.UNKNOWN_TYPE, CmisError.UNKNOWN_TYPE_EXPLANATION,
                CmisError.FOLDER_NOT_EXIST, CmisError.FOLDER_NOT_EXIST_EXPLANATION);
    }

    @Override
    public String getSubFunctionName() {
        return "DeleteDocument";
    }


    @Override
    public String getSubFunctionDescription() {
        return "Delete a document. A Cmis ObjectID is provided and will be deleted.";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-document";
    }

}
