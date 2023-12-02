package duru;

public enum Token {
  NO_TOKEN, OPENING_BRACE, CLOSING_BRACE, ENTRYPOINT, IDENTIFIER;

  public boolean is_varying() {
    return length() == 0;
  }

  public int length() {
    switch (this) {
      case OPENING_BRACE -> {
        return "{".length();
      }
      case CLOSING_BRACE -> {
        return "}".length();
      }
      case ENTRYPOINT -> {
        return "entrypoint".length();
      }
      default -> {
        return 0;
      }
    }
  }
}
