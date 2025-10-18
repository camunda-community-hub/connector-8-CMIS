package io.camunda.connector.cmis.toolbox;

public class CmisError {
    public static final String UNKNOWN_FUNCTION = "UNKNOWN_FUNCTION";
    public static final String UNKNOWN_FUNCTION_EXPLANATION = "The function is unknown. There is a limited number of operation";

    public static final String NO_CONNECTION_TO_CMIS = "NO_CONNECTION_CMIS";
    public static final String NO_CONNECTION_TO_CMIS_EXPLANATION = "No connection to the CMIS server";

    public static final String INVALID_PARENT = "INVALID_PARENT";
    public static final String INVALID_PARENT_EXPLANATION = "Can't retrieve the parent";

    public static final String FOLDER_CREATION = "FOLDER_CREATION";
    public static final String FOLDER_CREATION_EXPLANATION = "Error during the creation of one object";


    public static final String ERROR_DOUBLE_FOLDER = "DOUBLE_OBJECT";
    public static final String ERROR_DOUBLE_FOLDER_EXPLANATION = "Folder path and Folder Id are fulfill. Only one must be set";

    public static final String FOLDER_NOT_EXIST = "FOLDER_NOT_EXIST";
    public static final String FOLDER_NOT_EXIST_EXPLANATION = "Folder does not exists";

    public static final String DOCUMENT_NOT_EXIST = "DOCUMENT_NOT_EXIST";
    public static final String DOCUMENT_NOT_EXIST_EXPLANATION = "Document specified does not exists";

    public static final String NOT_A_FOLDER = "OBJECT_IS_NOT_A_FOLDER";
    public static final String NOT_A_FOLDER_EXPLANATION = "Object must be a folder";

    public static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";
    public static final String UNKNOWN_TYPE_EXPLANATION = "Type given is not know";

    public final static String BAD_EXPRESSION = "BAD_EXPRESSION";
    public final static String BAD_EXPRESSION_EXPLANATION = "bad expression. It must a a RegExp expression";


    public static final String ERROR_DURING_READ = "ERROR_DURING_READ";
    public static final String ERROR_DURING_READ_EXPLANATION = "Error when reading the object";

    public static final String NOT_A_DOCUMENT = "NOT_A_DOCUMENT";
    public static final String NOT_A_DOCUMENT_EXPLANATION = "The object must be a document";

    public static final String TOO_MANY_OBJECTS = "TOO_MANY_OBJECTS";
    public static final String TOO_MANY_OBJECTS_EXPLANATION = "Too many objects present in the location";


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


    public static final String INCORRECT_STORAGEDEFINITION = "INCORRECTSTORAGEDEFINITION";
    public static final String INCORRECT_STORAGEDEFINITION_EXPLANATION = "Definition to access the storage is incorrect";
}
