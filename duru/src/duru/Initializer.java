package duru;

final class Initializer {
  public static Result<Void, String> initialize(NormalPath directory) {
    return Identifier
      .of(directory.value().getFileName().toString())
      .then(name -> initialize(directory, name));
  }

  public static Result<Void, String> initialize(
    NormalPath directory,
    Identifier name)
  {
    return Result.failure("Unimplemented!");
  }
}
