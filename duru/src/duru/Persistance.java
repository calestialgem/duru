package duru;

import java.io.IOException;
import java.nio.file.Files;

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

  private Persistance() {}
}
