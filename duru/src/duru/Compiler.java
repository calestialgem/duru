package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Compiler() {}
}
