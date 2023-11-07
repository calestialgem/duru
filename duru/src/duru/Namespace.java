package duru;

import java.util.List;

record Namespace(List<Identifier> scopes) {
  public static Result<Namespace> of(List<Identifier> scopes) {
    if (scopes.isEmpty())
      return Result.failure("Namespace must have at least one scope!");
    return Result.success(new Namespace(List.copyOf(scopes)));
  }
}
