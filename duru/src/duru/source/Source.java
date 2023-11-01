package duru.source;

import java.nio.file.Path;

import duru.diagnostic.Subject;

/** Representation of a source file as its contents. */
public record Source(Path path, String contents) {
  /** File extension of source files. */
  public static final String EXTENSION = ".duru";

  /** Constructs. */
  public Source {
    path = path.toAbsolutePath().normalize();
  }

  /** Returns this source as a subject. */
  public Subject subject() {
    return Subject.of(path);
  }

  /** Returns this source as a subject. */
  public Subject subject(int index) {
    return subject(index, 1);
  }

  /** Returns this source as a subject. */
  public Subject subject(int start, int end) {
    return Subject.of(path, contents, start, end);
  }

  /** Returns the source file's name. */
  public String name() {
    var name = path.getFileName().toString();
    return name.substring(0, name.length() - EXTENSION.length());
  }
}
