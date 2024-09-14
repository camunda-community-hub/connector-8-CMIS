package io.camunda.connector.cmis;

import com.google.gson.Gson;

public class CmisToolbox {

  /**
   * Explore the String connection to return an object value.
   * Cmis connection should be a JSON like {"url":"http://localhost:8099/lightweightcmis/browser","userName":"cmisaccess","password":"demo"}
   * @param cmisConnectionString the cmis connection
   * @return the object parsed by Gson
   * @throws Exception
   */
  public static Object getCmisConnection(String cmisConnectionString) throws Exception {
  String valueInJson = cmisConnectionString.toString();
  valueInJson = valueInJson.replace("\\\"", "\"");

      try {
    Gson gson = new Gson();
    return gson.fromJson(valueInJson, Object.class);
  } catch (Exception var7) {
    throw new Exception("Can't decode the GSON on " + valueInJson);
  }
}

}
