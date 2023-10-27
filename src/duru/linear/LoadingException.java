package duru.linear;

import java.nio.file.Path;

/** Exceptions that can interfere with loading a source file. */
public final class LoadingException extends Exception {
    /** Path to the source file that could not be loaded. */
    private final Path path;

    /** Constructs. */
    LoadingException(Path path, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    /** Path to the source file that could not be loaded. */
    public Path path() {
        return path;
    }
}
