package duru;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class Persistance {
  public static List<Path> list(Object subject, Path directory) {
    try {
      return List.of(Files.list(directory).toArray(Path[]::new));
    }
    catch (IOException cause) {
      throw Diagnostic
        .failure(subject, cause, "cannot list entries of `%s`", directory);
    }
  }

  public static Path path(String path) {
    return Path.of(path).toAbsolutePath().normalize();
  }

  public static void store(Object subject, Path file, Object text) {
    try {
      Files.writeString(file, text.toString());
    }
    catch (IOException cause) {
      throw Diagnostic.failure(subject, cause, "could not write to `%s`", file);
    }
  }

  public static String load(Object subject, Path file) {
    try {
      return Files.readString(file);
    }
    catch (IOException cause) {
      throw Diagnostic
        .failure(subject, cause, "could not read from `%s`", file);
    }
  }

  public static void ensure(Object subject, Path directory) {
    if (Files.exists(directory)) {
      return;
    }
    create(subject, directory);
  }

  public static void recreate(Object subject, Path directory) {
    if (Files.exists(directory)) {
      delete(subject, directory);
    }
    create(subject, directory);
  }

  public static void create(Object subject, Path directory) {
    try {
      Files.createDirectory(directory);
    }
    catch (IOException cause) {
      throw Diagnostic
        .failure(subject, cause, "could not create `%s`", directory);
    }
  }

  private static void delete(Object subject, Path directory) {
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
      throw Diagnostic
        .failure(subject, cause, "could not delete `%s`", directory);
    }
  }

  private Persistance() {}
}
