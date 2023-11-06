package duru;

record Identifier(String value) {
  public static Result<Identifier> of(String value) {
    for (var i = 0; i != value.length(); i = value.offsetByCodePoints(i, 1)) {
      var character = value.codePointAt(i);
      if (!isWord(character)) {
        return Result
          .failure(
            "Character `%c` at %d in identifier `%s` is invalid!",
            character,
            i,
            value);
      }
    }

    if (value.isEmpty()) {
      return Result.failure("Identifier is empty!");
    }

    var initial = value.charAt(0);
    if (!isInitial(initial)) {
      return Result
        .failure("Initial `%c` of identifier `%s` is invalid!", initial, value);
    }

    return Result.success(new Identifier(value));
  }

  private static boolean isInitial(int character) {
    return Character.isLetter(character) || character == '_';
  }

  private static boolean isWord(int character) {
    return Character.isLetterOrDigit(character) || character == '_';
  }
}
