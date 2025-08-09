package io.camunda.connector.cmis.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.filestorage.cmis.CmisConnection;

import java.util.List;
import java.util.Map;

public interface CmisSubFunction {
  CmisOutput executeSubFunction(CmisConnection cmisConnection, CmisInput pdfInput, OutboundConnectorContext context) throws ConnectorException;


  List<RunnerParameter> getInputsParameter();

  List<RunnerParameter> getOutputsParameter();

  Map<String, String> getBpmnErrors();

  String getSubFunctionName();

  String getSubFunctionDescription();

  String getSubFunctionType();

}
