package io.camunda.connector.cmis.sourceobject;

import io.camunda.connector.api.error.ConnectorException;
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

  public static List<CmisObject> getCmisSourceObject(CmisConnection cmisConnection, CmisSourceObjectInt sourceObject)
      throws ConnectorException {
    if (CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ID.equals(sourceObject.getSourceObject())) {
      return List.of(cmisConnection.getObjectById(sourceObject.getCmisObjectId()));
    } else if (CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME.equals(sourceObject.getSourceObject())) {
      return List.of(cmisConnection.getObjectByPath(sourceObject.getCmisAbsolutePathName()));
    } else if (CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_FOLDERCONTENT.equals(sourceObject.getSourceObject())) {
      CmisObject cmisObject = cmisConnection.getObjectByPath(sourceObject.getCmisAbsolutePathName());

      // use the filter. If the object is a folder, get the contents

      if (!(cmisObject instanceof Folder))
        throw new ConnectorException(ERROR_NOT_A_FOLDER,"Folder [" + sourceObject.getCmisAbsolutePathName()
            + "] is not a folder - parameter folderName and documentName implie folder is a Folder");
      List<CmisObject> listOfCmisObject = new ArrayList<>();
      Folder folder = (Folder) cmisObject;
      final ItemIterable<CmisObject> documents = folder.getChildren();
      Pattern pattern = null;
      if (!sourceObject.getFilter().trim().isEmpty()) {
        try {

          pattern = Pattern.compile(sourceObject.getFilter());
        } catch (Exception e) {
          throw new ConnectorException(ERROR_BAD_EXPRESSION,"Bad regex expression [" + sourceObject.getFilter()+"]");

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
      logger.error("CmisSourceAccess : unknown type["+sourceObject.getSourceObject()+"]");
      throw new ConnectorException(ERROR_UNKNOWN_TYPE, "unknown type["+sourceObject.getSourceObject()+"]");
    }
  }
}
