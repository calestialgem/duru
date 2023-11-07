package duru;

import java.util.function.Function;

final class Checker {
  public static Result<Semantic.Package> check(
    Resolution.Package resolution,
    Function<Namespace, Semantic.Package> finder)
  {
    throw Exceptions.unimplemented();
  }

  private Checker() {}
}
