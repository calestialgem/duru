package duru;

public enum Node {
  NO_NODE, ENTRYPOINT, BLOCK_BEGIN, BLOCK_END;

  public boolean is_declaration() {
    return this == ENTRYPOINT;
  }
}
