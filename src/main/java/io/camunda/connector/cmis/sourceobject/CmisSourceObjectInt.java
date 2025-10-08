package io.camunda.connector.cmis.sourceobject;

/**
 * Each Input must declare all members.
 * To factorize the getSourceObject, an input must implement this interface
 */
public interface CmisSourceObjectInt {

    String INPUT_SOURCE_OBJECT_OLD = "sourceObject";
    String INPUT_SOURCE_OBJECT_V_ID_OLD = "objectId";
    String INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME_OLD = "absolutePathName";
    String INPUT_SOURCE_OBJECT_V_FOLDERCONTENT_OLD = "folderContent";

    String INPUT_SOURCE_OBJECT_V_ID_LABEL_OLD = "objectId";
    String INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME_LABEL_OLD = "Absolute PathName";
    String INPUT_SOURCE_OBJECT_V_FOLDERCONTENT_LABEL_OLD = "Folder Content";

    String INPUT_CMIS_OBJECTID_OLD = "cmisObjectId";

    String INPUT_CMIS_ABSOLUTE_PATH_NAME_OLD = "cmisAbsolutePathName";

    String INPUT_FILTER_OLD = "filter";

    String getSourceObject();

    String getCmisObjectId();

    String getFilter();

    String getCmisAbsolutePathName();

}
