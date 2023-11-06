package duru;

final class Builder {
  public static Result<Void> build(Semantic.Target target) {
    return Result.failure("Unimplemented!", new Throwable().getStackTrace()[0]);
  }

  private Builder() {}
}
