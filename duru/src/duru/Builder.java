package duru;

public final class Builder {
  public static void build(Subject subject, Semantic.Module module) {
    throw subject.diagnose("failure", "unimplemented").exception();
  }

  private Builder() {}
}
