package io.camunda.cherry.cmis.upload;

public class UploadDocumentInput {
  public static final String INPUT_CMIS_CONNECTION = "cmisConnection";

  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_CMIS_ABSOLUTE_FOLDER_PATH_NAME = "cmisAbsoluteFolderPathName";
  public static final String INPUT_DOCUMENT_NAME = "documentName";
  public static final String INPUT_DESCRIPTION = "description";
  public static final String INPUT_IMPORT_POLICY = "importPolicy";
  public static final String INPUT_IMPORT_POLICY_V_NEW_DOCUMENT = "NEW_DOCUMENT";
  public static final String INPUT_IMPORT_POLICY_V_NEW_VERSION = "NEW_VERSION";
  public static final String INPUT_CMIS_TYPE = "cmisType";

  public static final String INPUT_VERSION_LABEL = "versionLabel";
  /**
   * According to https://chemistry.apache.org/docs/cmis-samples/samples/content/index.html
   * A CMIS client cannot upload, modify, or delete a rendition.
   */

  public String sourceFile;
  public String cmisAbsoluteFolderPathName;
  public String documentName;
  public String description;
  public String importPolicy;
  public String cmisType;
  public String versionLabel;



  public String cmisConnection;

  public String getSourceFile() {
    return sourceFile;
  }

  public String getCmisAbsoluteFolderPathName() {
    return cmisAbsoluteFolderPathName;
  }

  public String getDocumentName() {
    return documentName;
  }

  public String getDescription() {
    return description;
  }

  public String getImportPolicy() {
    return importPolicy;
  }

  public String getVersionLabel() {
    return versionLabel;
  }

  public String getCmisType() {
    return cmisType==null || cmisType.trim().isEmpty()? "cmis:document" : cmisType;
  }

  public String getCmisConnection() {
    return cmisConnection;
  }
}
