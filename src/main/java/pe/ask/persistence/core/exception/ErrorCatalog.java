package pe.ask.persistence.core.exception;

import pe.quillqasoft.dev.core.catalog.BaseExceptionCatalog;

import java.util.Map;

public enum ErrorCatalog implements BaseExceptionCatalog {
    MAP_FAILED(
            "ERR-PERSISTENCE-001",
            "MAP_FAILED",
            "The requested user could not be found in the system.",
            404,
            null
    )
    ;

    private final String errorCode;
    private final String exceptionName;
    private final String message;
    private final int status;
    private final Map<String, String> errors;

    ErrorCatalog(String errorCode, String exceptionName, String message, int status, Map<String, String> errors) {
        this.errorCode = errorCode;
        this.exceptionName = exceptionName;
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getExceptionName() {
        return exceptionName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getErrors() {
        return errors;
    }
}
