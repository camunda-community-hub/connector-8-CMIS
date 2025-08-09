/* ******************************************************************** */
/*   DeleteDocumentWorker                                                                   */
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
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.cmis.CmisConnection;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeleteDocumentWorker implements CmisSubFunction {

    public DeleteDocumentWorker() {
    }
    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {
        return new CmisOutput();

    }

    @Override
    public List<RunnerParameter> getInputsParameter() {

        return Collections.emptyList();
    }
    @Override
    public List<RunnerParameter> getOutputsParameter() {

        return Collections.emptyList();
    }
    public Map<String, String> getBpmnErrors() {
        return Collections.emptyMap();

    }

    @Override
    public String getSubFunctionName() {
        return "DeleteDocument";
    }


    @Override
    public String getSubFunctionDescription() {
        return "Delete a document";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-document";
    }

}
