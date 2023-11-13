package duru;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class Persistance {
  public static Path path(String text) {
    return Path.of(text).toAbsolutePath().normalize();
  }

  public static void write(Path file, Object text) {
    try {
      Files.writeString(file, text.toString());
    }
    catch (IOException cause) {
      throw Diagnostic.failure(cause, "could not write to file `%s`", file);
    }
  }

  public static void recreate(Path directory) {
    if (Files.exists(directory))
      delete(directory);
    create(directory);
  }

  private static void delete(Path directory) {
    try {
      Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(
          Path file,
          BasicFileAttributes attributes)
          throws IOException
        {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(
          Path directory,
          IOException cause)
          throws IOException
        {
          if (cause != null) {
            throw cause;
          }
          Files.delete(directory);
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException cause) {
      throw Diagnostic
        .failure(cause, "could not delete directory `%s`", directory);
    }
  }

  public static void create(Path directory) {
    try {
      Files.createDirectory(directory);
    }
    catch (IOException cause) {
      throw Diagnostic
        .failure(cause, "could not create directory `%s`", directory);
    }
  }

  private Persistance() {}
}
