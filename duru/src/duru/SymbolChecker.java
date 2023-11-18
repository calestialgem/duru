package duru;

public final class SymbolChecker {
  public static Semantic.Symbol check(Node.Declaration declaration) {
    var checker = new SymbolChecker(declaration);
    return checker.check();
  }

  private final Node.Declaration declaration;

  private SymbolChecker(Node.Declaration declaration) {
    this.declaration = declaration;
  }

  private Semantic.Symbol check() {
    throw Subject.unimplemented();
  }
}
