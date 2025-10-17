package io.camunda.connector.cmis.sourceobject;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.cmis.CmisInput;
import io.camunda.connector.cmis.toolbox.CmisError;
import io.camunda.filestorage.cmis.CmisConnection;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CmisSourceAccess {

    private static final Logger logger = LoggerFactory.getLogger(CmisSourceAccess.class.getName());


    /**
     * Each Input must declare all members.
     * To factorize the getSourceObject, an input must implement this interface
     * Constant are declared at this level
     */

    public static List<CmisObject> getCmisSourceObject(CmisConnection cmisConnection, CmisInput cmisInput)
            throws ConnectorException {

        if (CmisInput.SOURCE_OBJECT_V_ID.equals(cmisInput.getSourceObject())) {
            try {
                return List.of(cmisConnection.getObjectById(cmisInput.getCmisObjectId()));
            } catch (Exception e) {
                logger.error("cmisObjectId[{}] does not exist in the CMIS database", cmisInput.getCmisObjectId());
                throw new ConnectorException(CmisError.DOCUMENT_NOT_EXIST, "Document [" + cmisInput.getCmisObjectId() + "] does not exist");
            }
        } else if (CmisInput.SOURCE_OBJECT_V_ABSOLUTEPATHNAME.equals(cmisInput.getSourceObject())) {
            try {
                return List.of(cmisConnection.getObjectByPath(cmisInput.getAbsoluteFolderName()));
            } catch (Exception e) {
                logger.error("absoluteFolderName[{}] does not exist in the CMIS database", cmisInput.getAbsoluteFolderName());
                throw new ConnectorException(CmisError.FOLDER_NOT_EXIST, "Document [" + cmisInput.getAbsoluteFolderName() + "] does not exist");
            }
        } else if (CmisInput.SOURCE_OBJECT_V_FOLDERCONTENT.equals(cmisInput.getSourceObject())) {
            CmisObject cmisObject;
            try {
                cmisObject = cmisConnection.getObjectByPath(cmisInput.getAbsoluteFolderName());
            } catch (Exception e) {
                logger.error("absoluteFolderName[{}] does not exist in the CMIS database", cmisInput.getAbsoluteFolderName());
                throw new ConnectorException(CmisError.FOLDER_NOT_EXIST, "Document [" + cmisInput.getAbsoluteFolderName() + "] does not exist");
            }
            // use the filter. If the object is a folder, get the contents
            if (!(cmisObject instanceof Folder folder)) {
                logger.error("absoluteFolderName[{}] is not a folder in the CMIS database", cmisInput.getAbsoluteFolderName());
                throw new ConnectorException(CmisError.NOT_A_FOLDER, "Folder [" + cmisInput.getAbsoluteFolderName()
                        + "] is not a folder");
            }
            List<CmisObject> listOfCmisObject = new ArrayList<>();
            final ItemIterable<CmisObject> documents = folder.getChildren();
            Pattern pattern = null;
            if (!cmisInput.getFilter().trim().isEmpty()) {
                try {
                    pattern = Pattern.compile(cmisInput.getFilter());
                } catch (Exception e) {
                    logger.error("Filter[{}] bad expression", cmisInput.getFilter(),e);
                    throw new ConnectorException(CmisError.BAD_EXPRESSION, "Bad regex expression [" + cmisInput.getFilter() + "]");

                }
            }
            for (CmisObject document : documents) {
                // a filter is actif?
                if (pattern != null && !pattern.matcher(document.getName()).matches())
                    continue;
                listOfCmisObject.add(document);

            }
            return listOfCmisObject;
        } else {
            logger.error("CmisSourceObject : unknown type[{}]", cmisInput.getSourceObject());
            throw new ConnectorException(CmisError.UNKNOWN_TYPE, "unknown type[" + cmisInput.getSourceObject() + "]");
        }
    }
}
