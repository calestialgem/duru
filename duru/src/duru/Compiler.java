package duru;

final class Compiler {
  public static Result<Semantic.Target> compile(NormalPath project) {
    return Result
      .failure("Unimplemented `%s`!", new Throwable().getStackTrace()[0]);
  }

  private Compiler() {}
}
