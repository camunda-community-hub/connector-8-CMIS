package io.camunda.cherry.cmis.download;

import io.camunda.cherry.cmis.sourceobject.CmisSourceObjectInt;

public class DownloadDocumentInput implements CmisSourceObjectInt {

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
