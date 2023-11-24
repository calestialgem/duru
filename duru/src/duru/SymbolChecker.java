package duru;

public final class SymbolChecker {
  public static Semantic.Symbol check(
    SetBuffer<String> externalNames,
    Accessor<Name, Semantic.Symbol> accessor,
    Name packageName,
    Node.Declaration declaration)
  {
    var checker =
      new SymbolChecker(externalNames, accessor, packageName, declaration);
    return checker.check();
  }

  private final SetBuffer<String>               externalNames;
  private final Accessor<Name, Semantic.Symbol> accessor;
  private final Name                            packageName;
  private final Node.Declaration                declaration;
  private Name                                  symbolName;
  private MapBuffer<String, Semantic.Type>      locals;
  private Semantic.Type                         returnType;

  private SymbolChecker(
    SetBuffer<String> externalNames,
    Accessor<Name, Semantic.Symbol> accessor,
    Name packageName,
    Node.Declaration declaration)
  {
    this.externalNames = externalNames;
    this.accessor      = accessor;
    this.packageName   = packageName;
    this.declaration   = declaration;
  }

  private Semantic.Symbol check() {
    symbolName = packageName.scope(declaration.name().text());
    locals     = MapBuffer.create();
    return switch (declaration) {
      case Node.Type struct -> checkStruct(struct);
      case Node.Fn proc -> checkProc(proc);
      default -> throw Diagnostic.unimplemented(declaration.location());
    };
  }

  private Semantic.Proc checkProc(Node.Fn node) {
    var externalName = checkExternalName(node.externalName());
    var isPublic     = node.isPublic();
    var parameters   = checkParameters(node.parameters());
    returnType = checkType(node.returnType());
    for (var bodyNode : node.body()) {
      var checkedBody = checkStatement(bodyNode);
      var body        = checkedBody.statement();
      if (checkedBody.control() == Control.FLOWS) {
        if (!(returnType instanceof Semantic.Unit)) {
          throw Diagnostic
            .error(
              node.returnType().location(),
              "procedure `%s` must return a `%s`",
              symbolName,
              returnType);
        }
        if (body instanceof Semantic.Block block) {
          var innerStatements = ListBuffer.<Semantic.Statement>create();
          innerStatements.addAll(block.innerStatements());
          innerStatements.add(Semantic.UNIT_RETURN);
          body = new Semantic.Block(innerStatements.toList());
        }
        else {
          body = new Semantic.Block(List.of(body, Semantic.UNIT_RETURN));
        }
      }
      return new Semantic.Proc(
        externalName,
        isPublic,
        symbolName,
        parameters,
        returnType,
        Optional.present(body));
    }
    return new Semantic.Proc(
      externalName,
      isPublic,
      symbolName,
      parameters,
      returnType,
      Optional.absent());
  }

  private Map<String, Semantic.Type> checkParameters(List<Node.Binding> nodes) {
    var parameters = MapBuffer.<String, Semantic.Type>create();
    for (var parameter : nodes) {
      var name = parameter.name().text();
      var type = checkType(parameter.type());
      if (!parameters.add(name, type) || !locals.add(name, type)) {
        throw Diagnostic
          .error(
            parameter.name().location(),
            "redeclaration of parameter `%s.%s`",
            symbolName,
            name);
      }
    }
    return parameters.toMap();
  }

  private Semantic.Struct checkStruct(Node.Type node) {
    return new Semantic.Struct(
      checkExternalName(node.externalName()),
      node.isPublic(),
      symbolName);
  }

  private Optional<String> checkExternalName(
    Optional<Token.StringConstant> token)
  {
    for (var externalName : token) {
      var value = externalName.value();
      if (value.indexOf('$') != -1) {
        throw Diagnostic
          .error(
            externalName.location(),
            "illegal character `$` in external name");
      }
      if (!externalNames.add(value)) {
        throw Diagnostic
          .error(
            externalName.location(),
            "redeclaration of external `%s`",
            value);
      }
      return Optional.present(value);
    }
    return Optional.absent();
  }

