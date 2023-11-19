package duru;

import java.util.function.Function;
import java.util.function.Supplier;

public final class SourceParser {
  public static List<Node.Declaration> parse(List<Token> tokens) {
    var parser = new SourceParser(tokens);
    return parser.parse();
  }

  private final List<Token> tokens;
  private int               index;

  private SourceParser(List<Token> tokens) {
    this.tokens = tokens;
  }

  private List<Node.Declaration> parse() {
    index = 0;
    var declarations = ListBuffer.<Node.Declaration>create();
    while (index != tokens.length()) {
      var declaration = expect(this::parseDeclaration, "symbol declaration");
      declarations.add(declaration);
    }
    return declarations.toList();
  }

  private Optional<Node.Declaration> parseDeclaration() {
    var begin    = index;
    var isPublic = take(Token.Public.class);
    if (take(Token.Proc.class)) {
      var name =
        expect(Token.Identifier.class, "name of the procedure declaration");
      expect(
        Token.OpeningParenthesis.class,
        "parameter list opener `(` of the procedure declaration");
      var parameters = MapBuffer.<Token.Identifier, Node.Formula>create();
      while (true) {
        var parameterName = parse(Token.Identifier.class);
        if (parameterName.isEmpty())
          break;
        var parameterType =
          expect(this::parseFormula, "type of the parameter declaration");
        parameters.add(parameterName.getFirst(), parameterType);
        if (!take(Token.Comma.class))
          break;
      }
      expect(
        Token.ClosingParenthesis.class,
        "parameter list closer `)` of the procedure declaration");
      var returnType = parseFormula();
      if (take(Token.Equal.class)) {
        var externalName =
          expect(
            this::parseStringConstant,
            "external name of the procedure declaration");
        expect(
          Token.Semicolon.class,
          "terminator `;` of the procedure declaration");
        return Optional
          .present(
            new Node.ExternalProc(
              location(begin),
              isPublic,
              name,
              parameters.toMap(),
              returnType,
              externalName));
      }
      var body = expect(this::parseBlock, "body of the procedure declaration");
      return Optional
        .present(
          new Node.Proc(
            location(begin),
            isPublic,
            name,
            parameters.toMap(),
            returnType,
            body));
    }
    if (take(Token.Struct.class)) {
      var name =
        expect(Token.Identifier.class, "name of the structure declaration");
      expect(
        Token.Semicolon.class,
        "terminator `;` of the structure declaration");
      return Optional.present(new Node.Struct(location(begin), isPublic, name));
    }
    if (isPublic) {
      throw Subject
        .error("expected a declaration after the visibility modifier");
    }
    return Optional.absent();
  }

  private Optional<Node.Formula> parseFormula() {
    var begin = index;
    if (take(Token.Star.class)) {
      var pointee =
        expect(this::parseFormula, "pointee of the pointer formula");
      return Optional.present(new Node.Pointer(location(begin), pointee));
    }
    var name = parseMention();
    if (name.isEmpty())
      return Optional.absent();
    return Optional.present(new Node.Base(name.getFirst()));
  }

  private Optional<Node.Statement> parseStatement() {
    return or(
      this::parseBlock,
      this::parseIf,
      this::parseVar,
      this::parseReturn,
      this::parseDiscard);
  }

  private Optional<Node.If> parseIf() {
    var begin = index;
    if (!take(Token.If.class)) {
      return Optional.absent();
    }
    var condition   =
      expect(this::parseExpression, "condition of the if statement");
    var trueBranch  =
      expect(this::parseBlock, "true branch of the if statement");
    var falseBranch = Optional.<Node.Statement>absent();
    if (take(Token.Else.class)) {
      falseBranch =
        Optional
          .present(
            expect(
              () -> or(this::parseBlock, this::parseIf),
              "false branch of the if statement"));
    }
    return Optional
      .present(
        new Node.If(location(begin), condition, trueBranch, falseBranch));
  }

  private Optional<Node.Return> parseReturn() {
    var begin = index;
    if (!take(Token.Return.class))
      return Optional.absent();
    var value = parseExpression();
    expect(Token.Semicolon.class, "terminator `;` of the return statement");
    return Optional.present(new Node.Return(location(begin), value));
  }

  private Optional<Node.Discard> parseDiscard() {
    var begin     = index;
    var discarded = parseExpression();
    if (discarded.isEmpty())
      return Optional.absent();
    expect(Token.Semicolon.class, "terminator `;` of the discard statement");
    return Optional
      .present(new Node.Discard(location(begin), discarded.getFirst()));
  }

