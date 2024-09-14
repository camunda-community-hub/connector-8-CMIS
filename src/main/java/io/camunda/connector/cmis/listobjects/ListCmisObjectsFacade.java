/* ******************************************************************** */
/*  ListObjectsWorker                                                                  */
/*                                                                      */
/*  List all objects in a folder                                        */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.connector.cmis.listobjects;

import io.camunda.connector.cmis.CmisCherryToolbox;
import io.camunda.connector.cmis.sourceobject.CmisSourceAccess;
import io.camunda.cherry.definition.AbstractConnector;
import io.camunda.cherry.definition.BpmnError;
import io.camunda.cherry.definition.RunnerParameter;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ListCmisObjectsFacade extends AbstractConnector {

  public ListCmisObjectsFacade() {
    super(ListCmisObjectFunction.TYPE_CMIS_LISTOBJECTS,

        new ArrayList<RunnerParameter>() {{
          addAll(Arrays.asList(CmisCherryToolbox.CmisConnectionParameter));
          addAll(CmisCherryToolbox.CmisListIdentifyObject);
        }},

        ListCmisObjectInput.class,

        Arrays.asList(
            RunnerParameter.getInstance(ListCmisObjectOutput.OUTPUT_LIST_OBJECT, "List of Cmis object", List.class,
                    RunnerParameter.Level.REQUIRED, "List of description for all Cmis Object found")
                .setVisibleInTemplate()),

        ListCmisObjectOutput.class,

        Arrays.asList(CmisCherryToolbox.NO_CONNECTION_TO_CMIS, // can't connect
            BpmnError.getInstance(CmisSourceAccess.ERROR_UNKNOWN_TYPE, "The Input is not expexcted"), // O
            BpmnError.getInstance(CmisSourceAccess.ERROR_NOT_A_FOLDER,
                "The object must be a Folder to get the content"),
            BpmnError.getInstance(CmisSourceAccess.ERROR_BAD_EXPRESSION, "Filter expression is incorrect")));
  }

  @Override
  public String getName() {
    return ListCmisObjectFunction.NAME_CMIS_LISTOBJECTS;
  }

  @Override
  public String getDescription() {
    return "List objects in a folder";
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
  public ListCmisObjectOutput execute(OutboundConnectorContext context) throws Exception {
    ListCmisObjectFunction listCmisObjectFunction = new ListCmisObjectFunction();
    try {
      return listCmisObjectFunction.execute(context);
    } catch (ConnectorException e) {
      throw new ZeebeBpmnError(e.getErrorCode(), e.getMessage());
    }
  }

}
