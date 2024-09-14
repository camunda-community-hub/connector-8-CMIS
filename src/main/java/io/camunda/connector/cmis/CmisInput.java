package io.camunda.connector.cmis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cmis.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

/**
 * the JsonIgnoreProperties is mandatory: the template may contain additional widget to help the designer, especially on the OPTIONAL parameters
 * This avoids the MAPPING Exception
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmisInput implements CherryInput {
  /**
   * Attention, each Input here must be added in the PdfFunction, list of InputVariables
   */
  public static final String INPUT_CMIS_FUNCTION = "cmisFunction";
  private String cmisFunction;

  public static final String INPUT_CMIS_CONNECTION = "cmisConnection";
  public String cmisConnection;

  public static final String INPUT_FOLDER_PATH = "folderPath";
  public String folderPath;

  public static final String INPUT_FOLDER_NAME = "folderName";
  public String folderName;
  public static final String INPUT_RECURSIVE_NAME = "recursiveName";
  public Boolean recursiveName;

  public static final String INPUT_FOLDER_CMIS_TYPE = "folderCmisType";
  public String folderCmisType;

  public static final String INPUT_ERROR_IF_NOT_EXIST = "ErrorIfNotExist";
  public Boolean errorIfNotExist;

  public static final String INPUT_FOLDER_ID = "folderId";
  public String folderId;


  public String getCmisFunction() {
    return cmisFunction;
  }

  public String getCmisConnection() {
    return cmisConnection;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public String getFolderName() {
    return folderName;
  }

  public Boolean getRecursiveName() {
    return recursiveName;
  }

  public String getFolderCmisType() {
    return folderCmisType;
  }

  public String getFolderId() {
    return folderId;
  }

  public Boolean getErrorIfNotExist() {
    return errorIfNotExist;
  }

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getInputParameters();

    /*
    Arrays.asList(
        CmisCherryToolbox.CmisConnectionParameter,
        RunnerParameter.getInstance(INPUT_FOLDER_PATH,
            "Parent Folder Path",
            String.class, RunnerParameter.Level.REQUIRED, "Folder path where folder will be created"),
        RunnerParameter.getInstance(INPUT_FOLDER_NAME,
            "Folder Name",
            String.class, RunnerParameter.Level.REQUIRED, "Folder name to be created."),
        RunnerParameter.getInstance(INPUT_RECURSIVE_NAME,
                "Recursive Name",
                Boolean.class, RunnerParameter.Level.OPTIONAL, "Recursive name: folder name can contains '/'")
            .setVisibleInTemplate()
            .setDefaultValue(Boolean.FALSE),
        RunnerParameter.getInstance(INPUT_FOLDER_CMIS_TYPE,
                "Folder CMIS Type",
                String.class, RunnerParameter.Level.OPTIONAL, "When an CMIS object is created, a type is assigned")
            .setDefaultValue("cmis:folder")



    public static final RunnerParameter CmisConnectionParameter = RunnerParameter.getGsonInstance(
        CmisCherryToolbox.INPUT_CMIS_CONNECTION, "Cmis connection", RunnerParameter.Level.REQUIRED,
        "Connection to the CMIS repository", CmisCherryToolbox.CMIS_TEMPLATE_CONNECTION);


    public static final RunnerParameter CmisSourceDocument = RunnerParameter.getInstance(
            CmisSourceObjectInt.INPUT_SOURCE_OBJECT, "Source document", String.class, RunnerParameter.Level.REQUIRED,
            "Define the source of document")
        .addChoice(CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ID, "by document Id")
        .addChoice(CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME, "By document folder/name")
        .addChoice(CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_FOLDERCONTENT, "Content of the folder")
        .setGroup(GROUP_SOURCE_OBJECT);

    public static final RunnerParameter CmisObjectId = RunnerParameter.getInstance(
            CmisSourceObjectInt.INPUT_CMIS_OBJECTID, "CMIS Object (Folder, Document) ID", String.class,
            RunnerParameter.Level.REQUIRED, "CMIS ID to access the item")
        .addCondition(CmisSourceObjectInt.INPUT_SOURCE_OBJECT, List.of(CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ID))
        .setGroup(GROUP_SOURCE_OBJECT);

    public static final RunnerParameter CmisFolderName = RunnerParameter.getInstance(
            CmisSourceObjectInt.INPUT_CMIS_ABSOLUTE_PATH_NAME, "Absolute Object (Folder, Document) path name", String.class,
            RunnerParameter.Level.REQUIRED,
            "Contains / to access via a hierarchy of folder, like '/company/consulting/customers")
        .addCondition(CmisSourceObjectInt.INPUT_SOURCE_OBJECT,
            List.of(CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_ABSOLUTEPATHNAME,
                CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_FOLDERCONTENT))
        .setGroup(GROUP_SOURCE_OBJECT);

    public static final RunnerParameter CmisFilter = RunnerParameter.getInstance(CmisSourceObjectInt.INPUT_FILTER,
            "Item Name", String.class, ".*", RunnerParameter.Level.OPTIONAL,
            "Filter (RegExp) to apply to collect object. [.*] mean all")
        .addCondition(CmisSourceObjectInt.INPUT_SOURCE_OBJECT,
            List.of(CmisSourceObjectInt.INPUT_SOURCE_OBJECT_V_FOLDERCONTENT))
        .setGroup(GROUP_SOURCE_OBJECT);
  }
  */
  }
}