  private Optional<Node.Var> parseVar() {
    var begin = index;
    if (!take(Token.Var.class))
      return Optional.absent();
    var name         =
      expect(
        Token.Identifier.class,
        "name of the variable declaration statement");
    var type         = parseFormula();
    var initialValue =
      expect(
        this::parseExpression,
        "initial value of the variable declaration statement");
    expect(
      Token.Semicolon.class,
      "terminator `;` of the variable declaration statement");
    return Optional
      .present(new Node.Var(location(begin), name, type, initialValue));
  }

  private Optional<Node.Block> parseBlock() {
    var begin = index;
    if (!take(Token.OpeningBrace.class))
      return Optional.absent();
    var innerStatements = ListBuffer.<Node.Statement>create();
    while (true) {
      var innerStatement = parseStatement();
      if (innerStatement.isEmpty())
        break;
      innerStatements.add(innerStatement.getFirst());
    }
    expect(
      Token.ClosingBrace.class,
      "inner statement list closer `}` of the block expression");
    return Optional
      .present(new Node.Block(location(begin), innerStatements.toList()));
  }

  private Optional<Node.Expression> parseExpression() {
    return parsePrecedence01().transform(Function.identity());
  }

  private Optional<Node.Precedence01> parsePrecedence01() {
    var begin = index;
    var left  = parsePrecedence00();
    if (left.isEmpty())
      return Optional.absent();
    if (!take(Token.Left.class))
      return Optional.present(left.getFirst());
    var right =
      expect(this::parsePrecedence01, "right operand of less than expression");
    return Optional
      .present(new Node.LessThan(location(begin), left.getFirst(), right));
  }

  private Optional<Node.Precedence00> parsePrecedence00() {
    return or(
      this::parseNaturalConstant,
      this::parseStringConstant,
      this::parseAccessOrInvocation);
  }

  private Optional<Node.NaturalConstant> parseNaturalConstant() {
    return parse(Token.NaturalConstant.class)
      .transform(Node.NaturalConstant::new);
  }

  private Optional<Node.StringConstant> parseStringConstant() {
    return parse(Token.StringConstant.class)
      .transform(Node.StringConstant::new);
  }

  private Optional<Node.Precedence00> parseAccessOrInvocation() {
    var begin  = index;
    var symbol = parseMention();
    if (symbol.isEmpty())
      return Optional.absent();
    if (!take(Token.OpeningParenthesis.class))
      return Optional.present(new Node.Access(symbol.getFirst()));
    var arguments = ListBuffer.<Node.Expression>create();
    while (true) {
      var argument = parseExpression();
      if (argument.isEmpty())
        break;
      arguments.add(argument.getFirst());
      if (!take(Token.Comma.class))
        break;
    }
    expect(
      Token.ClosingParenthesis.class,
      "argument list closer `)` of the procedure invocation expression");
    return Optional
      .present(
        new Node.Invocation(
          location(begin),
          symbol.getFirst(),
          arguments.toList()));
  }

  private Optional<Node.Mention> parseMention() {
    var begin   = index;
    var portion = parse(Token.Identifier.class);
    if (portion.isEmpty())
      return Optional.absent();
    var name      = portion.getFirst();
    var subspaces = ListBuffer.<Token.Identifier>create();
    while (take(Token.Dot.class)) {
      subspaces.add(name);
      name = expect(Token.Identifier.class, "name of the mention");
    }
    return Optional
      .present(new Node.Mention(location(begin), subspaces.toList(), name));
  }

  private Location location(int begin) {
    var beginLocation = tokens.get(begin).location();
    var endLocation   = tokens.get(index - 1).location();
    return new Location(
      beginLocation.source(),
      beginLocation.begin(),
      endLocation.end());
  }

  private <T extends Token> T expect(Class<T> tokenClass, String explanation) {
    return expect(() -> parse(tokenClass), explanation);
  }

  private <Value> Value expect(
    Supplier<Optional<Value>> parserFunction,
    String explanation)
  {
    var value = parserFunction.get();
    if (!value.isEmpty()) {
      return value.getLast();
    }
    throw Subject.error("expected %s", explanation);
  }

  private <T extends Token> boolean take(Class<T> tokenClass) {
    return !parse(tokenClass).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private <T extends Token> Optional<T> parse(Class<T> tokenClass) {
    if (index == tokens.length()) {
      return Optional.absent();
    }
    var token = tokens.get(index);
    if (!tokenClass.isInstance(token)) {
      return Optional.absent();
    }
    index++;
    return Optional.present((T) token);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  private <V> Optional<V> or(
    Supplier<Optional<? extends V>>... parserFunctions)
  {
    for (var parserFunction : parserFunctions) {
      var value = parserFunction.get();
      if (!value.isEmpty()) {
        return (Optional<V>) value;
      }
    }
    return Optional.absent();
  }
}
