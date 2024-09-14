package io.camunda.connector.cmis.listobjects;

import java.util.ArrayList;
import java.util.List;

public class ListCmisObjectOutput {

  public static class CmisObjectDescription {
    public String id;
    public String name;
    public String description;
    public boolean isFolder;
  }

  public final static String OUTPUT_LIST_OBJECT = "listCmisObject";
  public List<CmisObjectDescription> listCmisObject = new ArrayList<>();

  public List<CmisObjectDescription> getlistCmisObject() {
    return listCmisObject;
  }
}
