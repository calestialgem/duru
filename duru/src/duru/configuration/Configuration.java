package duru.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import duru.diagnostic.Subject;

public record Configuration(String name, List<Reference> executables) {
  public Configuration {
    executables = List.copyOf(executables);
  }

  public static Configuration parse(Path file) {
    String contents;
    try {
      contents = Files.readString(file);
    }
    catch (IOException cause) {
      throw Subject
        .of(file)
        .diagnose("failure", "Could not read file!")
        .toException(cause);
    }
    var tokens        = Lexer.lex(file, contents);
    var configuration = Parser.parse(file, contents, tokens);
    return configuration;
  }
}
