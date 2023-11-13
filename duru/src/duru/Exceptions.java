package duru;

public final class Exceptions {
  public static RuntimeException unimplemented() {
    throw new RuntimeException("Unimplemented!");
  }

  private Exceptions() {}
}
