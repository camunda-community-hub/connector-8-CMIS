package io.camunda.cherry.cmis.listobjects;

import io.camunda.cherry.cmis.sourceobject.CmisSourceObjectInt;

public class ListCmisObjectInput implements CmisSourceObjectInt {

  public static final String INPUT_CMIS_CONNECTION = "cmisConnection";

  // constant attached to this properties is defined in CmisSourceObjectInt

  String sourceObject;
  String cmisObjectId;
  String cmisAbsolutePathName;
  String filter;


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

  public String getCmisConnection() {
    return cmisConnection;
  }
}
