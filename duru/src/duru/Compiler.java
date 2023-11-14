package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Module compile(Subject subject, Path directory) {
    throw subject.diagnose("failure", "unimplemented").exception();
  }

  private Compiler() {}
}
