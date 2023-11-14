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
      if (Files.exists(configuration))
        throw Subject
          .get()
          .diagnose(
            "error",
            "initializing in module defined by `%s`",
            configuration)
          .exception();
    }
  }

  private void checkName() {
    for (var i = 0; i != name.length(); i = name.offsetByCodePoints(i, 1)) {
      var body = name.codePointAt(i);
      if (!Text.isIdentifierBody(body)) {
        throw Subject
          .get()
          .diagnose(
            "error",
            "invalid codepoint `%c` in name `%s` at %d",
            body,
            name,
            i)
          .exception();
      }
    }
    if (name.length() == 0)
      throw Subject.get().diagnose("error", "empty name").exception();
    var initial = name.codePointAt(0);
    if (!Text.isIdentifierInitial(initial))
      throw Subject
        .get()
        .diagnose("error", "invalid initial `%c` of name `%s`", initial, name)
        .exception();
    if (Text.isReserved(name)) {
      throw Subject
        .get()
        .diagnose("error", "name `%s` is a keyword", name)
        .exception();
    }
    if (Text.isReservedForConfiguration(name))
      throw Subject
        .get()
        .diagnose(
          "error",
          "name `%s` is a keyword for module configuration",
          name)
        .exception();
  }

  private void createConfiguration() {
    Persistance.store(directory.resolve("module.duru"), """
        module %s {
          executable %s;
        }
        """.formatted(name, name));
  }

  private void createMainSource() {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }
}
