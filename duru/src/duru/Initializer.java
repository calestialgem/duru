package duru;

import java.nio.file.Files;

final class Initializer {
  private static final String CONFIGURATION_NAME = "project.duru";

  public static Result<Void> initialize(NormalPath directory) {
    return Identifier
      .of(directory.value().getFileName().toString())
      .then(name -> initialize(directory, name));
  }

  public static Result<Void> initialize(NormalPath directory, Identifier name) {
    for (var i = directory.value(); i != null; i = i.getParent()) {
      var configuration = i.resolve(CONFIGURATION_NAME);
      if (Files.exists(configuration)) {
        return Result
          .failure(
            "Cannot initialize inside another project, which is defined by `%s`!",
            configuration);
      }
    }
    return Result.failure("Unimplemented!");
  }
}
