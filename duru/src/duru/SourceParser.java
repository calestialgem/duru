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
      var declaration = expect(this::parseDeclaration, "declaration");
      declarations.add(declaration);
    }
    return declarations.toList();
  }

  private Optional<Node.Declaration> parseDeclaration() {
    var begin        = index;
    var externalName = Optional.<Token.StringConstant>absent();
    if (take(Token.Extern.class)) {
      externalName =
        Optional.present(expect(Token.StringConstant.class, "external name"));
    }
    var isPublic = take(Token.Public.class);
    if (take(Token.Proc.class)) {
      var name = expect(Token.Identifier.class, "procedure name");
      expect(Token.OpeningParenthesis.class, "`(` of parameter list");
      var parameters = parseSeparated(this::parseParameter);
      expect(Token.ClosingParenthesis.class, "`)` of parameter list");
      var returnType = parseFormula();
      var body       = Optional.<Node.Statement>absent();
      if (!take(Token.Semicolon.class)) {
        body = Optional.present(expect(this::parseBlock, "procedure body"));
      }
      return Optional
        .present(
          new Node.Proc(
            location(begin),
            externalName,
            isPublic,
            name,
            parameters,
            returnType,
            body));
    }
    if (take(Token.Struct.class)) {
      var name = expect(Token.Identifier.class, "structure name");
      expect(Token.Semicolon.class, "`;` of structure declaration");
      return Optional
        .present(
          new Node.Struct(location(begin), externalName, isPublic, name));
    }
    if (!externalName.isEmpty() || isPublic) {
      throw missing("declaration");
    }
    return Optional.absent();
  }

  private Optional<Node.Parameter> parseParameter() {
    var begin         = index;
    var parameterName = parse(Token.Identifier.class);
    if (parameterName.isEmpty()) {
      return Optional.absent();
    }
    var parameterType = expect(this::parseFormula, "parameter type");
    return Optional
      .present(
        new Node.Parameter(
          location(begin),
          parameterName.getFirst(),
          parameterType));
  }

  private Optional<Node.Formula> parseFormula() {
    var begin = index;
    if (take(Token.Star.class)) {
      var pointee = expect(this::parseFormula, "pointee type");
      return Optional.present(new Node.Pointer(location(begin), pointee));
    }
    var name = parseMention();
    if (name.isEmpty()) {
      return Optional.absent();
    }
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
    var condition   = expect(this::parseExpression, "if condition");
    var trueBranch  = expect(this::parseBlock, "if branch");
    var falseBranch = Optional.<Node.Statement>absent();
    if (take(Token.Else.class)) {
      falseBranch =
        Optional
          .present(
            expect(() -> or(this::parseBlock, this::parseIf), "else branch"));
    }
    return Optional
      .present(
        new Node.If(location(begin), condition, trueBranch, falseBranch));
  }

  private Optional<Node.Return> parseReturn() {
    var begin = index;
    if (!take(Token.Return.class)) {
      return Optional.absent();
    }
    var value = parseExpression();
    expect(Token.Semicolon.class, "`;` of return statement");
    return Optional.present(new Node.Return(location(begin), value));
  }

  private Optional<Node.Discard> parseDiscard() {
    var begin     = index;
    var discarded = parseExpression();
    if (discarded.isEmpty()) {
      return Optional.absent();
    }
    expect(Token.Semicolon.class, "`;` of discard statement");
    return Optional
      .present(new Node.Discard(location(begin), discarded.getFirst()));
  }

  private Optional<Node.Var> parseVar() {
    var begin = index;
    if (!take(Token.Var.class)) {
      return Optional.absent();
    }
    var name = expect(Token.Identifier.class, "variable name");
    var type = parseFormula();
    expect(Token.Equal.class, "`=` of variable declaration");
    var initialValue = expect(this::parseExpression, "initial value");
    expect(Token.Semicolon.class, "`;` of variable declaration");
    return Optional
      .present(new Node.Var(location(begin), name, type, initialValue));
  }

  private Optional<Node.Block> parseBlock() {
    var begin = index;
    if (!take(Token.OpeningBrace.class)) {
      return Optional.absent();
    }
    var innerStatements = ListBuffer.<Node.Statement>create();
    while (true) {
      var innerStatement = parseStatement();
      if (innerStatement.isEmpty()) {
        break;
      }
      innerStatements.add(innerStatement.getFirst());
    }
    expect(Token.ClosingBrace.class, "`}` of block statement");
    return Optional
      .present(new Node.Block(location(begin), innerStatements.toList()));
  }

  private Optional<Node.Expression> parseExpression() {
    return parsePrecedence01().transform(Function.identity());
  }

  private Optional<Node.Precedence01> parsePrecedence01() {
    var begin = index;
    var left  = parsePrecedence00();
    if (left.isEmpty()) {
      return Optional.absent();
    }
    if (!take(Token.Left.class)) {
      return Optional.present(left.getFirst());
    }
    var right =
      expect(this::parsePrecedence01, "right operand of less-than expression");
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
    if (symbol.isEmpty()) {
      return Optional.absent();
    }
    if (!take(Token.OpeningParenthesis.class)) {
      return Optional.present(new Node.Access(symbol.getFirst()));
    }
    var arguments = parseSeparated(this::parseExpression);
    expect(Token.ClosingParenthesis.class, "`)` of argument list");
    return Optional
      .present(
        new Node.Invocation(location(begin), symbol.getFirst(), arguments));
  }

  private Optional<Node.Mention> parseMention() {
    var begin   = index;
    var portion = parse(Token.Identifier.class);
    if (portion.isEmpty()) {
      return Optional.absent();
    }
    var name      = portion.getFirst();
    var subspaces = ListBuffer.<Token.Identifier>create();
    while (take(Token.Dot.class)) {
      subspaces.add(name);
      name = expect(Token.Identifier.class, "name");
    }
    return Optional
      .present(new Node.Mention(location(begin), subspaces.toList(), name));
  }

  private Location location(int begin) {
    var beginLocation = tokens.get(begin).location();
    var endLocation   = tokens.get(index - 1).location();
    return Location
      .at(
        beginLocation.source(),
        beginLocation.beginIndex(),
        endLocation.endIndex());
  }

  private <V> List<V> parseSeparated(Supplier<Optional<V>> parserFunction) {
    var list = ListBuffer.<V>create();
    while (true) {
      var value = parserFunction.get();
      if (value.isEmpty()) {
        break;
      }
      list.add(value.getFirst());
      if (!take(Token.Comma.class)) {
        break;
      }
    }
    return list.toList();
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
    throw missing(explanation);
  }

  private RuntimeException missing(String explanation) {
    if (index == 0) {
      return Diagnostic
        .error(
          tokens.getFirst().location(),
          "expected %s instead of %s at beginning of file",
          explanation,
          tokens.getFirst());
    }
    if (index == tokens.length()) {
      return Diagnostic
        .error(
          tokens.getLast().location(),
          "expected %s after %s at end of file",
          explanation,
          tokens.getLast());
    }
    return Diagnostic
      .error(
        tokens.get(index).location(),
        "expected %s instead of %s after %s",
        explanation,
        tokens.get(index),
        tokens.get(index - 1));
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
