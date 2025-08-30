package io.camunda.connector.cmis.listobjects;

import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.CmisOutput;
import io.camunda.connector.cmis.sourceobject.CmisSourceAccess;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ListCmisObjectFunction  implements CmisSubFunction  {

  public static final String TYPE_CMIS_LISTOBJECTS = "list-objects";
  public static final String NAME_CMIS_LISTOBJECTS = "CMIS: List objects";

  @Override
  public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                       CmisInput cmisInput,
                                       OutboundConnectorContext context) throws ConnectorException {

  CmisOutput cmisOutput = new CmisOutput();

    List<CmisObject> listOfCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, cmisInput);
    if (listOfCmisObject == null)
      return cmisOutput;

    for (CmisObject cmisObject : listOfCmisObject) {
      cmisOutput.listCmisObject.add(createDescription(cmisObject));
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
    return "ListObjects";
  }

  @Override
  public String getSubFunctionDescription() {
    return "List all objects found in a folder";
  }

  @Override
  public String getSubFunctionType() {
    return "list-objects";
  }
  /**
   * Create a description from a CmisObject
   *
   * @param cmisObject the source object
   * @return the description
   */
  private CmisOutput.CmisObjectDescription createDescription(CmisObject cmisObject) {
    CmisOutput.CmisObjectDescription description = new CmisOutput.CmisObjectDescription();

    description.id = cmisObject.getId();
    description.name = cmisObject.getName();
    description.description = cmisObject.getDescription();
    description.isFolder = cmisObject instanceof Folder;
    return description;
  }
}
