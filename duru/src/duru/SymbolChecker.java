package duru;

import java.util.function.BiFunction;
import java.util.function.Function;

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
  private ListBuffer<Optional<String>>          loops;

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
    loops      = ListBuffer.create();
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
      case Node.If if_ -> {
        var scope                           = locals.length();
        var checkedInitializationStatements =
          if_.initializationStatements().transform(this::checkStatement);
        var control                         =
          Control
            .sequence(
              checkedInitializationStatements
                .transform(CheckedStatement::control));
        var checkedCondition                = checkExpression(if_.condition());
        var condition                       =
          coerce(
            if_.condition().location(),
            checkedCondition,
            Semantic.BOOLEAN);
        var trueBranchScope                 = locals.length();
        var trueBranch                      = checkStatement(if_.trueBranch());
        locals.removeDownTo(trueBranchScope);
        var falseBranchScope = locals.length();
        var falseBranch      =
          if_.falseBranch().transform(this::checkStatement);
        locals.removeDownTo(falseBranchScope);
        control =
          control
            .sequent(
              trueBranch
                .control()
                .branch(
                  falseBranch
                    .transform(CheckedStatement::control)
                    .getOrElse(Control.FLOWS)));
        locals.removeDownTo(scope);
        yield new CheckedStatement(
          new Semantic.If(
            checkedInitializationStatements
              .transform(CheckedStatement::statement),
            condition,
            trueBranch.statement(),
            falseBranch.transform(CheckedStatement::statement)),
          control);
      }
      case Node.For for_ -> {
        var scope = locals.length();
        if (for_.label().isEmpty()) {
          loops.add(Optional.absent());
        }
        else {
          var label = for_.label().getFirst();
          for (var otherLoop : loops) {
            if (otherLoop.isEmpty()) {
              continue;
            }
            if (otherLoop.getFirst().equals(label.text())) {
              throw Diagnostic
                .error(
                  label.location(),
                  "redeclaration of loop `%s`",
                  label.text());
            }
          }
          loops.add(Optional.present(label.text()));
        }
        var checkedInitializationStatements =
          for_.initializationStatements().transform(this::checkStatement);
        var control                         =
          Control
            .sequence(
              checkedInitializationStatements
                .transform(CheckedStatement::control));
        var checkedCondition                = checkExpression(for_.condition());
        var condition                       =
          coerce(
            for_.condition().location(),
            checkedCondition,
            Semantic.BOOLEAN);
        var interleavedStatement            =
          for_.interleavedStatement().transform(this::checkStatement);
        var loopBranchScope                 = locals.length();
        var loopBranch                      = checkStatement(for_.loopBranch());
        locals.removeDownTo(loopBranchScope);
        var falseBranchScope = locals.length();
        var falseBranch      =
          for_.falseBranch().transform(this::checkStatement);
        locals.removeDownTo(falseBranchScope);
        var falseBranchControl =
          falseBranch
            .transform(CheckedStatement::control)
            .getOrElse(Control.FLOWS);
        control =
          control
            .sequent(
              loopBranch.control().unloop(false).branch(falseBranchControl));
        loops.removeLast();
        locals.removeDownTo(scope);
        yield new CheckedStatement(
          new Semantic.For(
            for_.label().transform(Token.Identifier::text),
            checkedInitializationStatements
              .transform(CheckedStatement::statement),
            condition,
            interleavedStatement.transform(CheckedStatement::statement),
            loopBranch.statement(),
            falseBranch.transform(CheckedStatement::statement)),
          control);
      }
      case Node.Break break_ ->
        new CheckedStatement(
          new Semantic.Break(findLoop(break_.location(), break_.label())),
          Control.BREAKS);
      case Node.Continue continue_ ->
        new CheckedStatement(
          new Semantic.Continue(
            findLoop(continue_.location(), continue_.label())),
          Control.CONTINUES);
      case Node.Return return_ -> {
        var value = return_.value().transform(this::checkExpression);
        if (value.isEmpty() && !(returnType instanceof Semantic.Void)) {
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
      case Node.Declare declare -> {
        var name           = declare.name().text();
        var typeAnnotation = declare.type().transform(this::checkType);
        var initialValue   = checkExpression(declare.initialValue());
        var type           = typeAnnotation.getOrElse(initialValue::type);
        if (type instanceof Semantic.Noreturn) {
          throw Diagnostic
            .error(
              declare.initialValue().location(),
              "`%s.%s` cannot be `duru.Noreturn`",
              symbolName,
              name);
        }
        if (!locals.add(name, type)) {
          throw Diagnostic
            .error(
              declare.name().location(),
              "redeclaration of local `%s.%s`",
              symbolName,
              name);
        }
        yield new CheckedStatement(
          new Semantic.Declare(
            name,
            type,
            coerce(declare.initialValue().location(), initialValue, type)),
          Control.FLOWS);
      }
      case Node.Discard discard -> {
        var discarded = checkExpression(discard.source());
        yield new CheckedStatement(
          new Semantic.Discard(discarded.expression()),
          discarded.type() instanceof Semantic.Noreturn
            ? Control.SINKS
            : Control.FLOWS);
      }
      case Node.Increment increment ->
        throw Diagnostic.unimplemented(node.location());
      case Node.Decrement decrement ->
        throw Diagnostic.unimplemented(node.location());
      case Node.Assign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.MultiplyAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.QuotientAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.ReminderAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.AddAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.SubtractAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.ShiftLeftAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.ShiftRightAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.AndAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.XorAssign assign ->
        throw Diagnostic.unimplemented(node.location());
      case Node.OrAssign assign ->
        throw Diagnostic.unimplemented(node.location());
    };
  }

  private Optional<String> findLoop(
    Location fallback,
    Optional<Token.Identifier> optionalLabel)
  {
    for (var label : optionalLabel) {
      for (var index = loops.length(); index != 0; index--) {
        var loop = loops.get(index);
        if (loop.isEmpty()) {
          continue;
        }
        if (loop.getFirst().equals(label.text())) {
          return Optional.present(label.text());
        }
      }
      throw Diagnostic
        .error(label.location(), "there is no loop labeled `%s`", label.text());
    }
    if (loops.isEmpty()) {
      throw Diagnostic.error(fallback, "there is no loop");
    }
    return Optional.absent();
  }

  private CheckedExpression checkExpression(Node.Expression node) {
    switch (node) {
      case Node.LogicalOr op -> {
        return checkLogicalOperation(op, Semantic.LogicalOr::new);
      }
      case Node.LogicalAnd op -> {
        return checkLogicalOperation(op, Semantic.LogicalAnd::new);
      }
      case Node.NotEqualTo op -> {
        return checkComparisonOperation(op, Semantic.NotEqualTo::new);
      }
      case Node.EqualTo op -> {
        return checkComparisonOperation(op, Semantic.EqualTo::new);
      }
      case Node.GreaterThanOrEqualTo op -> {
        return checkComparisonOperation(op, Semantic.GreaterThanOrEqualTo::new);
      }
      case Node.GreaterThan op -> {
        return checkComparisonOperation(op, Semantic.GreaterThan::new);
      }
      case Node.LessThanOrEqualTo op -> {
        return checkComparisonOperation(op, Semantic.LessThanOrEqualTo::new);
      }
      case Node.LessThan op -> {
        return checkComparisonOperation(op, Semantic.LessThan::new);
      }
      case Node.BitwiseOr op -> {
        return checkBitwiseOperation(op, Semantic.BitwiseOr::new);
      }
      case Node.BitwiseXor op -> {
        return checkBitwiseOperation(op, Semantic.BitwiseXor::new);
      }
      case Node.BitwiseAnd op -> {
        return checkBitwiseOperation(op, Semantic.BitwiseAnd::new);
      }
      case Node.RightShift op -> {
        return checkBitwiseOperation(op, Semantic.RightShift::new);
      }
      case Node.LeftShift op -> {
        return checkBitwiseOperation(op, Semantic.LeftShift::new);
      }
      case Node.Subtraction op -> {
        return checkArithmeticOperation(op, Semantic.Subtraction::new);
      }
      case Node.Addition op -> {
        return checkArithmeticOperation(op, Semantic.Addition::new);
      }
      case Node.Reminder op -> {
        return checkArithmeticOperation(op, Semantic.Reminder::new);
      }
      case Node.Quotient op -> {
        return checkArithmeticOperation(op, Semantic.Quotient::new);
      }
      case Node.Multiplication op -> {
        return checkArithmeticOperation(op, Semantic.Multiplication::new);
      }
      case Node.LogicalNot op -> {
        return checkLogicalOperation(op, Semantic.LogicalNot::new);
      }
      case Node.BitwiseNot op -> {
        return checkBitwiseOperation(op, Semantic.BitwiseNot::new);
      }
      case Node.Negation op -> {
        return checkArithmeticOperation(op, Semantic.Negation::new);
      }
      case Node.Promotion op -> {
        return checkArithmeticOperation(op, Semantic.Promotion::new);
      }
      case Node.MemberAccess memberAccess -> {
        var object = checkExpression(memberAccess.object());
        if (!(object.type() instanceof Semantic.Struct struct)) {
          throw Diagnostic
            .error(
              memberAccess.object().location(),
              "`%s` is not struct",
              object.type());
        }
        var type = struct.members().get(memberAccess.member().text());
        if (type.isEmpty()) {
          throw Diagnostic
            .error(
              memberAccess.member().location(),
              "`%s` does not have member `%s`",
              struct,
              memberAccess.member().text());
        }
        return new CheckedExpression(
          new Semantic.MemberAccess(
            object.expression(),
            memberAccess.member().text()),
          type.getFirst());
      }
      case Node.InfixCall infixCall -> {
        var arguments = ListBuffer.<Node.Expression>create();
        arguments.add(infixCall.firstArgument());
        arguments.addAll(infixCall.remainingArguments());
        return checkCall(
          infixCall.location(),
          infixCall.callee(),
          arguments.toList());
      }
      case Node.PostfixCall postfixCall -> {
        return checkCall(
          postfixCall.location(),
          postfixCall.callee(),
          postfixCall.arguments());
      }
      case Node.Initialization initialization -> {
        var initialized = accessGlobal(initialization.type());
        if (!(initialized instanceof Semantic.Struct struct)) {
          throw Diagnostic
            .error(
              initialization.type().location(),
              "`%s` is not struct",
              initialized);
        }
        var memberInitializations =
          MapBuffer.<String, Semantic.Expression>create();
        for (var member : initialization.memberInitializations()) {
          var name = member.member().text();
          var type = struct.members().get(name);
          if (type.isEmpty()) {
            throw Diagnostic
              .error(
                member.member().location(),
                "member `%s::%s` does not exist",
                struct,
                name);
          }
          if (memberInitializations.contains(name)) {
            throw Diagnostic
              .error(
                member.member().location(),
                "reinitialization of `%s::%s`",
                struct,
                name);
          }
          var value =
            coerce(
              member.value().location(),
              checkExpression(member.value()),
              type.getFirst());
          memberInitializations.add(name, value);
        }
        var members = ListBuffer.<Semantic.Expression>create();
        for (var member : struct.members().keys()) {
          var value = memberInitializations.get(member);
          members.add(value.getOrElse(Semantic.ZERO));
        }
        return new CheckedExpression(
          new Semantic.Initialization(struct, members.toList()),
          struct);
      }
      case Node.Cast cast -> {
        var source = checkExpression(cast.source());
        var target = checkType(cast.target());
        if (source.type() instanceof Semantic.Arithmetic
          && target instanceof Semantic.Arithmetic)
        {
          return new CheckedExpression(
            new Semantic.Conversion(source.expression(), target),
            target);
        }
        return new CheckedExpression(
          coerce(cast.target().location(), source, target),
          target);
      }
      case Node.Access access -> {
        if (access.mention().identifiers().length() == 1) {
          var name  = access.mention().identifiers().getFirst().text();
          var local = locals.get(name);
          if (!local.isEmpty()) {
            return new CheckedExpression(
              new Semantic.LocalAccess(name),
              local.getFirst());
          }
        }
        var symbol = accessGlobal(access.mention());
        return switch (symbol) {
          case Semantic.Const const_ ->
            new CheckedExpression(
              new Semantic.GlobalAccess(const_.name()),
              const_.type());
          case Semantic.Var var_ ->
            new CheckedExpression(
              new Semantic.GlobalAccess(var_.name()),
              var_.type());
          case Semantic.Fn fn ->
            new CheckedExpression(
              new Semantic.GlobalAccess(fn.name()),
              new Semantic.Callable(
                fn.parameters().transform(Entry::value),
                fn.returnType()));
          default ->
            throw Diagnostic
              .error(access.location(), "`%s` is not variable", symbol);
        };
      }
      case Node.Grouping grouping -> {
        return checkExpression(grouping.grouped());
      }
      case Node.NumberConstant numberConstant -> {
        try {
          return new CheckedExpression(
            new Semantic.IntegralConstant(
              numberConstant.value().value().toBigIntegerExact()),
            Semantic.CONSTANT_INTEGRAL);
        }
        catch (@SuppressWarnings("unused") ArithmeticException cause) {
          return new CheckedExpression(
            new Semantic.RealConstant(numberConstant.value().value()),
            Semantic.CONSTANT_REAL);
        }
      }
      case Node.StringConstant stringConstant -> {
        return new CheckedExpression(
          new Semantic.StringConstant(stringConstant.value().value()),
          Semantic.CONSTANT_STRING);
      }
    }
  }

  private CheckedExpression checkArithmeticOperation(
    Node.UnaryOperator node,
    Function<Semantic.Expression, Semantic.Expression> creator)
  {
    var operand = checkExpression(node.operand());
    if (!(operand.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.operand().location(),
          "`%s` is not arithmetic",
          operand.type());
    }
    return new CheckedExpression(
      creator.apply(operand.expression()),
      operand.type());
  }

  private CheckedExpression checkBitwiseOperation(
    Node.UnaryOperator node,
    Function<Semantic.Expression, Semantic.Expression> creator)
  {
    var operand = checkExpression(node.operand());
    if (!(operand.type() instanceof Semantic.Integral)) {
      throw Diagnostic
        .error(
          node.operand().location(),
          "`%s` is not integral",
          operand.type());
    }
    return new CheckedExpression(
      creator.apply(operand.expression()),
      operand.type());
  }

  private CheckedExpression checkLogicalOperation(
    Node.UnaryOperator node,
    Function<Semantic.Expression, Semantic.Expression> creator)
  {
    var operand = checkExpression(node.operand());
    if (!(operand.type() instanceof Semantic.Boolean)) {
      throw Diagnostic
        .error(
          node.operand().location(),
          "`%s` is not boolean",
          operand.type());
    }
    return new CheckedExpression(
      creator.apply(operand.expression()),
      operand.type());
  }

  private CheckedExpression checkArithmeticOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    var leftOperand = checkExpression(node.leftOperand());
    if (!(leftOperand.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not arithmetic",
          leftOperand.type());
    }
    var rightOperand = checkExpression(node.rightOperand());
    if (!(rightOperand.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not arithmetic",
          rightOperand.type());
    }
    return new CheckedExpression(
      creator
        .apply(
          leftOperand.expression(),
          coerce(
            node.rightOperand().location(),
            rightOperand,
            leftOperand.type())),
      leftOperand.type());
  }

  private CheckedExpression checkBitwiseOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    var leftOperand = checkExpression(node.leftOperand());
    if (!(leftOperand.type() instanceof Semantic.Integral)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not integral",
          leftOperand.type());
    }
    var rightOperand = checkExpression(node.rightOperand());
    if (!(rightOperand.type() instanceof Semantic.Integral)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not integral",
          rightOperand.type());
    }
    return new CheckedExpression(
      creator
        .apply(
          leftOperand.expression(),
          coerce(
            node.rightOperand().location(),
            rightOperand,
            leftOperand.type())),
      leftOperand.type());
  }

  private CheckedExpression checkComparisonOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    var leftOperand = checkExpression(node.leftOperand());
    if (!(leftOperand.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not arithmetic",
          leftOperand.type());
    }
    var rightOperand = checkExpression(node.rightOperand());
    if (!(rightOperand.type() instanceof Semantic.Arithmetic)) {
      throw Diagnostic
        .error(
          node.leftOperand().location(),
          "`%s` is not arithmetic",
          rightOperand.type());
    }
    return new CheckedExpression(
      creator
        .apply(
          leftOperand.expression(),
          coerce(
            node.rightOperand().location(),
            rightOperand,
            leftOperand.type())),
      Semantic.BOOLEAN);
  }

  private CheckedExpression checkLogicalOperation(
    Node.BinaryOperator node,
    BiFunction<Semantic.Expression, Semantic.Expression, Semantic.Expression> creator)
  {
    var leftOperand  =
      coerce(
        node.leftOperand().location(),
        checkExpression(node.leftOperand()),
        Semantic.BOOLEAN);
    var rightOperand =
      coerce(
        node.rightOperand().location(),
        checkExpression(node.rightOperand()),
        Semantic.BOOLEAN);
    return new CheckedExpression(
      creator.apply(leftOperand, rightOperand),
      Semantic.BOOLEAN);
  }

  private CheckedExpression checkCall(
    Location location,
    Node.Expression calleeNode,
    List<Node.Expression> argumentNodes)
  {
    var callee = checkExpression(calleeNode);
    if (!(callee.type() instanceof Semantic.Callable callable)) {
      throw Diagnostic.error(calleeNode.location(), "is not callable");
    }
    if (argumentNodes.length() != callable.parameters().length()) {
      throw Diagnostic
        .error(
          location,
          "callable with %d parameters is called with %d arguments",
          callable.parameters().length(),
          argumentNodes.length());
    }
    var arguments = ListBuffer.<Semantic.Expression>create();
    for (var i = 0; i < arguments.length(); i++) {
      var argument  = argumentNodes.get(i);
      var parameter = callable.parameters().get(i);
      var checked   = checkExpression(argument);
      var coerced   = coerce(argument.location(), checked, parameter);
      arguments.add(coerced);
    }
    return new CheckedExpression(
      new Semantic.Calling(callee.expression(), arguments.toList()),
      callable.returnType());
  }

  private Semantic.Symbol accessGlobal(Node.Mention mention) {
    var symbol = accessor.access(mention.location(), mention.toName());
    while (symbol instanceof Semantic.Alias alias) {
      symbol = accessor.access(mention.location(), alias.aliased());
    }
    return symbol;
  }

  private Semantic.Expression coerce(
    Object subject,
    CheckedExpression source,
    Semantic.Type target)
  {
    if (source.type().equals(target)) {
      return source.expression();
    }
    switch (source.expression()) {
      case Semantic.StringConstant stringConstant -> {
        if (target.equals(Semantic.BYTE_POINTER)) {
          return new Semantic.Conversion(source.expression(), target);
        }
        throw Diagnostic
          .error(subject, "`%s` cannot coerce to `%s`", source.type(), target);
      }
      case Semantic.RealConstant realConstant -> {
        if (target instanceof Semantic.Real real) {
          if (!real.canRepresent(realConstant.value())) {
            throw Diagnostic
              .error(
                subject,
                "`%s` cannot represent `%s`",
                target,
                Text.format(realConstant.value()));
          }
          return new Semantic.Conversion(source.expression(), target);
        }
        throw Diagnostic
          .error(subject, "`%s` cannot coerce to `%s`", source.type(), target);
      }
      case Semantic.IntegralConstant integralConstant -> {
        if (target instanceof Semantic.Real real) {
          if (!real.canRepresent(integralConstant.value())) {
            throw Diagnostic
              .error(
                subject,
                "`%s` cannot represent `%s`",
                target,
                integralConstant.value());
          }
          return new Semantic.Conversion(source.expression(), target);
        }
        if (target instanceof Semantic.Integral integral) {
          if (!integral.canRepresent(integralConstant.value())) {
            throw Diagnostic
              .error(
                subject,
                "`%s` cannot represent `%s`",
                target,
                integralConstant.value());
          }
          return new Semantic.Conversion(source.expression(), target);
        }
        throw Diagnostic
          .error(subject, "`%s` cannot coerce to `%s`", source.type(), target);
      }
      default ->
        throw Diagnostic
          .error(subject, "`%s` cannot coerce to `%s`", source.type(), target);
    }
  }
}
