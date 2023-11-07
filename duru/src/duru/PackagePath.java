package duru;

record PackagePath(NormalPath value) {
  public static Result<PackagePath> of(NormalPath value) {
    return Result
      .perform(() -> Identifier.of(value.value().getFileName().toString()))
      .perform(identifier -> Result.success(new PackagePath(value)));
  }
}
