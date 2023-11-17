package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory) {
    var compiler = new Compiler(directory);
    return compiler.compile();
  }

  private final Path directory;

  private Compiler(Path directory) {
    this.directory = directory;
  }

  private Semantic.Target compile() {
    var path      = directory.resolve("module.duru");
    var source    = new Source(path, Persistance.load(path));
    var artifacts = directory.resolve("art");
    Persistance.create(artifacts);
    Persistance.store(artifacts.resolve("module.source.duru"), source);
    var tokens = ConfigurationLexer.lex(source);
    Persistance.store(artifacts.resolve("module.tokens.duru"), tokens);
    var node = ConfigurationParser.parse(tokens);
    Persistance.store(artifacts.resolve("module.node.duru"), node);
    var resolution = ConfigurationResolver.resolve(node);
    Persistance.store(artifacts.resolve("module.resolution.duru"), resolution);
    throw Subject.unimplemented();
  }
}
