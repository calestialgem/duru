package duru;

import java.nio.file.Path;

public final class Persistance {
  public static Path path(String path) {
    return Path.of(path).toAbsolutePath().normalize();
  }

  public static void recreate(Path directory) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Persistance() {}
}
