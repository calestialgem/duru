package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Module compile(Path directory) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Compiler() {}
}
