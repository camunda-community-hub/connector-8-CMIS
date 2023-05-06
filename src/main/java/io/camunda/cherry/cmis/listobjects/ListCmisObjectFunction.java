package io.camunda.cherry.cmis.listobjects;

import io.camunda.cherry.cmis.CmisCherryToolbox;
import io.camunda.cherry.cmis.CmisToolbox;
import io.camunda.cherry.cmis.sourceobject.CmisSourceAccess;
import io.camunda.cherry.cmis.sourceobject.CmisSourceObjectInt;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisFactoryConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;

import java.util.List;

@OutboundConnector(name = ListCmisObjectFunction.NAME_CMIS_LISTOBJECTS, inputVariables = {
    ListCmisObjectInput.INPUT_CMIS_CONNECTION, CmisSourceObjectInt.INPUT_SOURCE_OBJECT,
    CmisSourceObjectInt.INPUT_CMIS_OBJECTID, CmisSourceObjectInt.INPUT_CMIS_ABSOLUTE_PATH_NAME,
    CmisSourceObjectInt.INPUT_FILTER, }, type = ListCmisObjectFunction.TYPE_CMIS_LISTOBJECTS)

public class ListCmisObjectFunction {

  public static final String TYPE_CMIS_LISTOBJECTS = "c-cmis-listobjects";
  public static final String NAME_CMIS_LISTOBJECTS = "CMIS: List objects";

  public ListCmisObjectOutput execute(OutboundConnectorContext context) throws Exception {
    ListCmisObjectInput cmisInput = context.getVariablesAsType(ListCmisObjectInput.class);

    CmisConnection cmisConnection;
    try {
      CmisParameters cmisParameters = CmisParameters.getCodingConnection(
          CmisToolbox.getCmisConnection(cmisInput.cmisConnection));

      cmisConnection = CmisFactoryConnection.getInstance().getCmisConnection(cmisParameters);
    } catch (Exception e) {
      throw new ConnectorException(CmisCherryToolbox.NO_CONNECTION_TO_CMIS.getCode(), "No connection");
    }

    ListCmisObjectOutput listCmisObjectOutput = new ListCmisObjectOutput();

    List<CmisObject> listOfCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, cmisInput);
    if (listOfCmisObject == null)
      return listCmisObjectOutput;

    for (CmisObject cmisObject : listOfCmisObject) {
      listCmisObjectOutput.listCmisObject.add(createDescription(cmisObject));
    }

    return listCmisObjectOutput;
  }

  /**
   * Create a description from a CmisObject
   *
   * @param cmisObject the source object
   * @return the description
   */
  private ListCmisObjectOutput.CmisObjectDescription createDescription(CmisObject cmisObject) {
    ListCmisObjectOutput.CmisObjectDescription description = new ListCmisObjectOutput.CmisObjectDescription();

    description.id = cmisObject.getId();
    description.name = cmisObject.getName();
    description.description = cmisObject.getDescription();
    description.isFolder = cmisObject instanceof Folder;
    return description;
  }
}
