package duru;

import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Initializer() {}
}
