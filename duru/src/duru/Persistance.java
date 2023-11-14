package duru;

import java.nio.file.Path;

public final class Persistance {
  public static Path path(String path) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  public static void recreate(Path directory) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Persistance() {}
}
