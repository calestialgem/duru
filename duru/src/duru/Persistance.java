package duru;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class Persistance {
  public static Path path(String path) {
    return Path.of(path).toAbsolutePath().normalize();
  }

  public static void store(Path file, Object text) {
    try {
      Files.writeString(file, text.toString());
    }
    catch (IOException cause) {
      throw Subject
        .get()
        .diagnose("failure", "could not write to `%s`", file)
        .exception(cause);
    }
  }

  public static void recreate(Path directory) {
    if (Files.exists(directory)) {
      delete(directory);
    }
    create(directory);
  }

  public static void create(Path directory) {
    try {
      Files.createDirectory(directory);
    }
    catch (IOException cause) {
      throw Subject
        .get()
        .diagnose("failure", "could not create `%s`", directory)
        .exception(cause);
    }
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
          IOException exception)
          throws IOException
        {
          if (exception != null) {
            throw exception;
          }
          Files.delete(directory);
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException cause) {
      throw Subject
        .get()
        .diagnose("failure", "could not delete `%s`", directory)
        .exception(cause);
    }
  }

  private Persistance() {}
}
