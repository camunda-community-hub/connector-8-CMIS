package io.camunda.connector.cmis.listobjects;

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

public class ListCmisObjectFunction implements CmisSubFunction {
    public static final String TYPE_CMIS_LISTOBJECTS = "list-objects";
    public static final String NAME_CMIS_LISTOBJECTS = "CMIS: List objects";
    public static final String TYPE_CMIS_LIST_OBJECTS = "list-objects";
    private final Logger logger = LoggerFactory.getLogger(ListCmisObjectFunction.class.getName());

    @Override
    public CmisOutput executeSubFunction(CmisConnection cmisConnection,
                                         CmisInput cmisInput,
                                         OutboundConnectorContext context) throws ConnectorException {

        CmisOutput cmisOutput = new CmisOutput();

        List<CmisObject> listOfCmisObject = CmisSourceAccess.getCmisSourceObject(cmisConnection, cmisInput);
        if (listOfCmisObject == null)
            return cmisOutput;
        StringBuilder listObjectsToLog = new StringBuilder();
        for (CmisObject cmisObject : listOfCmisObject) {
            cmisOutput.listCmisObject.add(CmisToolbox.createDescription(cmisObject));
            listObjectsToLog.append(cmisObject.getId() + ",");
        }
        logger.info("List documents [{}]", listObjectsToLog);
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
        );
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(
                RunnerParameter.getInstance(CmisOutput.LIST_CMIS_OBJECT, "List CMIS Object deleted", List.class,
                        RunnerParameter.Level.REQUIRED,
                        "List of ID of CMIS Object deleted"));
    }

    @Override
    public Map<String, String> getBpmnErrors() {
        return Map.of(
                CmisError.BAD_EXPRESSION, CmisError.BAD_EXPRESSION_EXPLANATION,
                CmisError.NOT_A_FOLDER, CmisError.NOT_A_FOLDER_EXPLANATION,
                CmisError.UNKNOWN_TYPE, CmisError.UNKNOWN_TYPE_EXPLANATION,
                CmisError.FOLDER_NOT_EXIST, CmisError.FOLDER_NOT_EXIST_EXPLANATION,
                CmisError.DOCUMENT_NOT_EXIST, CmisError.DOCUMENT_NOT_EXIST_EXPLANATION);
    }

    @Override
    public String getSubFunctionName() {
        return "ListObjects";
    }

    @Override
    public String getSubFunctionDescription() {
        return "List all objects found in a folder. This is not a recursive list, just one level.";
    }

    @Override
    public String getSubFunctionType() {
        return TYPE_CMIS_LIST_OBJECTS;
    }

}
