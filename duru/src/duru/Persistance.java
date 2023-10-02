package duru;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import duru.diagnostic.Diagnostic;

public final class Persistance {
  public static boolean create(Diagnostic diagnostic, Path directory) {
    try {
      Files.createDirectory(directory);
      return false;
    }
    catch (IOException exception) {
      diagnostic.begin();
      diagnostic.exception(exception);
      diagnostic
        .failure(
          "Could not create the directory '%s'!",
          directory.toAbsolutePath().normalize());
      return true;
    }
  }

  public static boolean write(
    Diagnostic diagnostic,
    Path file,
    String contents)
  {
    try {
      Files.writeString(file, contents);
      return false;
    }
    catch (IOException exception) {
      diagnostic.begin();
      diagnostic.exception(exception);
      diagnostic
        .failure(
          "Could not write to the file '%s'!",
          file.toAbsolutePath().normalize());
      return true;
    }
  }

  private Persistance() {}
}
