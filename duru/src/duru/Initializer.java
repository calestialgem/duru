package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    checkAvailability(directory);
    var name = directory.getFileName().toString();
    checkIdentifier(name);
    createConfig(directory, name);
    createMainSource(directory);
  }

  private static void checkAvailability(Path directory) {
    for (var i = directory; i != null; i = i.getParent()) {
      var config = i.resolve("project.duru");
      if (Files.exists(config)) {
        throw Exceptions
          .error(
            directory,
            "Cannot initialize inside another project, which is configured at `%s`!",
            config);
      }
    }
  }

  private static void checkIdentifier(String name) {
    for (var i = 0; i != name.length(); i = name.offsetByCodePoints(i, 1)) {
      var character = name.codePointAt(i);
      if (!Character.isLetterOrDigit(character))
        throw Exceptions
          .error(
            "unknown",
            "Name `%s` has a non-letter or digit character `%c` at %d!",
            name,
            character,
            i);
    }
    if (name.length() == 0)
      throw Exceptions.error("unknown", "Name is empty!");
    var initial = name.codePointAt(0);
    if (!Character.isLetter(initial))
      throw Exceptions
        .error(
          "unknown",
          "Name `%s` starts with a non-letter character `%c`!",
          name,
          initial);
    switch (name) {
      case "project", "executable" ->
        throw Exceptions
          .error(
            "unknown",
            "error: Name `%s` is a reserved word for project configuration!",
            name);
      case "void" ->
        throw Exceptions
          .error("unknown", "error: Name `%s` is a reserved word!", name);
      default -> {}
    }
  }

  private static void createConfig(Path directory, String name) {
    Persistance.write(directory.resolve("project.duru"), """
        project %s {
          executable %s;
        }
        """.formatted(name, name));
  }

  private static void createMainSource(Path directory) {
    var sources = directory.resolve("src");
    Persistance.create(sources);
    Persistance.write(sources.resolve("main.duru"), """
        void main() {
          duru.print("Hello, World!\\n");
        }
        """);
  }

  private Initializer() {}
}
