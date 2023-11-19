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
  private String                                  symbolName;
  private MapBuffer<String, Semantic.Type>        locals;
  private Optional<Semantic.Type>                 returnType;

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
    symbolName = "%s.%s".formatted(packageName, declaration.name().text());
    locals     = MapBuffer.create();
    return switch (declaration) {
      case Node.Proc proc -> checkProc(proc);
      case Node.ExternalProc proc -> checkExternalProc(proc);
      case Node.Struct struct -> checkStruct(struct);
    };
  }

  private Semantic.Proc checkProc(Node.Proc node) {
    var parameters = checkParameters(node.parameters());
    returnType = node.returnType().transform(this::checkType);
    var body = checkStatement(node.body());
    if (!returnType.isEmpty() && body.control() == Control.FLOWS)
      throw Subject.error("procedure `%s` must return a value", symbolName);
    return new Semantic.Proc(
      node.isPublic(),
      symbolName,
      parameters,
      returnType,
      body.statement());
  }

  private Semantic.ExternalProc checkExternalProc(Node.ExternalProc node) {
    return new Semantic.ExternalProc(
      node.isPublic(),
      symbolName,
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
        throw Subject
          .error("redeclaration of parameter `%s.%s`", symbolName, name);
      var type = checkType(parameter.type());
      parameters.add(name, type);
      locals.add(name, type);
    }
    return parameters.toMap();
  }

  private Semantic.Struct checkStruct(Node.Struct node) {
    return new Semantic.Struct(node.isPublic(), symbolName);
  }

  private Semantic.Type checkType(Node.Formula node) {
    return switch (node) {
      case Node.Pointer pointer ->
        new Semantic.Pointer(checkType(pointer.pointee()));
      case Node.Base base -> {
        var symbol = accessGlobal(base.name());
        if (!(symbol instanceof Semantic.Type type)) {
          throw Subject.error("`%s` is not a type", symbol.name());
        }
        yield type;
      }
    };
  }

  private CheckedStatement checkStatement(Node.Statement node) {
    return switch (node) {
      case Node.Block block -> {
        var scope           = locals.length();
        var control         = Control.FLOWS;
        var innerStatements = ListBuffer.<Semantic.Statement>create();
        for (var innerStatement : block.innerStatements()) {
          var checkedStatement = checkStatement(innerStatement);
          control =
            Control.combineSequents(control, checkedStatement.control());
          innerStatements.add(checkedStatement.statement());
        }
        locals.removeDownTo(scope);
        yield new CheckedStatement(
          new Semantic.Block(innerStatements.toList()),
          control);
      }
      case Node.Discard discard -> {
        var discarded = checkExpression(discard.discarded());
        // TODO: Check for calling a noreturn procedure!
        yield new CheckedStatement(
          new Semantic.Discard(discarded),
          Control.FLOWS);
      }
      case Node.If if_ -> {
        var scope           = locals.length();
        var control         = Control.FLOWS;
        var condition       = checkExpression(if_.condition());
        var trueBranchScope = locals.length();
        var trueBranch      = checkStatement(if_.trueBranch());
        locals.removeDownTo(trueBranchScope);
        var falseBranchScope = locals.length();
        var falseBranch      =
          if_.falseBranch().transform(this::checkStatement);
        locals.removeDownTo(falseBranchScope);
        if (!falseBranch.isEmpty()) {
          control =
            Control
              .combineBranches(
                trueBranch.control(),
                falseBranch.getFirst().control());
        }
        locals.removeDownTo(scope);
        yield new CheckedStatement(
          new Semantic.If(
            condition,
            trueBranch.statement(),
            falseBranch.transform(CheckedStatement::statement)),
          control);
      }
      case Node.Return return_ -> {
        // TODO: Check for calling a noreturn procedure!
        // TODO: Check the return type!
        var value = return_.value().transform(this::checkExpression);
        if (returnType.isEmpty() && !value.isEmpty())
          throw Subject
            .error("procedure `%s` cannot return a value", symbolName);
        if (!returnType.isEmpty() && value.isEmpty())
          throw Subject.error("procedure `%s` must return a value", symbolName);
        yield new CheckedStatement(new Semantic.Return(value), Control.RETURNS);
      }
      case Node.Var var -> {
        // TODO: Check for calling a noreturn procedure!
        // TODO: Check the variable type!
        // TODO: Add variable to locals!
        var initialValue = checkExpression(var.initialValue());
        yield new CheckedStatement(
          new Semantic.Var(
            var.name().text(),
            var.type().transform(this::checkType),
            initialValue),
          Control.FLOWS);
      }
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

  private Semantic.Symbol accessGlobal(Node.Mention mention) {
    var string = new StringBuilder();
    for (var subspace : mention.subspaces()) {
      string.append(subspace.text());
      string.append('.');
    }
    string.append(mention.name().text());
    return accessor.access(string.toString());
  }
}
