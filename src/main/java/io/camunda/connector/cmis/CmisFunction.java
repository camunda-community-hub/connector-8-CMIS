package io.camunda.connector.cmis;

/* ******************************************************************** */
/*                                                                      */
/*  PdfFunction                                                         */
/*                                                                      */
/*  This connector is the main connector, doing the distribution on     */
/*  specific function                                                   */
/* ******************************************************************** */

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.connector.cmis.checkconnection.CheckConnectionFunction;
import io.camunda.connector.cmis.createfolder.CreateFolderFunction;
import io.camunda.connector.cmis.deletedocument.DeleteDocumentFunction;
import io.camunda.connector.cmis.deletefolder.DeleteFolderFunction;
import io.camunda.connector.cmis.download.DownloadDocumentFunction;
import io.camunda.connector.cmis.listobjects.ListCmisObjectFunction;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.connector.cmis.toolbox.CmisSubFunction;
import io.camunda.connector.cmis.upload.UploadDocumentFunction;
import io.camunda.filestorage.cmis.CmisConnection;
import io.camunda.filestorage.cmis.CmisFactoryConnection;
import io.camunda.filestorage.cmis.CmisParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

@OutboundConnector(name = "CMISFunction", inputVariables = {
        CmisInput.CMIS_FUNCTION,
        CmisInput.CMIS_CONNECTION,
        CmisInput.FOLDER_PATH,
        CmisInput.FOLDER_NAME,
        CmisInput.RECURSIVE_NAME,
        CmisInput.CMIS_TYPE,
        CmisInput.RECURSIVE_NAME,
        CmisInput.UPLOAD_FOLDER_NAME,
        CmisInput.JSON_STORAGE_DEFINITION,
        CmisInput.STORAGE_DEFINITION,
        CmisInput.STORAGE_DEFINITION_FOLDER_COMPLEMENT,
        CmisInput.STORAGE_DEFINITION_CMIS_COMPLEMENT,
        CmisInput.ERROR_IF_NOT_EXIST,
        CmisInput.FOLDER_ID,
        CmisInput.SOURCE_FILE,
        CmisInput.ABSOLUTE_FOLDER_NAME,
        CmisInput.DOCUMENT_NAME,
        CmisInput.CMIS_TYPE,
        CmisInput.IMPORT_POLICY,
        CmisInput.VERSION_LABEL,
        CmisInput.SOURCE_OBJECT,
        CmisInput.CMIS_OBJECT_ID,
        CmisInput.FILTER

}, type = "c-cmis-function")

public class CmisFunction implements OutboundConnectorFunction, CherryConnector {
    public static final List<Class<?>> allFunctions = Arrays.asList(CreateFolderFunction.class,
            DeleteDocumentFunction.class,
            DeleteFolderFunction.class,
            DownloadDocumentFunction.class,
            ListCmisObjectFunction.class,
            UploadDocumentFunction.class,
            CheckConnectionFunction.class);
    public static final String WORKER_LOGO =
            "data:image/png;base64,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";
    private final Logger logger = LoggerFactory.getLogger(CmisFunction.class.getName());

    @Override
    public CmisOutput execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
        CmisInput cmisInput = outboundConnectorContext.bindVariables(CmisInput.class);
        // search the function referenced
        String function = cmisInput.getCmisFunction();
        long beginTime = System.currentTimeMillis();
        logger.info("CmisFunction receive function [{}]", function);


        CmisConnection cmisConnection = null;
        try {
            CmisParameters cmisParameters = CmisParameters.getCodingConnection(cmisInput.getCmisConnection());
            cmisConnection = CmisFactoryConnection.getInstance().getCmisConnection(cmisParameters);
        } catch (Exception e) {
            throw new ConnectorException(CmisError.NO_CONNECTION_TO_CMIS, "No connection with [" + cmisInput.getCmisConnection());
        }


        for (CmisSubFunction inputSubFunction : getListSubFunctions()) {
            if (inputSubFunction.getSubFunctionType().equals(function)) {
                CmisOutput cmisOutput = inputSubFunction.executeSubFunction(cmisConnection, cmisInput, outboundConnectorContext);
                logger.info("CMISFunction End function [{}] in {} ms", function, System.currentTimeMillis() - beginTime);
                return cmisOutput;
            }
        }
        throw new ConnectorException(CmisError.UNKNOWN_FUNCTION, "Unknown function " + function + "]");

    }

    @Override
    public String getDescription() {
        return "Different CMIS functions available: " + getListSubFunctions().stream()
                .map(CmisSubFunction::getSubFunctionDescription)
                .collect(Collectors.joining(","));
    }

    @Override
    public String getLogo() {
        return WORKER_LOGO;
    }

    @Override
    public String getCollectionName() {
        return "CMIS";
    }

    @Override
    public Map<String, String> getListBpmnErrors() {
        Map<String, String> allErrors = new HashMap<>();
        allErrors.put(CmisError.UNKNOWN_FUNCTION, CmisError.UNKNOWN_FUNCTION_EXPLANATION);
        allErrors.put(CmisError.NO_CONNECTION_TO_CMIS, CmisError.NO_CONNECTION_TO_CMIS_EXPLANATION);

        for (CmisSubFunction subFunction : getListSubFunctions()) {
            allErrors.putAll(subFunction.getBpmnErrors());
        }
        return allErrors;
    }

    @Override
    public Class<?> getInputParameterClass() {
        return CmisInput.class;
    }

    @Override
    public Class<?> getOutputParameterClass() {
        return CmisOutput.class;
    }

    /**
     * Only task at this moment (no InboundConnector)
     *
     * @return list of items where the function applies
     */
    @Override
    public List<String> getAppliesTo() {
        return List.of("bpmn:ServiceTask");
    }

    /**
     * Return the list of function detected
     *
     * @return list of sub functions available
     */
    public List<CmisSubFunction> getListSubFunctions() {
        List<CmisSubFunction> listSubFunction = new ArrayList<>();

        for (Class<?> classFunction : allFunctions) {
            try {
                Constructor<?> constructor = classFunction.getConstructor();
                CmisSubFunction inputSubFunction = (CmisSubFunction) constructor.newInstance();
                listSubFunction.add(inputSubFunction);
            } catch (Exception e) {
                logger.error("Can't call a constructor on {} : {}", classFunction.getName(), e.toString());
            }

        }
        return listSubFunction;
    }

}
