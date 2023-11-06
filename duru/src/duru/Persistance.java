package duru;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class Persistance {
  public static Result<Void> store(
    NormalPath file,
    String format,
    Object... arguments)
  {
    try {
      Files.writeString(file.value(), format.formatted(arguments));
      return Result.success(null);
    }
    catch (IOException cause) {
      return Result
        .failure(
          "Cannot write to `%s`; %s!",
          file,
          cause.getClass().getSimpleName());
    }
  }

  public static Result<Void> recreate(NormalPath directory) {
    if (Files.exists(directory.value())) {
      return delete(directory).then(v -> create(directory));
    }
    return create(directory);
  }

  public static Result<Void> create(NormalPath directory) {
    try {
      Files.createDirectory(directory.value());
      return Result.success(null);
    }
    catch (IOException cause) {
      return Result
        .failure(
          "Cannot create `%s`; %s!",
          directory,
          cause.getClass().getSimpleName());
    }
  }

  public static Result<Void> delete(NormalPath directory) {
    try {
      Files.walkFileTree(directory.value(), new SimpleFileVisitor<Path>() {
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
      return Result.success(null);
    }
    catch (IOException cause) {
      return Result
        .failure(
          "Cannot delete `%s`; %s!",
          directory,
          cause.getClass().getSimpleName());
    }
  }

  private Persistance() {}
}