  private Semantic.Type checkType(Node.Formula node) {
    return switch (node) {
      case Node.Pointer pointer ->
        new Semantic.Pointer(checkType(pointer.pointee()));
      case Node.Base base -> {
        var symbol = accessGlobal(base.name());
        if (!(symbol instanceof Semantic.Type type)) {
          throw Diagnostic
            .error(base.name().location(), "`%s` is not a type", symbol.name());
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
        var discarded = checkExpression(discard.source());
        yield new CheckedStatement(
          new Semantic.Discard(discarded.expression()),
          discarded.type() instanceof Semantic.Noreturn
            ? Control.SINKS
            : Control.FLOWS);
      }
      case Node.If if_ -> {
        var scope            = locals.length();
        var checkedCondition = checkExpression(if_.condition());
        var condition        =
          coerce(
            if_.condition().location(),
            checkedCondition,
            Semantic.BOOLEAN);
        var trueBranchScope  = locals.length();
        var trueBranch       = checkStatement(if_.trueBranch());
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
            condition,
            trueBranch.statement(),
            falseBranch.transform(CheckedStatement::statement)),
          control);
      }
      case Node.Return return_ -> {
        var value = return_.value().transform(this::checkExpression);
        if (value.isEmpty() && !(returnType instanceof Semantic.Unit)) {
          throw Diagnostic
            .error(
              return_.location(),
              "procedure `%s` must return `%s`",
              symbolName,
              returnType);
        }
        yield new CheckedStatement(
          new Semantic.Return(
            value
              .transform(
                v -> coerce(return_.value().getFirst(), v, returnType))),
          Control.RETURNS);
      }
      case Node.Declare var -> {
        var name           = var.name().text();
        var typeAnnotation = var.type().transform(this::checkType);
        var initialValue   = checkExpression(var.initialValue());
        var type           = typeAnnotation.getOrElse(initialValue::type);
        if (type instanceof Semantic.Noreturn) {
          throw Diagnostic
            .error(
              var.initialValue().location(),
              "`%s.%s` cannot be `duru.Noreturn`",
              symbolName,
              name);
        }
        if (!locals.add(name, type)) {
          throw Diagnostic
            .error(
              var.name().location(),
              "redeclaration of local `%s.%s`",
              symbolName,
              name);
        }
        yield new CheckedStatement(
          new Semantic.Var(
            name,
            type,
            coerce(var.initialValue().location(), initialValue, type)),
          Control.FLOWS);
      }
      default -> throw Diagnostic.unimplemented(node.location());
    };
  }

  private CheckedExpression checkExpression(Node.Expression node) {
    return switch (node) {
      case Node.LessThan binary -> {
        var left  = checkExpression(binary.leftOperand());
        var right = checkExpression(binary.rightOperand());
        if (!(left.type() instanceof Semantic.Arithmetic)) {
          throw Diagnostic
            .error(
              binary.leftOperand().location(),
              "`%s` is not arithmetic",
              left.type());
        }
        if (!(right.type() instanceof Semantic.Arithmetic)) {
          throw Diagnostic
            .error(
              binary.rightOperand().location(),
              "`%s` is not arithmetic",
              right.type());
        }
        if (left.expression() instanceof Semantic.IntegralConstant cl) {
          if (right.expression() instanceof Semantic.IntegralConstant cr) {
            yield new CheckedExpression(
              new Semantic.LessThan(
                new Semantic.Natural64Constant(cl.value()),
                new Semantic.Natural64Constant(cr.value())),
              Semantic.BOOLEAN);
          }
          yield new CheckedExpression(
            new Semantic.LessThan(
              coerce(binary.leftOperand().location(), left, right.type()),
              right.expression()),
            Semantic.BOOLEAN);
        }
        if (right.expression() instanceof Semantic.IntegralConstant cr) {
          yield new CheckedExpression(
            new Semantic.LessThan(
              left.expression(),
              coerce(binary.rightOperand().location(), right, left.type())),
            Semantic.BOOLEAN);
        }
        if (!left.type().equals(right.type())) {
          throw Diagnostic
            .error(
              binary.location(),
              "`%s` and `%s` cannot be operated",
              left.type(),
              right.type());
        }
        yield new CheckedExpression(
          new Semantic.LessThan(left.expression(), right.expression()),
          Semantic.BOOLEAN);
      }
      case Node.Access access -> {
        if (access.mention().identifiers().length() == 1) {
          var name  = access.mention().identifiers().getFirst().text();
          var local = locals.get(name);
          if (!local.isEmpty()) {
            yield new CheckedExpression(
              new Semantic.LocalAccess(name),
              local.getFirst());
          }
        }
        accessGlobal(access.mention());
        throw Diagnostic.unimplemented(access.location());
      }
      // case Node.PostfixCall invocation -> {
      // var accessed = accessGlobal(invocation.procedure());
      // if (!(accessed instanceof Semantic.Procedure procedure)) {
      // throw Diagnostic
      // .error(
      // invocation.procedure().location(),
      // "`%s` is not procedure",
      // accessed.name());
      // }
      // if (invocation.arguments().length()
      // != procedure.parameters().length())
      // {
      // throw Diagnostic
      // .error(
      // invocation.location(),
      // "`%s` takes %d parameters but %d arguments were given",
      // accessed.name(),
      // procedure.parameters().length(),
      // invocation.arguments().length());
      // }
      // var arguments = ListBuffer.<Semantic.Expression>create();
      // for (var i = 0; i < invocation.arguments().length(); i++) {
      // var argument = invocation.arguments().get(i);
      // var parameter = procedure.parameters().values().get(i);
      // var checked = checkExpression(argument);
      // var coerced = coerce(argument.location(), checked, parameter);
      // arguments.add(coerced);
      // }
      // yield new CheckedExpression(
      // new Semantic.Invocation(accessed.name(), arguments.toList()),
      // procedure.returnType());
      // }
      case Node.NumberConstant naturalConstant -> {
        try {
          yield new CheckedExpression(
            new Semantic.IntegralConstant(
              naturalConstant.value().value().toBigIntegerExact()),
            new Semantic.ConstantIntegral());
        }
        catch (@SuppressWarnings("unused") ArithmeticException cause) {
          throw Diagnostic
            .error(
              naturalConstant.location(),
              "non-integral constants are not implemented yet");
        }
      }
      case Node.StringConstant stringConstant ->
        new CheckedExpression(
          new Semantic.StringConstant(stringConstant.value().value()),
          new Semantic.Pointer(new Semantic.Byte()));
      default -> throw Diagnostic.unimplemented(node.location());
    };
  }

  private Semantic.Symbol accessGlobal(Node.Mention mention) {
    return accessor.access(mention.location(), mention.toName());
  }

  private Semantic.Expression coerce(
    Object subject,
    CheckedExpression raw,
    Semantic.Type target)
  {
    if (raw.type().coerces(target)) {
      return raw.expression();
    }
    if (!(raw.expression() instanceof Semantic.IntegralConstant constant)) {
      throw Diagnostic
        .error(subject, "`%s` cannot coerce to `%s`", raw.type(), target);
    }
    if (!(target instanceof Semantic.Integral type)) {
      throw Diagnostic
        .error(
          subject,
          "constant integral `%s` cannot coerce to `%s`",
          constant.value(),
          target);
    }
    if (!type.canRepresent(constant.value())) {
      throw Diagnostic
        .error(subject, "`%s` cannot represent `%s`", type, constant.value());
    }
    return type.constant(constant.value());
  }
}
