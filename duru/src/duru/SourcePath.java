package duru;

record SourcePath(NormalPath value) {
  public static final String EXTENSION = ".duru";

  public static Result<SourcePath> of(NormalPath value) {
    var fileName = value.value().getFileName().toString();
    if (!fileName.endsWith(EXTENSION)) {
      return Result
        .failure(
          "Source file `%s` does not have the extension `%s`!",
          value,
          EXTENSION);
    }
    var name = fileName.substring(0, fileName.length() - EXTENSION.length());
    return Result
      .perform(() -> Identifier.of(name))
      .perform(identifier -> Result.success(new SourcePath(value)));
  }
}
