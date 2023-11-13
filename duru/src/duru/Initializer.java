package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    checkAvailability(directory);
    var name = directory.getFileName().toString();
    checkIdentifier(name);
    throw Exceptions.unimplemented();
  }

  private static void checkAvailability(Path directory) {
    for (var i = directory; i != null; i = i.getParent()) {
      var config = i.resolve("project.duru");
      if (Files.exists(config)) {
        throw new RuntimeException(
          "%s: error: Cannot initialize inside another project, which is configured at `%s`!"
            .formatted(directory, config));
      }
    }
  }

  private static void checkIdentifier(String name) {
    for (var i = 0; i != name.length(); i = name.offsetByCodePoints(i, 1)) {
      var character = name.codePointAt(i);
      if (!Character.isLetterOrDigit(character))
        throw new RuntimeException(
          "error: Name `%s` has a non-letter or digit character `%c` at %d!"
            .formatted(name, character, i));
    }
    if (name.length() == 0)
      throw new RuntimeException("error: Name is empty!");
    var initial = name.codePointAt(0);
    if (!Character.isLetter(initial))
      throw new RuntimeException(
        "error: Name `%s` starts with a non-letter character `%c`!"
          .formatted(name, initial));
    switch (name) {
      case "project", "executable" ->
        throw new RuntimeException(
          "error: Name `%s` is a reserved word for project configuration!"
            .formatted(name));
      case "void" ->
        throw new RuntimeException(
          "error: Name `%s` is a reserved word!".formatted(name));
      default -> {}
    }
  }

  private Initializer() {}
}
