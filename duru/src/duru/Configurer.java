package duru;

import java.nio.file.Path;
import java.util.Optional;

public final class Configurer {
  public static Configuration.Project configure(Path file) {
    var configurer = new Configurer(file);
    return configurer.configure();
  }

  private final Path                           file;
  private Optional<Configuration.Identifier>   name;
  private ListBuffer<Configuration.Executable> executables;
  private String                               text;
  private int                                  index;

  private Configurer(Path file) {
    this.file = file;
  }

  private Configuration.Project configure() {
    text        = Persistance.read(file);
    name        = Optional.empty();
    executables = ListBuffer.create();
    index       = 0;
    skipWhitespaceAndComments();
    var begin = index;
    if (!text.startsWith("project", index)) {
      throw Diagnostic.error("expected project configuration at %d", index);
    }
    index += "project".length();
    skipWhitespaceAndComments();
    var end = index;
    skipWhitespaceAndComments();
    if (hasCharacter()) {
      throw Diagnostic
        .error(
          "expected end of file instead of `%c` at %d",
          getCharacter(),
          index);
    }
    return new Configuration.Project(
      begin,
      end,
      name.get(),
      executables.toList());
  }

  private void skipWhitespaceAndComments() {
    while (hasCharacter()) {
      if (Character.isWhitespace(getCharacter())) {
        advance();
        continue;
      }
      if (getCharacter() == '#') {
        advance();
        while (hasCharacter() && getCharacter() != '\n')
          advance();
        advance();
      }
      break;
    }
  }

  private int advance() {
    return index = text.offsetByCodePoints(index, 1);
  }

  private int getCharacter() {
    return text.codePointAt(index);
  }

  private boolean hasCharacter() {
    return index != text.length();
  }
}
