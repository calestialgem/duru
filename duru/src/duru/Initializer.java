package duru;

import java.nio.file.Path;

final class Initializer {
  public static Result<Void, String> initialize(Path directory) {
    return initialize(directory, directory.getFileName().toString());
  }

  public static Result<Void, String> initialize(Path directory, String name) {
    return Result.failure("Unimplemented!");
  }
}
