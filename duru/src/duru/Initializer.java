package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    var initializer = new Initializer(directory);
    initializer.initialize();
  }

  private final Path directory;
  private String     name;

  private Initializer(Path directory) {
    this.directory = directory;
  }

  private void initialize() {
    checkAvailability();
    name = directory.getFileName().toString();
    checkName();
    createConfiguration();
    createMainSource();
  }

  private void checkAvailability() {
    for (var i = directory; i != null; i = i.getParent()) {
      var configuration = i.resolve("module.duru");
      if (Files.exists(configuration)) {
        throw Diagnostic
          .error(
            directory,
            "initializing in module defined by `%s`",
            configuration);
      }
    }
  }

  private void checkName() {
    for (var i = 0; i != name.length(); i = name.offsetByCodePoints(i, 1)) {
      var body = name.codePointAt(i);
      if (!Text.isIdentifierBody(body)) {
        throw Diagnostic
          .error(
            directory,
            "invalid codepoint `%c` in name `%s` at %d",
            body,
            name,
            i);
      }
    }
    if (name.length() == 0) {
      throw Diagnostic.error(directory, "empty name");
    }
    var initial = name.codePointAt(0);
    if (!Text.isIdentifierInitial(initial)) {
      throw Diagnostic
        .error(directory, "invalid initial `%c` of name `%s`", initial, name);
    }
    if (Text.isReserved(name)) {
      throw Diagnostic.error(directory, "name `%s` is a keyword", name);
    }
    if (Text.isReservedForConfiguration(name)) {
      throw Diagnostic
        .error(
          directory,
          "name `%s` is a keyword for module configuration",
          name);
    }
  }

  private void createConfiguration() {
    Persistance.store(directory, directory.resolve("module.duru"), """
executable %s;
""".formatted(name));
  }

  private void createMainSource() {
    var sources = directory.resolve("src");
    Persistance.create(directory, sources);
    Persistance.store(directory, sources.resolve("main.duru"), """
proc main() {
  duru.print("Hello, World!\\n");
}
""");
  }
}
