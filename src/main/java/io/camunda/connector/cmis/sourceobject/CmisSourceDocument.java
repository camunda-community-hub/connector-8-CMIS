package io.camunda.connector.cmis.sourceobject;

public class CmisSourceDocument implements CmisSourceObjectInt {

  public static final String INPUT_CMIS_CONNECTION = "cmisConnection";
  String sourceObject;
  String cmisObjectId;
  String cmisAbsolutePathName;
  String filter;

  public static final String INPUT_STORAGE_DEFINITION = "storageDefinition";

  String storageDefinition;

  String cmisConnection;

  public String getSourceObject() {
    return sourceObject;
  }

  public String getCmisObjectId() {
    return cmisObjectId;
  }

  public String getFilter() {
    return filter;
  }

  public String getCmisAbsolutePathName() {
    return cmisAbsolutePathName;
  }

  public String getStorageDefinition() {
    return storageDefinition;
  }

  public String getCmisConnection() {
    return cmisConnection;
  }
}
