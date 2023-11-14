package duru;

import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Subject subject, Path directory) {
    throw subject.diagnose("failure", "unimplemented").exception();
  }

  private Initializer() {}
}
