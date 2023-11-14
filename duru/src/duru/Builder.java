package duru;

public final class Builder {
  public static void build(Semantic.Module module) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private Builder() {}
}
