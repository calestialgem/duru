package duru.source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import duru.diagnostic.Subject;

/** Reads a source file and stores its contents in memory. */
public final class Loader {
  /** Loads the source file at a path. */
  public static Source load(Path path) {
    var loader = new Loader(path);
    return loader.load();
  }

  /** Path to the loaded source file. */
  private final Path path;

  /** Constructs. */
  private Loader(Path path) {
    this.path = path;
  }

  /** Runs the loader. */
  private Source load() {
    String contents;
    try {
      contents = Files.readString(path);
    }
    catch (IOException cause) {
      throw Subject
        .of(path)
        .diagnose("failure", "Could not read the source file!")
        .toException(cause);
    }
    return new Source(path, contents);
  }
}
