/* ******************************************************************** */
/*                                                                      */
/*  CreateFolderWorker                                                   */
/*                                                                      */
/*  Create a folder in CMIS      */
/* ******************************************************************** */
package io.camunda.connector.cmis.checkconnection;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CheckConnectionFunction implements CmisSubFunction {

    public CheckConnectionFunction() {
        // No special to add here
    }

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {


        try {
            CmisObject object = cmisConnection.getObjectByPath("/");
            if (object == null)
                throw new ConnectorException(CmisError.NO_CONNECTION_TO_CMIS);
        } catch (Exception e) {
            throw new ConnectorException(CmisError.NO_CONNECTION_TO_CMIS);
        }


        return new CmisOutput();

    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return List.of(
                new RunnerParameter(CmisInput.CMIS_CONNECTION, "CMIS Connection", String.class,
                        RunnerParameter.Level.REQUIRED, "Cmis Connection. JSON like {\"url\":\"http://localhost:8099/cmis/browser\",\"userName\":\"test\",\"password\":\"test\"}")
        );
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(CmisError.NO_CONNECTION_TO_CMIS, CmisError.NO_CONNECTION_TO_CMIS_EXPLANATION);

    }

    @Override
    public String getSubFunctionName() {
        return "CheckConnection";
    }


    @Override
    public String getSubFunctionDescription() {
        return "Check the connection to CMIS";
    }

    @Override
    public String getSubFunctionType() {
        return "check-connection";
    }

}
