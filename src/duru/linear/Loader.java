package duru.linear;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads a source file and stores its contents in memory. */
public final class Loader {
    /** Loads a source file at a path. */
    public static Linear load(Path path) throws LoadingException {
        var loader = new Loader(path);
        return loader.load();
    }

    /** Loaded path. */
    private final Path path;

    /** Constructs. */
    private Loader(Path path) { this.path = path; }

    /** Runs the loader. */
    private Linear load() throws LoadingException {
        String contents;
        try {
            contents = Files.readString(path);
        }
        catch (IOException cause) {
            throw new LoadingException(
                path,
                "Could not read the source file!",
                cause);
        }
        return new Linear(path, contents);
    }
}
