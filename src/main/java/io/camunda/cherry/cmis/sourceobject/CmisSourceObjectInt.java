package io.camunda.cherry.cmis.sourceobject;

/**
 * Each Input must declare all members.
 * To factorize the getSourceObject, an input must implement this interface
 */
public interface CmisSourceObjectInt {

  public final static String INPUT_SOURCE_OBJECT = "sourceObject";
  public static final String INPUT_SOURCE_OBJECT_V_ID = "objectId";
  public static final String INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME = "absolutePathName";
  public static final String INPUT_SOURCE_OBJECT_V_FOLDERCONTENT = "folderContent";

  public static final String INPUT_CMIS_OBJECTID = "cmisObjectId";

  public static final String INPUT_CMIS_ABSOLUTE_PATH_NAME = "cmisAbsolutePathName";

  public static final String INPUT_FILTER = "filter";

  String getSourceObject();

  String getCmisObjectId();

  String getFilter();

  String getCmisAbsolutePathName();

}
