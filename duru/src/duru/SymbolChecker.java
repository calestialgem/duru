package duru;

public final class SymbolChecker {
  public static Semantic.Symbol check(
    Accessor<String, Semantic.Symbol> accessor,
    String packageName,
    Node.Declaration declaration)
  {
    var checker = new SymbolChecker(accessor, packageName, declaration);
    return checker.check();
  }

  private final Accessor<String, Semantic.Symbol> accessor;
  private final String                            packageName;
  private final Node.Declaration                  declaration;
  private MapBuffer<String, Semantic.Type>        locals;

  private SymbolChecker(
    Accessor<String, Semantic.Symbol> accessor,
    String packageName,
    Node.Declaration declaration)
  {
    this.accessor    = accessor;
    this.packageName = packageName;
    this.declaration = declaration;
  }

  private Semantic.Symbol check() {
    locals = MapBuffer.create();
    return switch (declaration) {
      case Node.Proc proc -> checkProc(proc);
      case Node.ExternalProc proc -> checkExternalProc(proc);
      case Node.Struct struct -> checkStruct(struct);
    };
  }

  private Semantic.Proc checkProc(Node.Proc node) {
    return new Semantic.Proc(
      packageName,
      node.isPublic(),
      node.name().text(),
      checkParameters(node.parameters()),
      node.returnType().transform(this::checkType),
      checkStatement(node.body()));
  }

  private Semantic.ExternalProc checkExternalProc(Node.ExternalProc node) {
    return new Semantic.ExternalProc(
      packageName,
      node.isPublic(),
      node.name().text(),
      checkParameters(node.parameters()),
      node.returnType().transform(this::checkType),
      node.externalName().toString());
  }

  private Map<String, Semantic.Type> checkParameters(
    List<Node.Parameter> nodes)
  {
    var parameters = MapBuffer.<String, Semantic.Type>create();
    for (var parameter : nodes) {
      var name = parameter.name().text();
      if (parameters.contains(name))
        throw Subject.error("redeclaration of parameter `%s`", name);
      var type = checkType(parameter.type());
      parameters.add(name, type);
      locals.add(name, type);
    }
    return parameters.toMap();
  }

  private Semantic.Struct checkStruct(Node.Struct node) {
    return new Semantic.Struct(
      packageName,
      node.isPublic(),
      node.name().text());
  }

  private Semantic.Type checkType(Node.Formula node) {
    return switch (node) {
      case Node.Pointer pointer ->
        new Semantic.Pointer(checkType(pointer.pointee()));
      case Node.Base base -> {
        var symbol = getSymbol(base.name());
        if (!(symbol instanceof Semantic.Type type)) {
          throw Subject
            .error(
              "`%s.%s` is not a type",
              symbol.packageName(),
              symbol.name());
        }
        yield type;
      }
    };
  }

  private Semantic.Statement checkStatement(Node.Statement node) {
    return switch (node) {
      case Node.Block block -> {
        var innerStatements = ListBuffer.<Semantic.Statement>create();
        for (var innerStatement : block.innerStatements())
          innerStatements.add(checkStatement(innerStatement));
        yield new Semantic.Block(innerStatements.toList());
      }
      case Node.Discard discard ->
        new Semantic.Discard(checkExpression(discard.discarded()));
      case Node.If if_ ->
        new Semantic.If(
          checkExpression(if_.condition()),
          checkStatement(if_.trueBranch()),
          if_.falseBranch().transform(this::checkStatement));
      case Node.Return return_ ->
        new Semantic.Return(return_.value().transform(this::checkExpression));
      case Node.Var var ->
        new Semantic.Var(
          var.name().text(),
          var.type().transform(this::checkType),
          checkExpression(var.initialValue()));
    };
  }

  private Semantic.Expression checkExpression(Node.Expression node) {
    return switch (node) {
      case Node.LessThan lessThan ->
        new Semantic.LessThan(
          checkExpression(lessThan.left()),
          checkExpression(lessThan.right()));
      case Node.Access access -> throw Subject.unimplemented();
      case Node.Invocation invocation -> throw Subject.unimplemented();
      case Node.NaturalConstant naturalConstant ->
        new Semantic.NaturalConstant(naturalConstant.value().value());
      case Node.StringConstant stringConstant ->
        new Semantic.StringConstant(stringConstant.value().value());
    };
  }

  private Semantic.Symbol getSymbol(Node.Mention mention) {
    var string = new StringBuilder();
    for (var subspace : mention.subspaces()) {
      string.append(subspace.text());
      string.append('.');
    }
    string.append(mention.name().text());
    return accessor.access(string.toString());
  }
}
