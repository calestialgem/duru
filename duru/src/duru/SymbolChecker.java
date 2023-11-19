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
  private Semantic.Type                           returnType;

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
    returnType =
      node
        .returnType()
        .transform(this::checkType)
        .getOrElse(Semantic.Unit::new);
    var body = checkStatement(node.body());
    if (!(returnType instanceof Semantic.Unit)
      && body.control() == Control.FLOWS)
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
      node
        .returnType()
        .transform(this::checkType)
        .getOrElse(Semantic.Unit::new),
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
          control = control.sequent(checkedStatement.control());
          innerStatements.add(checkedStatement.statement());
        }
        locals.removeDownTo(scope);
        yield new CheckedStatement(
          new Semantic.Block(innerStatements.toList()),
          control);
      }
      case Node.Discard discard -> {
        var discarded = checkExpression(discard.discarded());
        yield new CheckedStatement(
          new Semantic.Discard(discarded.expression()),
          discarded.type() instanceof Semantic.Noreturn
            ? Control.SINKS
            : Control.FLOWS);
      }
      case Node.If if_ -> {
        var scope     = locals.length();
        var condition = checkExpression(if_.condition());
        if (!(condition.type() instanceof Semantic.Boolean))
          throw Subject.error("if statement's condition must be a boolean");
        var trueBranchScope = locals.length();
        var trueBranch      = checkStatement(if_.trueBranch());
        locals.removeDownTo(trueBranchScope);
        var falseBranchScope = locals.length();
        var falseBranch      =
          if_.falseBranch().transform(this::checkStatement);
        locals.removeDownTo(falseBranchScope);
        var control =
          trueBranch
            .control()
            .branch(
              falseBranch
                .transform(CheckedStatement::control)
                .getOrElse(Control.FLOWS));
        locals.removeDownTo(scope);
        yield new CheckedStatement(
          new Semantic.If(
            condition.expression(),
            trueBranch.statement(),
            falseBranch.transform(CheckedStatement::statement)),
          control);
      }
      case Node.Return return_ -> {
        var value = return_.value().transform(this::checkExpression);
        if (!value
          .transform(CheckedExpression::type)
          .getOrElse(Semantic.Unit::new)
          .equals(returnType))
          throw Subject
            .error(
              "procedure `%s`s return type does not match the return value",
              symbolName);
        yield new CheckedStatement(
          new Semantic.Return(value.transform(CheckedExpression::expression)),
          Control.RETURNS);
      }
      case Node.Var var -> {
        var initialValue   = checkExpression(var.initialValue());
        var typeAnnotation = var.type().transform(this::checkType);
        var name           = var.name().text();
        var type           = initialValue.type();
        if (!typeAnnotation.isEmpty()
          && !typeAnnotation.getFirst().equals(type))
        {
          throw Subject
            .error(
              "variable `%s.%s`s type annotation does not match the initial value",
              symbolName,
              name);
        }
        if (type instanceof Semantic.Noreturn)
          throw Subject
            .error("variable `%s.%s`s type is noreturn", symbolName, name);
        locals.add(name, type);
        yield new CheckedStatement(
          new Semantic.Var(name, type, initialValue.expression()),
          Control.FLOWS);
      }
    };
  }

  private CheckedExpression checkExpression(Node.Expression node) {
    return switch (node) {
      case Node.LessThan binary -> {
        var left  = checkExpression(binary.left());
        var right = checkExpression(binary.right());
        if (!(left.type() instanceof Semantic.Arithmetic))
          throw Subject
            .error("binary operator's left operand is not arithmetic");
        if (!(right.type() instanceof Semantic.Arithmetic))
          throw Subject
            .error("binary operator's left operand is not arithmetic");
        if (!left.type().equals(right.type()))
          throw Subject.error("binary operator's operands are not the same");
        yield new CheckedExpression(
          new Semantic.LessThan(left.expression(), right.expression()),
          left.type());
      }
      case Node.Access access -> {
        if (access.mention().subspaces().isEmpty()) {
          var name  = access.mention().name().text();
          var local = locals.get(name);
          if (!local.isEmpty()) {
            yield new CheckedExpression(
              new Semantic.LocalAccess(name),
              local.getFirst());
          }
        }
        accessGlobal(access.mention());
        throw Subject.unimplemented();
      }
      case Node.Invocation invocation -> {
        var accessed = accessGlobal(invocation.procedure());
        if (!(accessed instanceof Semantic.Procedure procedure))
          throw Subject
            .error("invoked symbol `%s` is not a procedure", accessed.name());
        if (invocation.arguments().length() != procedure.parameters().length())
          throw Subject
            .error(
              "invoked procedure `%s` takes %d parameters but %d arguments were given",
              accessed.name(),
              procedure.parameters().length(),
              invocation.arguments().length());
        var arguments = ListBuffer.<Semantic.Expression>create();
        for (var i = 0; i < invocation.arguments().length(); i++) {
          var argument = checkExpression(invocation.arguments().get(i));
          if (!argument.type().equals(procedure.parameters().get(i).value()))
            throw Subject
              .error(
                "passed argument's type does not match the parameter `%s.%s`",
                accessed.name(),
                procedure.parameters().get(i).key());
          arguments.add(argument.expression());
        }
        yield new CheckedExpression(
          new Semantic.Invocation(accessed.name(), arguments.toList()),
          procedure.returnType());
      }
      case Node.NaturalConstant naturalConstant -> {
        var value = naturalConstant.value().value();
        if (Long.compareUnsigned(value, -1) >= 0)
          throw Subject.unimplemented();
        yield new CheckedExpression(
          new Semantic.NaturalConstant(value),
          new Semantic.Natural32());
      }
      case Node.StringConstant stringConstant ->
        new CheckedExpression(
          new Semantic.StringConstant(stringConstant.value().value()),
          new Semantic.Pointer(new Semantic.Byte()));
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
