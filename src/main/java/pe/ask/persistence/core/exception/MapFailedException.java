package pe.ask.persistence.core.exception;

import pe.quillqasoft.dev.core.annotation.ExceptionDef;
import pe.quillqasoft.dev.core.model.BaseException;

@ExceptionDef(ErrorCatalog.class)
public class MapFailedException extends BaseException {
    public static Builder<MapFailedException> builder() {
        return new Builder<>(MapFailedException.class);
    }
}
