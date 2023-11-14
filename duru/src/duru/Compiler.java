package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory) {
    var configuration =
      ConfigurationParser.parse(directory.resolve(Configuration.name));
    var artifacts     = directory.resolve("art");
    Persistance.create(artifacts);
    Persistance.write(artifacts.resolve(Configuration.name), configuration);
    throw Diagnostic.failure("unimplemented");
  }

  private Compiler() {}
}
