package duru;

import java.nio.file.Path;

public final class Persistance {
  public static Path path(Subject subject, String path) {
    throw subject.diagnose("failure", "unimplemented").exception();
  }

  public static void recreate(Subject subject, Path directory) {
    throw subject.diagnose("failure", "unimplemented").exception();
  }

  private Persistance() {}
}
