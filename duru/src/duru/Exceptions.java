package duru;

final class Exceptions {
  public static RuntimeException unimplemented() {
    return new RuntimeException("This feature is not implemented yet!");
  }

  private Exceptions() {}
}
