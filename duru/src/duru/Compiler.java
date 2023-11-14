package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory) {
    var text      = Persistance.read(directory.resolve("project.duru"));
    var artifacts = directory.resolve("art");
    Persistance.create(artifacts);
    Persistance.write(artifacts.resolve("project.duru.text"), text);
    var lectics = ConfigurationLexer.lex(text);
    Persistance.write(artifacts.resolve("project.duru.lectics"), lectics);
    var syntactics = ConfigurationParser.parse(lectics);
    Persistance.write(artifacts.resolve("project.duru.syntactics"), syntactics);
    throw Diagnostic.failure("unimplemented");
  }

  private Compiler() {}
}
