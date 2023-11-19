package duru;

public final class PackageChecker {
  public static Map<String, Semantic.Type> check(Resolution resolution) {
    var checker = new PackageChecker(resolution);
    return checker.check();
  }

  private final Resolution resolution;

  private PackageChecker(Resolution resolution) {
    this.resolution = resolution;
  }

  private Map<String, Semantic.Type> check() {
    return resolution.declarations().transformValues(this::checkDeclaration);
  }

  private Semantic.Type checkDeclaration(Node.Declaration declaration) {
    return switch (declaration) {
      case Node.Proc proc ->
        checkCallable(proc.parameters(), proc.returnType());
      case Node.ExternalProc proc ->
        checkCallable(proc.parameters(), proc.returnType());
      case Node.Struct struct ->
        new Semantic.Struct(
          resolution.name(),
          struct.isPublic(),
          struct.name().text());
    };
  }

  private Semantic.Callable checkCallable(
    Map<Token.Identifier, Node.Formula> parameters,
    Optional<Node.Formula> returnType)
  {
    throw Subject.unimplemented();
  }
}
