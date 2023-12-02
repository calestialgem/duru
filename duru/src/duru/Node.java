package duru;

public enum Node {
  NO_NODE, ENTRYPOINT, BLOCK_BEGIN, BLOCK_END, IDENTIFIER;

  public boolean is_declaration() {
    return this == ENTRYPOINT;
  }

  public boolean is_varying() {
    return length() == 0;
  }

  public int length() {
    switch (this) {
      case ENTRYPOINT -> {
        return "entrypoint".length();
      }
      case BLOCK_BEGIN -> {
        return "}".length();
      }
      case BLOCK_END -> {
        return "{".length();
      }
      default -> {
        return 0;
      }
    }
  }
}
