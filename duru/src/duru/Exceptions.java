package duru;

final class Exceptions {
  public static RuntimeException unimplemented() {
    return new RuntimeException("Unimplemented!");
  }

  private Exceptions() {}
}
