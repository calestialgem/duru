package duru;

import java.nio.file.Path;

final class Initializer {
  public static void initialize(Path directory) {
    initialize(directory, directory.getFileName().toString());
  }

  public static void initialize(Path directory, String name) {
    throw Exceptions.unimplemented();
  }
}
