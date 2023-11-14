package duru;

import java.nio.file.Path;

public final class ConfigurationParser {
  public static Configuration.Project parse(Path file) {
    var parser = new ConfigurationParser(file);
    return parser.parse();
  }

  private final Path file;

  private ConfigurationParser(Path file) {
    this.file = file;
  }

  private Configuration.Project parse() {
    var text    = Persistance.read(file);
    var lectics = ConfigurationLexer.lex(text);
    System.err.println(lectics);
    throw Diagnostic.error("unimplemented");
  }
}
