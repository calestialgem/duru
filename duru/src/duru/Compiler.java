package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory) {
    var configuration =
      Configurer.configure(directory.resolve(Configuration.name));
    throw Diagnostic.failure("Unimplemented!");
  }

  private Compiler() {}
}
