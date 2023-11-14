package duru;

public final class Builder {
  public static void build(Semantic.Target target) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Builder() {}
}
