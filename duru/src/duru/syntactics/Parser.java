package duru.syntactics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import duru.lectics.Lectics;
import duru.lectics.Token;

/** Transforms tokens to a list of declarations. */
public final class Parser {
  /** Parses a source file. Returns the declarations in the file. */
  public static Syntactics parse(Lectics source) {
    var parser = new Parser(source);
    return parser.parse();
  }

  /** Source file that is parsed. */
  private Lectics source;

  /** Index of the currently parsed token. */
  private int current;

  /** Constructor. */
  private Parser(Lectics source) {
    this.source = source;
  }

  /** Parses the source file. */
  private Syntactics parse() {
    current = 0;
    var declarations = new ArrayList<Node.Declaration>();
    while (current != source.tokens().size()) {
      var declaration = expect(this::parseDeclaration, "top level declaration");
      declarations.add(declaration);
    }
    return new Syntactics(source, declarations);
  }

  /** Parses a declaration. */
  private Optional<Node.Declaration> parseDeclaration() {
    return firstOf(this::parseEntrypoint, this::parseDefinition);
  }

  /** Parses a entrypoint. */
  private Optional<Node.Entrypoint> parseEntrypoint() {
    if (parseToken(Token.Entrypoint.class).isEmpty()) {
      return Optional.empty();
    }
    var body       =
      expect(this::parseBlock, "body of the entrypoint declaration");
    var entrypoint = new Node.Entrypoint(body);
    return Optional.of(entrypoint);
  }

  /** Parses a definition. */
  private Optional<Node.Definition> parseDefinition() {
    var modifier = parseToken(Token.Public.class);
    if (modifier.isPresent()) {
      return Optional
        .of(
          expect(
            () -> firstOf(
              () -> parseUsing(modifier),
              () -> parseProc(modifier),
              () -> parseConst(modifier),
              () -> parseGlobalVar(modifier)),
            "a definition"));
    }
    return firstOf(
      () -> parseUsing(modifier),
      () -> parseProc(modifier),
      () -> parseConst(modifier),
      () -> parseGlobalVar(modifier));
  }

  /** Parses a using. */
  private Optional<Node.Using> parseUsing(Optional<Token.Public> modifier) {
    if (parseToken(Token.Using.class).isEmpty()) {
      return Optional.empty();
    }
    var used  =
      expect(this::parseMention, "mention to used of the alias definition");
    var alias = Optional.<Token.Identifier>empty();
    if (parseToken(Token.As.class).isPresent()) {
      alias =
        Optional
          .of(
            expectToken(
              Token.Identifier.class,
              "alias identifier of the alias definition"));
    }
    expectToken(
      Token.Semicolon.class,
      "terminator `;` of the alias definition");
    var using = new Node.Using(modifier, used, alias);
    return Optional.of(using);
  }

  /** Parses a proc. */
  private Optional<Node.Proc> parseProc(Optional<Token.Public> modifier) {
    if (parseToken(Token.Proc.class).isEmpty()) {
      return Optional.empty();
    }
    var identifier =
      expectToken(
        Token.Identifier.class,
        "identifier of the procedure declaration");
    expectToken(
      Token.OpeningParenthesis.class,
      "parameter list opener `(` of the procedure declaration");
    var parameters = separatedOf(this::parseParameter);
    expectToken(
      Token.ClosingParenthesis.class,
      "parameter list closer `)` of the procedure declaration");
    var body = expect(this::parseBlock, "body of the procedure declaration");
    var proc = new Node.Proc(modifier, identifier, parameters, body);
    return Optional.of(proc);
  }

  /** Parses a parameter. */
  private Optional<Node.Parameter> parseParameter() {
    var identifier = parseToken(Token.Identifier.class);
    if (identifier.isEmpty()) {
      return Optional.empty();
    }
    var reference = parseToken(Token.Ampersand.class).isPresent();
    var parameter = new Node.Parameter(identifier.get(), reference);
    return Optional.of(parameter);
  }

