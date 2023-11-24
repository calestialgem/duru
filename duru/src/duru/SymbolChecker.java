package duru;

import java.util.function.Function;
import java.util.function.BiFunction;

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
    var externalName = Optional.<String>absent();
    if (!declaration.externalName().isEmpty()) {
      var token = declaration.externalName().getFirst();
      if (token.value().indexOf('$') != -1) {
        throw Diagnostic
          .error(token.location(), "illegal character `$` in external name");
      }
      if (!externalNames.add(token.value())) {
        throw Diagnostic
          .error(
            token.location(),
            "redeclaration of external `%s`",
            token.value());
      }
      externalName = Optional.present(token.value());
    }
    var isPublic = declaration.isPublic();
    return switch (declaration) {
      case Node.Using using ->
        new Semantic.Using(
          externalName,
          isPublic,
          symbolName,
          accessGlobal(using.aliased()).name());
      case Node.Struct struct -> {
        var members = MapBuffer.<String, Semantic.Type>create();
        for (var member : struct.members()) {
          var name = member.name().text();
          var type = checkType(member.type());
          if (!members.add(name, type)) {
            throw Diagnostic
              .error(
                member.name().location(),
                "redeclaration of member `%s::%s`",
                symbolName,
                name);
          }
        }
        yield new Semantic.Struct(
          externalName,
          isPublic,
          symbolName,
          members.toMap());
      }
      case Node.Const global -> {
        var type  = checkType(global.type());
        var value = checkExpression(global.value());
        yield new Semantic.Const(
          externalName,
          isPublic,
          symbolName,
          type,
          coerce(global.value().location(), value, type));
      }
      case Node.Var global -> {
        var type         = checkType(global.type());
        var initialValue = checkExpression(global.initialValue());
        yield new Semantic.Var(
          externalName,
          isPublic,
          symbolName,
          type,
          coerce(global.initialValue().location(), initialValue, type));
      }
      case Node.Fn fn -> {
        var parameters = MapBuffer.<String, Semantic.Type>create();
        for (var parameter : fn.parameters()) {
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
        returnType = checkType(fn.returnType());
        if (fn.body().isEmpty()) {
          yield new Semantic.Fn(
            externalName,
            isPublic,
            symbolName,
            parameters.toMap(),
            returnType,
            Optional.absent());
        }
        var checkedBody = checkStatement(fn.body().getFirst());
        if (checkedBody.control() == Control.FLOWS
          && !(returnType instanceof Semantic.Void))
        {
          throw Diagnostic
            .error(
              fn.returnType().location(),
              "function `%s` must return `%s`",
              symbolName,
              returnType);
        }
        yield new Semantic.Fn(
          externalName,
          isPublic,
          symbolName,
          parameters.toMap(),
          returnType,
          Optional.present(checkedBody.statement()));
      }
      default -> throw Diagnostic.unimplemented(declaration.location());
    };
  }

  private Semantic.Type checkType(Node.Formula node) {
    return switch (node) {
      case Node.Pointer pointer ->
        new Semantic.Pointer(checkType(pointer.pointee()));
      case Node.Base base -> {
        var symbol = accessGlobal(base.name());
        if (!(symbol instanceof Semantic.Type type)) {
          throw Diagnostic
            .error(base.name().location(), "`%s` is not type", symbol.name());
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
      case Node.LogicalOr op ->
        checkLogicalOperation(op, Semantic.LogicalOr::new);
      case Node.LogicalAnd op ->
        checkLogicalOperation(op, Semantic.LogicalAnd::new);
      case Node.NotEqualTo op ->
        checkComparisonOperation(op, Semantic.NotEqualTo::new);
      case Node.EqualTo op ->
        checkComparisonOperation(op, Semantic.EqualTo::new);
      case Node.GreaterThanOrEqualTo op ->
        checkComparisonOperation(op, Semantic.GreaterThanOrEqualTo::new);
      case Node.GreaterThan op ->
        checkComparisonOperation(op, Semantic.GreaterThan::new);
      case Node.LessThanOrEqualTo op ->
        checkComparisonOperation(op, Semantic.LessThanOrEqualTo::new);
      case Node.LessThan op ->
        checkComparisonOperation(op, Semantic.LessThan::new);
      case Node.BitwiseOr op ->
        checkBitwiseOperation(op, Semantic.BitwiseOr::new);
      case Node.BitwiseXor op ->
        checkBitwiseOperation(op, Semantic.BitwiseXor::new);
      case Node.BitwiseAnd op ->
        checkBitwiseOperation(op, Semantic.BitwiseAnd::new);
      case Node.RightShift op ->
        checkBitwiseOperation(op, Semantic.RightShift::new);
      case Node.LeftShift op ->
        checkBitwiseOperation(op, Semantic.LeftShift::new);
      case Node.Subtraction op ->
        checkArithmeticOperation(op, Semantic.Subtraction::new);
      case Node.Addition op ->
        checkArithmeticOperation(op, Semantic.Addition::new);
      case Node.Reminder op ->
        checkArithmeticOperation(op, Semantic.Reminder::new);
      case Node.Quotient op ->
        checkArithmeticOperation(op, Semantic.Quotient::new);
      case Node.Multiplication op ->
        checkArithmeticOperation(op, Semantic.Multiplication::new);
      case Node.LogicalNot op ->
        checkLogicalOperation(op, Semantic.LogicalNot::new);
      case Node.BitwiseNot op ->
        checkBitwiseOperation(op, Semantic.BitwiseNot::new);
      case Node.Negation op ->
        checkArithmeticOperation(op, Semantic.Negation::new);
      case Node.Promotion op ->
        checkArithmeticOperation(op, Semantic.Promotion::new);
      case Node.MemberAccess memberAccess ->
        throw Diagnostic.unimplemented(memberAccess.location());
      case Node.InfixCall infixCall ->
        throw Diagnostic.unimplemented(infixCall.location());
      case Node.PostfixCall invocation -> {
        var accessed = accessGlobal(invocation.procedure());
        if (!(accessed instanceof Semantic.Procedure procedure)) {
          throw Diagnostic
            .error(
              invocation.procedure().location(),
              "`%s` is not procedure",
              accessed.name());
        }
        if (invocation.arguments().length()
          != procedure.parameters().length())
        {
          throw Diagnostic
            .error(
              invocation.location(),
              "`%s` takes %d parameters but %d arguments were given",
              accessed.name(),
              procedure.parameters().length(),
              invocation.arguments().length());
        }
        var arguments = ListBuffer.<Semantic.Expression>create();
        for (var i = 0; i < invocation.arguments().length(); i++) {
          var argument  = invocation.arguments().get(i);
          var parameter = procedure.parameters().values().get(i);
          var checked   = checkExpression(argument);
          var coerced   = coerce(argument.location(), checked, parameter);
          arguments.add(coerced);
        }
        yield new CheckedExpression(
          new Semantic.Invocation(accessed.name(), arguments.toList()),
          procedure.returnType());
      }
      case Node.Initialization initialization ->
        throw Diagnostic.unimplemented(initialization.location());
      case Node.Cast cast -> throw Diagnostic.unimplemented(cast.location());
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
        var symbol = accessGlobal(access.mention());
        if (!(symbol instanceof Semantic.GlobalVariable global)) {
          throw Diagnostic
            .error(access.location(), "`%s` is not variable", symbol);
        }
        return new CheckedExpression(
          new Semantic.GlobalAccess(accessed.name()),
          accessed.type());
      }
      case Node.Grouping grouping -> checkExpression(grouping.grouped());
      case Node.NumberConstant numberConstant -> {
        try {
          yield new CheckedExpression(
            new Semantic.IntegralConstant(
              numberConstant.value().value().toBigIntegerExact()),
            CONSTANT_INTEGRAL);
        }
        catch (@SuppressWarnings("unused") ArithmeticException cause) {
          yield new CheckedExpression(
            new Semantic.RealConstant(numberConstant.value().value()),
            CONSTANT_REAL);
        }
      }
      case Node.StringConstant stringConstant ->
        new CheckedExpression(
          new Semantic.StringConstant(stringConstant.value().value()),
          Semantic.BYTE_POINTER);
    };
  }

  private CheckedExpression checkArithmeticOperation(
    Node.UnaryOperator node,
    Function<Semantic.Expression, Semantic.Expression> creator)
  {
    throw Diagnostic.unimplemented(node.location());
  }

  private CheckedExpression checkBitwiseOperation(
    Node.UnaryOperator node,
    Function<Semantic.Expression, Semantic.Expression> creator)
  {
    throw Diagnostic.unimplemented(node.location());
  }

  private CheckedExpression checkLogicalOperation(
    Node.UnaryOperator node,
    Function<Semantic.Expression, Semantic.Expression> creator)
  {
    throw Diagnostic.unimplemented(node.location());
  }

  private CheckedExpression checkArithmeticOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    throw Diagnostic.unimplemented(node.location());
  }

  private CheckedExpression checkBitwiseOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    throw Diagnostic.unimplemented(node.location());
  }

  private CheckedExpression checkComparisonOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    var left  = checkExpression(node.leftOperand());
    var right = checkExpression(node.rightOperand());
    if (!(left.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not arithmetic",
          left.type());
    }
    if (!(right.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.rightOperand().location(),
          "`%s` is not arithmetic",
          right.type());
    }
    if (left.expression() instanceof Semantic.IntegralConstant cl) {
      if (right.expression() instanceof Semantic.IntegralConstant cr) {
        return new CheckedExpression(
          creator
            .apply(
              new Semantic.Natural64Constant(cl.value()),
              new Semantic.Natural64Constant(cr.value())),
          Semantic.BOOLEAN);
      }
      return new CheckedExpression(
        creator
          .apply(
            coerce(node.leftOperand().location(), left, right.type()),
            right.expression()),
        Semantic.BOOLEAN);
    }
    if (right.expression() instanceof Semantic.IntegralConstant cr) {
      return new CheckedExpression(
        creator
          .apply(
            left.expression(),
            coerce(node.rightOperand().location(), right, left.type())),
        Semantic.BOOLEAN);
    }
    if (!left.type().equals(right.type())) {
      throw Diagnostic
        .error(
          node.location(),
          "`%s` and `%s` cannot be operated",
          left.type(),
          right.type());
    }
    return new CheckedExpression(
      creator.apply(left.expression(), right.expression()),
      Semantic.BOOLEAN);
  }

  private CheckedExpression checkLogicalOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    throw Diagnostic.unimplemented(node.location());
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
