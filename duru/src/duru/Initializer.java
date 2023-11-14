package duru;

import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    var initializer = new Initializer(directory);
    initializer.initialize();
  }

  private final Path directory;

  private Initializer(Path directory) {
    this.directory = directory;
  }

  private void initialize() {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }
}
