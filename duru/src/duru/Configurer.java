package duru;

import java.nio.file.Path;

public final class Configurer {
  public static Configuration.Project configure(Path file) {
    var configurer = new Configurer(file);
    return configurer.configure();
  }

  private final Path file;

  private Configurer(Path file) {
    this.file = file;
  }

  private Configuration.Project configure() {
    var text = Persistance.read(file);
    var tokens = ConfigurationLexer.lex(text);
    System.err.println(tokens);
    throw Diagnostic.error("unimplemented");
  }
}
