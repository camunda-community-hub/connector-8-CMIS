package io.camunda.connector.cmis.toolbox;

public class CmisError {
    public static final String BPMNERROR_UNKNOWN_FUNCTION = "UNKNOWN_FUNCTION";
    public static final String BPMNERROR_UNKNOWN_FUNCTION_EXPL = "The function is unknown. There is a limited number of operation";

    public static final String BPMNERROR_ERROR_INVALID_PARENT ="INVALID_PARENT";
    public static final String BPMNERROR_ERROR_INVALID_PARENT_EXPL ="The parent give is invalid";

    public static final String BPMNERROR_NO_CONNECTION_TO_CMIS = "NO_CONNECTION_CMIS";
    public static final String BPMNERROR_NO_CONNECTION_TO_CMIS_EXPL = "No connection to the CMIS server";

    public static final String BPMNERROR_INVALID_PARENT = "INVALID_PARENT";
    public static final String BPMNERROR_INVALID_PARENT_EXPL= "Can't retrieve the parent";

    public static final String BPMNERROR_FOLDER_CREATION = "FOLDER_CREATION";
    public static final String BPMNERROR_FOLDER_CREATION_EXPL= "Error during the creation of one object";


    public static final String BPMNERROR_ERROR_DOUBLE_FOLDER = "DOUBLE_OBJECT";
    public static final String BPMNERROR_ERROR_DOUBLE_FOLDER_EXPL="Folder path and Folder Id are fulfill. Only one must be set";
    public static final String BPMNERROR_ERROR_FOLDER_NOT_EXIST = "FOLDER_NOT_EXIST";
    public static final String BPMNERROR_ERROR_FOLDER_NOT_EXIST_EXPL="Folder does not exists";
    public static final String BPMNERROR_ERROR_OBJECT_IS_NOT_A_FOLDER = "OBJECT_IS_NOT_A_FOLDER";
    public static final String BPMNERROR_ERROR_OBJECT_IS_NOT_A_FOLDER_EXPL="Object must be a folder";

    public static final String BAD_STORAGE_DEFINITION = "BAD_STORAGE_DEFINITION";
    public static final String BAD_STORAGE_DEFINITION_EXPLANATION = "Storage definition is not correct";
    public static final String ERROR_DURING_READ = "ERROR_DURING_READ";
    public static final String ERROR_NOT_A_DOCUMENT = "NOT_A_DOCUMENT";
    public static final String ERROR_TOO_MANY_OBJECTS = "TOO_MANY_OBJECTS";

    public static final String ERROR_INVALID_PARENT = "INVALID_PARENT";
    public static final String ERROR_INVALID_PARENT_EXPLANATION = "The parent referenced is invalid and can't accept to upload document";

    public static final String LOAD_FILE_ERROR = "LOAD_FILE_ERROR";
    public static final String LOAD_FILE_ERROR_EXPLANATION = "The file can't be loaded";

    public static final String UPLOAD_TO_CMIS_ERROR = "UPLOAD_TO_CMIS_ERROR";
    public static final String UPLOAD_TO_CMIS_ERROR_EXPLANATION = "The upload failed";

    public static final String CMIS_CONSTRAINT_EXCEPTION = "CMIS_CONSTRAINT_EXCEPTION";
    public static final String CMIS_CONSTRAINT_EXCEPTION_EXPLANATION = "A CMIS Constraint rejects the operation";

    public static final String CANT_CHECKOUT_TO_CREATE_VERSION = "CANT_CHECKOUT_TO_CREATE_VERSION";
    public static final String CANT_CHECKOUT_TO_CREATE_VERSION_EXPLANATION = "To create a new version, the document must checkout. The operation failed";

    public static final String CMISTYPE_NOT_VERSIONABLE = "CMISTYPE_NOT_VERSIONABLE";
    public static final String CMISTYPE_NOT_VERSIONABLE_EXPLANATION = "The object is not versionable. Change the import policy";

    public static final String NO_CONNECTION_TO_CMIS = "NO_CONNECTION_CMIS";
    public static final String NO_CONNECTION_TO_CMIS_EXPLANATION="No connection to the CMIS server";

}