  /** Parses a const. */
  private Optional<Node.Const> parseConst(Optional<Token.Public> modifier) {
    if (parseToken(Token.Const.class).isEmpty()) {
      return Optional.empty();
    }
    var identifier =
      expectToken(
        Token.Identifier.class,
        "identifier of the constant declaration");
    expectToken(
      Token.Equal.class,
      "value separator `=` of the constant declaration");
    var initialValue =
      expect(this::parseExpression, "value of the constant declaration");
    expectToken(
      Token.Semicolon.class,
      "terminator `;` of the constant declaration");
    var var = new Node.Const(modifier, identifier, initialValue);
    return Optional.of(var);
  }

  /** Parses a global variable. */
  private Optional<Node.GlobalVar> parseGlobalVar(
    Optional<Token.Public> modifier)
  {
    if (parseToken(Token.Var.class).isEmpty()) {
      return Optional.empty();
    }
    var identifier   =
      expectToken(
        Token.Identifier.class,
        "identifier of the global variable declaration");
    var initialValue = Optional.<Node.Expression>empty();
    if (parseToken(Token.Equal.class).isPresent()) {
      var givenInitialValue =
        expect(
          this::parseExpression,
          "initial value of the global variable declaration");
      initialValue = Optional.of(givenInitialValue);
    }
    expectToken(
      Token.Semicolon.class,
      "terminator `;` of the global variable declaration");
    var global = new Node.GlobalVar(modifier, identifier, initialValue);
    return Optional.of(global);
  }

  /** Parses a statement. */
  private Optional<Node.Statement> parseStatement() {
    return firstOf(
      this::parseBlock,
      this::parseIf,
      this::parseWhile,
      this::parseBreak,
      this::parseContinue,
      this::parseReturn,
      this::parseLocalVar,
      this::parseAffect);
  }

  /** Parses a block. */
  private Optional<Node.Block> parseBlock() {
    var first = current;
    if (parseToken(Token.OpeningBrace.class).isEmpty()) {
      return Optional.empty();
    }
    var body = repeatsOf(this::parseStatement);
    expectToken(
      Token.ClosingBrace.class,
      "inner statement list closer `}` of the block statement");
    var block = new Node.Block(first, body);
    return Optional.of(block);
  }

  /** Parses an if statement. */
  private Optional<Node.If> parseIf() {
    if (parseToken(Token.If.class).isEmpty()) {
      return Optional.empty();
    }
    var variables   = repeatsOf(this::parseLocalVar);
    var condition   =
      expect(this::parseExpression, "condition of the if statement");
    var trueBranch  =
      expect(this::parseBlock, "true branch of the if statement");
    var falseBranch = Optional.<Node.Statement>empty();
    if (parseToken(Token.Else.class).isPresent()) {
      falseBranch =
        Optional
          .of(
            expect(
              () -> firstOf(this::parseBlock, this::parseIf),
              "false branch of the if statement"));
    }
    var ifStatement =
      new Node.If(variables, condition, trueBranch, falseBranch);
    return Optional.of(ifStatement);
  }

  /** Parses a while statement. */
  private Optional<Node.While> parseWhile() {
    var first = current;
    var label = parseToken(Token.Identifier.class);
    if (label.isPresent() && parseToken(Token.Colon.class).isEmpty()
      || parseToken(Token.While.class).isEmpty())
    {
      current = first;
      return Optional.empty();
    }
    var variables   = repeatsOf(this::parseLocalVar);
    var condition   =
      expect(this::parseExpression, "condition of the while statement");
    var interleaved = Optional.<Node.Statement>empty();
    if (parseToken(Token.Semicolon.class).isPresent()) {
      interleaved =
        Optional
          .of(
            expect(
              this::parseUnterminatedAffect,
              "interleaved of the while statement"));
    }
    var loop       =
      expect(this::parseStatement, "loop of the while statement");
    var zeroBranch = Optional.<Node.Statement>empty();
    if (parseToken(Token.Else.class).isPresent()) {
      zeroBranch =
        Optional
          .of(
            expect(
              () -> firstOf(this::parseBlock, this::parseIf),
              "zero branch of the while statement"));
    }
    var whileStatement =
      new Node.While(
        label,
        variables,
        condition,
        interleaved,
        loop,
        zeroBranch);
    return Optional.of(whileStatement);
  }

