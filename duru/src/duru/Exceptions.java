package duru;

public final class Exceptions {
  public static RuntimeException unimplemented() {
    throw new RuntimeException("fatal error: Unimplemented!");
  }

  private Exceptions() {}
}
