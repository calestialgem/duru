package duru.diagnostic;

/** Reports about the compilation process. */
public record Diagnostic(String message) {
    /** Returns an exception with this diagnostic's message. */
    public RuntimeException toException() {
        return new RuntimeException(message);
    }

    /** Returns an exception with this diagnostic's message and the given
     * cause. */
    public RuntimeException toException(Throwable cause) {
        return new RuntimeException(message, cause);
    }
}