  /** Parses a break statement. */
  private Optional<Node.Break> parseBreak() {
    var first = current;
    if (parseToken(Token.Break.class).isEmpty()) {
      return Optional.empty();
    }
    var label = parseToken(Token.Identifier.class);
    expectToken(Token.Semicolon.class, "terminator `;` of the break statement");
    var breakStatement = new Node.Break(first, label);
    return Optional.of(breakStatement);
  }

  /** Parses a continue statement. */
  private Optional<Node.Continue> parseContinue() {
    var first = current;
    if (parseToken(Token.Continue.class).isEmpty()) {
      return Optional.empty();
    }
    var label = parseToken(Token.Identifier.class);
    expectToken(
      Token.Semicolon.class,
      "terminator `;` of the continue statement");
    var continueStatement = new Node.Continue(first, label);
    return Optional.of(continueStatement);
  }

  /** Parses a return statement. */
  private Optional<Node.Return> parseReturn() {
    var first = current;
    if (parseToken(Token.Return.class).isEmpty()) {
      return Optional.empty();
    }
    var value = parseExpression();
    expectToken(
      Token.Semicolon.class,
      "terminator `;` of the return statement");
    var returnStatement = new Node.Return(first, value);
    return Optional.of(returnStatement);
  }

  /** Parses a local variable. */
  private Optional<Node.LocalVar> parseLocalVar() {
    if (parseToken(Token.Var.class).isEmpty()) {
      return Optional.empty();
    }
    var identifier   =
      expectToken(
        Token.Identifier.class,
        "identifier of the local variable declaration");
    var initialValue = Optional.<Node.Expression>empty();
    if (parseToken(Token.Equal.class).isPresent()) {
      var givenInitialValue =
        expect(
          this::parseExpression,
          "initial value of the local variable declaration");
      initialValue = Optional.of(givenInitialValue);
    }
    expectToken(
      Token.Semicolon.class,
      "terminator `;` of the local variable declaration");
    var local = new Node.LocalVar(identifier, initialValue);
    return Optional.of(local);
  }

  /** Parses an affect statement. */
  private Optional<Node.Affect> parseAffect() {
    var affect = parseUnterminatedAffect();
    if (affect.isPresent()) {
      expectToken(
        Token.Semicolon.class,
        "terminator `;` of the %s statement".formatted(switch (affect.get())
        {
          case Node.Increment i -> "increment";
          case Node.Decrement d -> "decrement";
          case Node.DirectlyAssign a -> "assign";
          case Node.MultiplyAssign a -> "multiply assign";
          case Node.DivideAssign a -> "divide assign";
          case Node.DivideIntegerAssign a -> "divide integer assign";
          case Node.ModulusAssign a -> "modulus assign";
          case Node.AddAssign a -> "add assign";
          case Node.SubtractAssign a -> "subtract assign";
          case Node.ShiftLeftAssign a -> "shift left assign";
          case Node.ShiftRightAssign a -> "shift right assign";
          case Node.AndBitwiseAssign a -> "and bitwise assign";
          case Node.XorBitwiseAssign a -> "xor bitwise assign";
          case Node.OrBitwiseAssign a -> "or bitwise assign";
          case Node.Discard d -> "discard";
        }));
    }
    return affect;
  }

  /** Parses an affect statement without a terminator. */
  private Optional<Node.Affect> parseUnterminatedAffect() {
    var expression = parseExpression();
    if (expression.isEmpty()) {
      return Optional.empty();
    }
    var affect = (Node.Affect) new Node.Discard(expression.get());
    if (expression.get() instanceof Node.SymbolAccess target) {
      var assignmentParser = parseAssignmentOperator();
      if (assignmentParser.isPresent()) {
        var source =
          expect(
            this::parseExpression,
            "source of the %s statement"
              .formatted(assignmentParser.get().name()));
        affect = assignmentParser.get().initializer().apply(target, source);
      }
      else if (parseToken(Token.PlusPlus.class).isPresent()) {
        affect = new Node.Increment(target);
      }
      else if (parseToken(Token.MinusMinus.class).isPresent()) {
        affect = new Node.Decrement(target);
      }
    }
    return Optional.of(affect);
  }

