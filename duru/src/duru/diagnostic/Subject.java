package duru.diagnostic;

import java.nio.file.Path;
import java.util.Formatter;

import duru.Locations;

/** A diagnostic's creator. */
public record Subject(String text) {
  /** Returns a subject by the given name. */
  public static Subject of(String name) {
    return new Subject(name);
  }

  /** Returns a subject as the given path. */
  public static Subject of(Path path) {
    return new Subject(path.toAbsolutePath().normalize().toString());
  }

  /** Returns a subject as the location in the given file from the start byte up
   * to the end byte. */
  public static Subject of(Path file, String contents, int start, int end) {
    return new Subject(Locations.locate(file, contents, start, end));
  }

  /** Returns a message from this subject. */
  public Diagnostic diagnose(String title, String format, Object... arguments) {
    var buffer = new StringBuilder();
    try (var formatter = new Formatter(buffer)) {
      formatter.format("%s: %s: ", text, title);
      formatter.format(format, arguments);
    }
    var message = buffer.toString();
    return new Diagnostic(message);
  }
}
