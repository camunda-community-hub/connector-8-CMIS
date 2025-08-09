package io.camunda.connector.cmis.sourceobject;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.cmis.CmisInput;
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
  public static final String ERROR_UNKNOWN_TYPE = "ERROR_UNKNOWN_TYPE";
  public final static String ERROR_NOT_A_FOLDER= "NOT_A_FOLDER";
  public final static String ERROR_BAD_EXPRESSION= "BAD_EXPRESSION";

  private static Logger logger = LoggerFactory.getLogger(CmisSourceAccess.class.getName());


  /**
   * Each Input must declare all members.
   * To factorize the getSourceObject, an input must implement this interface
   * Constant are declared at this level
   */

  public static List<CmisObject> getCmisSourceObject(CmisConnection cmisConnection, CmisInput cmisInput)
      throws ConnectorException {
    if (CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ID.equals(cmisInput.getSourceObject())) {
      return List.of(cmisConnection.getObjectById(cmisInput.getCmisObjectId()));
    } else if (CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME.equals(cmisInput.getSourceObject())) {
      return List.of(cmisConnection.getObjectByPath(cmisInput.getAbsoluteFolderName()));
    } else if (CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_FOLDERCONTENT.equals(cmisInput.getSourceObject())) {
      CmisObject cmisObject = cmisConnection.getObjectByPath(cmisInput.getAbsoluteFolderName());

      // use the filter. If the object is a folder, get the contents
      if (!(cmisObject instanceof Folder))
        throw new ConnectorException(ERROR_NOT_A_FOLDER,"Folder [" + cmisInput.getAbsoluteFolderName()
            + "] is not a folder - parameter folderName and documentName implie folder is a Folder");
      List<CmisObject> listOfCmisObject = new ArrayList<>();
      Folder folder = (Folder) cmisObject;
      final ItemIterable<CmisObject> documents = folder.getChildren();
      Pattern pattern = null;
      if (!cmisInput.getFilter().trim().isEmpty()) {
        try {

          pattern = Pattern.compile(cmisInput.getFilter());
        } catch (Exception e) {
          throw new ConnectorException(ERROR_BAD_EXPRESSION,"Bad regex expression [" + cmisInput.getFilter()+"]");

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
      logger.error("CmisSourceAccess : unknown type[{}]",cmisInput.getSourceObject());
      throw new ConnectorException(ERROR_UNKNOWN_TYPE, "unknown type["+cmisInput.getSourceObject()+"]");
    }
  }
}