  /** Parses an assignment operator. */
  private Optional<AssignmentParser> parseAssignmentOperator() {
    if (parseToken(Token.Equal.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.DirectlyAssign::new, "assign"));
    }
    if (parseToken(Token.StarEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.MultiplyAssign::new, "multiply assign"));
    }
    if (parseToken(Token.SlashEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.DivideAssign::new, "divide assign"));
    }
    if (parseToken(Token.PercentEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.ModulusAssign::new, "modulus assign"));
    }
    if (parseToken(Token.PlusEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.AddAssign::new, "add assign"));
    }
    if (parseToken(Token.MinusEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.SubtractAssign::new, "subtract assign"));
    }
    if (parseToken(Token.LeftLeftEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(Node.ShiftLeftAssign::new, "shift left assign"));
    }
    if (parseToken(Token.RightRightEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.ShiftRightAssign::new,
            "shift right assign"));
    }
    if (parseToken(Token.AmpersandEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.AndBitwiseAssign::new,
            "and bitwise assign"));
    }
    if (parseToken(Token.CaretEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.XorBitwiseAssign::new,
            "xor bitwise assign"));
    }
    if (parseToken(Token.PipeEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(Node.OrBitwiseAssign::new, "or bitwise assign"));
    }
    return Optional.empty();
  }

  /** Parses an expression. */
  private Optional<Node.Expression> parseExpression() {
    return firstOf(this::parsePrecedence11);
  }

  /** Parses an expression at precedence level 11. */
  private Optional<Node.Precedence11> parsePrecedence11() {
    return parseBinaryOperations(
      this::parsePrecedence10,
      new BinaryOperationParser<>(
        Token.PipePipe.class,
        Node.LogicalOr::new,
        "logical or"));
  }

  /** Parses an expression at precedence level 10. */
  private Optional<Node.Precedence10> parsePrecedence10() {
    return parseBinaryOperations(
      this::parsePrecedence09,
      new BinaryOperationParser<>(
        Token.AmpersandAmpersand.class,
        Node.LogicalAnd::new,
        "logical and"));
  }

  /** Parses an expression at precedence level 9. */
  private Optional<Node.Precedence09> parsePrecedence09() {
    return parseBinaryOperations(
      this::parsePrecedence08,
      new BinaryOperationParser<>(
        Token.EqualEqual.class,
        Node.EqualTo::new,
        "equal to"),
      new BinaryOperationParser<>(
        Token.ExclamationEqual.class,
        Node.NotEqualTo::new,
        "not equal to"),
      new BinaryOperationParser<>(
        Token.EqualEqualEqual.class,
        Node.StrictlyEqualTo::new,
        "strictly equal to"));
  }

  /** Parses an expression at precedence level 8. */
  private Optional<Node.Precedence08> parsePrecedence08() {
    return parseBinaryOperations(
      this::parsePrecedence07,
      new BinaryOperationParser<>(
        Token.Left.class,
        Node.LessThan::new,
        "less than"),
      new BinaryOperationParser<>(
        Token.LeftEqual.class,
        Node.LessThanOrEqualTo::new,
        "less than or equal to"),
      new BinaryOperationParser<>(
        Token.Right.class,
        Node.GreaterThan::new,
        "greater than"),
      new BinaryOperationParser<>(
        Token.RightEqual.class,
        Node.GreaterThanOrEqualTo::new,
        "greater than or equal to"));
  }

  /** Parses an expression at precedence level 7. */
  private Optional<Node.Precedence07> parsePrecedence07() {
    return parseBinaryOperations(
      this::parsePrecedence06,
      new BinaryOperationParser<>(
        Token.Pipe.class,
        Node.BitwiseOr::new,
        "bitwise or"));
  }

  /** Parses an expression at precedence level 6. */
  private Optional<Node.Precedence06> parsePrecedence06() {
    return parseBinaryOperations(
      this::parsePrecedence05,
      new BinaryOperationParser<>(
        Token.Caret.class,
        Node.BitwiseXor::new,
        "bitwise xor"));
  }

  /** Parses an expression at precedence level 5. */
  private Optional<Node.Precedence05> parsePrecedence05() {
    return parseBinaryOperations(
      this::parsePrecedence04,
      new BinaryOperationParser<>(
        Token.Ampersand.class,
        Node.BitwiseAnd::new,
        "bitwise and"));
  }

  /** Parses an expression at precedence level 4. */
  private Optional<Node.Precedence04> parsePrecedence04() {
    return parseBinaryOperations(
      this::parsePrecedence03,
      new BinaryOperationParser<>(
        Token.LeftLeft.class,
        Node.LeftShift::new,
        "left shift"),
      new BinaryOperationParser<>(
        Token.RightRight.class,
        Node.RightShift::new,
        "right shift"));
  }

  /** Parses an expression at precedence level 3. */
  private Optional<Node.Precedence03> parsePrecedence03() {
    return parseBinaryOperations(
      this::parsePrecedence02,
      new BinaryOperationParser<>(
        Token.Plus.class,
        Node.Addition::new,
        "addition"),
      new BinaryOperationParser<>(
        Token.Minus.class,
        Node.Subtraction::new,
        "subtraction"));
  }

  /** Parses an expression at precedence level 2. */
  private Optional<Node.Precedence02> parsePrecedence02() {
    return parseBinaryOperations(
      this::parsePrecedence01,
      new BinaryOperationParser<>(
        Token.Star.class,
        Node.Multiplication::new,
        "multiplication"),
      new BinaryOperationParser<>(
        Token.Slash.class,
        Node.Division::new,
        "division"),
      new BinaryOperationParser<>(
        Token.Percent.class,
        Node.Modulus::new,
        "modulus"));
  }

  /** Parses a group of binary operators in the same precedence level from left
   * to right. */
  @SafeVarargs
  private <PrecedenceType extends Node.Expression, OperandType extends PrecedenceType> Optional<PrecedenceType> parseBinaryOperations(
    Supplier<Optional<OperandType>> operandParserFunction,
    BinaryOperationParser<PrecedenceType>... binaryOperationParsers)
  {
    var firstOperand = operandParserFunction.get();
    if (firstOperand.isEmpty()) {
      return Optional.empty();
    }
    var result = (PrecedenceType) firstOperand.get();
    leftToRightSamePrecedenceOperatorParsing: while (true) {
      for (var binaryOperationParser : binaryOperationParsers) {
        if (parseToken(binaryOperationParser.operatorClass()).isEmpty()) {
          continue;
        }
        var rightOperand =
          expect(
            operandParserFunction,
            "right operand of %s expression"
              .formatted(binaryOperationParser.name()));
        result =
          binaryOperationParser.initializer().apply(result, rightOperand);
        continue leftToRightSamePrecedenceOperatorParsing;
      }
      break;
    }
    return Optional.of(result);
  }

  /** Parses an expression at precedence level 1. */
  private Optional<Node.Precedence01> parsePrecedence01() {
    return parseUnaryOperations(
      this::parsePrecedence00,
      new UnaryOperationParser<>(
        Token.Plus.class,
        Node.Promotion::new,
        "promotion"),
      new UnaryOperationParser<>(
        Token.Minus.class,
        Node.Negation::new,
        "negation"),
      new UnaryOperationParser<>(
        Token.Tilde.class,
        Node.BitwiseNot::new,
        "bitwise not"),
      new UnaryOperationParser<>(
        Token.Exclamation.class,
        Node.LogicalNot::new,
        "logical not"));
  }

  /** Parses a group of unary operators in the same precedence level from right
   * to left. */
  @SafeVarargs
  private <PrecedenceType extends Node.Expression, OperandType extends PrecedenceType> Optional<PrecedenceType> parseUnaryOperations(
    Supplier<Optional<OperandType>> operandParserFunction,
    UnaryOperationParser<PrecedenceType>... unaryOperationParsers)
  {
    var stack = new ArrayList<UnaryOperationParser<PrecedenceType>>();
    leftToRightSamePrecedenceOperatorParsing: while (true) {
      for (var unaryOperationParser : unaryOperationParsers) {
        if (parseToken(unaryOperationParser.operatorClass()).isEmpty()) {
          continue;
        }
        stack.add(unaryOperationParser);
        continue leftToRightSamePrecedenceOperatorParsing;
      }
      break;
    }
    if (stack.isEmpty()) {
      return operandParserFunction.get().map(Function.identity());
    }
    var result =
      (PrecedenceType) expect(
        operandParserFunction,
        "operand of %s expression"
          .formatted(stack.get(stack.size() - 1).name()));
    for (var i = stack.size(); i != 0; i--) {
      result = stack.get(i - 1).initializer().apply(result);
    }
    return Optional.of(result);
  }

  /** Parses an expression at precedence level 0. */
  private Optional<Node.Precedence00> parsePrecedence00() {
    Optional<Node.Precedence00> precedence0 =
      firstOf(
        this::parseGrouping,
        this::parseSymbolBased,
        this::parseNaturalConstant,
        this::parseRealConstant,
        this::parseStringConstant);
    if (precedence0.isEmpty()) {
      return precedence0;
    }
    var result = precedence0.get();
    while (parseToken(Token.Dot.class).isPresent()) {
      var member =
        expectToken(
          Token.Identifier.class,
          "member name in the member access expression");
      if (parseToken(Token.OpeningParenthesis.class).isPresent()) {
        var remainingArguments = separatedOf(this::parseExpression);
        expectToken(
          Token.ClosingParenthesis.class,
          "remaining argument list closer `)` of the member call expression");
        result = new Node.MemberCall(result, member, remainingArguments);
        continue;
      }
      result = new Node.MemberAccess(result, member);
    }
    return Optional.of(result);
  }

  /** Parses a grouping. */
  private Optional<Node.Grouping> parseGrouping() {
    if (parseToken(Token.OpeningParenthesis.class).isEmpty()) {
      return Optional.empty();
    }
    var grouped =
      expect(
        this::parseExpression,
        "grouped expression of the grouping expression");
    expectToken(
      Token.ClosingParenthesis.class,
      "closer `)` of the grouping expression");
    var grouping = new Node.Grouping(grouped);
    return Optional.of(grouping);
  }

  /** Parses a symbol based. */
  private Optional<Node.SymbolBased> parseSymbolBased() {
    var mention = parseMention();
    if (mention.isEmpty()) {
      return Optional.empty();
    }
    var symbolBased = (Node.SymbolBased) new Node.SymbolAccess(mention.get());
    if (parseToken(Token.OpeningParenthesis.class).isPresent()) {
      var arguments = separatedOf(this::parseExpression);
      expectToken(
        Token.ClosingParenthesis.class,
        "argument list closer `)` of the call expression");
      symbolBased = new Node.Call(mention.get(), arguments);
    }
    return Optional.of(symbolBased);
  }

  /** Parses a natural constant. */
  private Optional<Node.NaturalConstant> parseNaturalConstant() {
    return parseToken(Token.NaturalConstant.class)
      .map(Node.NaturalConstant::new);
  }

  /** Parses a real constant. */
  private Optional<Node.RealConstant> parseRealConstant() {
    return parseToken(Token.RealConstant.class).map(Node.RealConstant::new);
  }

  /** Parses a string constant. */
  private Optional<Node.StringConstant> parseStringConstant() {
    return parseToken(Token.StringConstant.class).map(Node.StringConstant::new);
  }

  /** Parses a mention. */
  private Optional<Node.Mention> parseMention() {
    var scope = parseScope();
    if (scope.isPresent()) {
      var identifier =
        expectToken(
          Token.Identifier.class,
          "identifier of the qualified mention");
      var mention    = new Node.Mention(scope, identifier);
      return Optional.of(mention);
    }
    var identifier = parseToken(Token.Identifier.class);
    if (identifier.isEmpty()) {
      return Optional.empty();
    }
    var mention = new Node.Mention(scope, identifier.get());
    return Optional.of(mention);
  }

  /** Parses a scope, which is the name of a source fallowed by `::`. */
  private Optional<Token.Identifier> parseScope() {
    var first = current;
    var scope = parseToken(Token.Identifier.class);
    if (scope.isEmpty()) {
      return Optional.empty();
    }
    if (parseToken(Token.ColonColon.class).isEmpty()) {
      current = first;
      return Optional.empty();
    }
    return scope.map(Function.identity());
  }

  /** Runs the given parser repeatedly and collects the parsed constructs as a
   * list. Parses a separator comma between the constructs. Optionally, there
   * could be a trailing comma. */
  private <ConstructType> List<ConstructType> separatedOf(
    Supplier<Optional<ConstructType>> parserFunction)
  {
    var constructs = new ArrayList<ConstructType>();
    while (true) {
      var construct = parserFunction.get();
      if (construct.isEmpty()) {
        break;
      }
      constructs.add(construct.get());
      if (parseToken(Token.Comma.class).isEmpty()) {
        break;
      }
    }
    return constructs;
  }

  /** Runs the given parser repeatedly and collects the parsed constructs as a
   * list. */
  private <ConstructType> List<ConstructType> repeatsOf(
    Supplier<Optional<ConstructType>> parserFunction)
  {
    var constructs = new ArrayList<ConstructType>();
    while (true) {
      var construct = parserFunction.get();
      if (construct.isEmpty()) {
        break;
      }
      constructs.add(construct.get());
    }
    return constructs;
  }

  /** Returns the first construct that successfully parsed out of the given
   * parsers. If all fail fails. */
  @SafeVarargs
  private <ConstructType> Optional<ConstructType> firstOf(
    Supplier<Optional<? extends ConstructType>>... parserFunctions)
  {
    for (var parserFunction : parserFunctions) {
      var construct = parserFunction.get();
      if (construct.isPresent()) {
        return Optional.of(construct.get());
      }
    }
    return Optional.empty();
  }

  /** Ensures that the given token parses. Otherwise throws a diagnostic with
   * the given explanation. Returns the parsed token. */
  private <TokenType extends Token> TokenType expectToken(
    Class<TokenType> tokenClass,
    String tokenExplanation)
  {
    return expect(() -> parseToken(tokenClass), tokenExplanation);
  }

  /** Parses the next token if it exists and it is of the given class. */
  @SuppressWarnings("unchecked")
  private <TokenType extends Token> Optional<TokenType> parseToken(
    Class<TokenType> tokenClass)
  {
    if (current == source.tokens().size()) {
      return Optional.empty();
    }
    var token = source.tokens().get(current);
    if (!tokenClass.isInstance(token)) {
      return Optional.empty();
    }
    current++;
    return Optional.of((TokenType) token);
  }

  /** Ensures that the given parser parses. Otherwise throws a diagnostic with
   * the given explanation. Returns the parsed construct. */
  private <ConstructType> ConstructType expect(
    Supplier<Optional<ConstructType>> parseFunction,
    String constructExplanation)
  {
    var construct = parseFunction.get();
    if (construct.isPresent()) {
      return construct.get();
    }
    if (current == source.tokens().size()) {
      var reportedToken = source.tokens().getLast();
      throw source
        .subject(reportedToken)
        .diagnose(
          "error",
          "Expected %s at the end of the file after %s!",
          constructExplanation,
          reportedToken.explanation())
        .toException();
    }
    var reportedToken = source.tokens().get(current);
    throw source
      .subject(reportedToken)
      .diagnose(
        "error",
        "Expected %s instead of %s!",
        constructExplanation,
        reportedToken.explanation())
      .toException();
  }
}
