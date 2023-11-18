package duru;

public sealed interface Node {
  sealed interface Declaration extends Node {
    boolean isPublic();
    Token.Identifier name();
  }

  record Proc(
    Location location,
    boolean isPublic,
    Token.Identifier name,
    Map<Token.Identifier, Formula> parameters,
    Optional<Formula> returnType,
    Statement body) implements Declaration
  {}

  record ExternalProc(
    Location location,
    boolean isPublic,
    Token.Identifier name,
    Map<Token.Identifier, Formula> parameters,
    Optional<Formula> returnType,
    StringConstant externalName) implements Declaration
  {}

  record Struct(Location location, boolean isPublic, Token.Identifier name)
    implements Declaration
  {}

  sealed interface Statement extends Node {}

  record Var(
    Location location,
    Token.Identifier name,
    Optional<Formula> type,
    Expression initialValue) implements Statement
  {}

  record Block(Location location, List<Statement> innerStatements)
    implements Statement
  {}

  record If(
    Location location,
    Expression condition,
    Statement trueBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record Return(Location location, Optional<Expression> value)
    implements Statement
  {}

  record Discard(Location location, Expression discarded)
    implements Statement
  {}

  sealed interface Formula extends Node {}

  record Pointer(Location location, Formula pointee) implements Formula {}

  record Base(Mention name) implements Formula {
    @Override
    public Location location() {
      return name.location();
    }
  }

  sealed interface Expression extends Node {}

  record LessThan(Location location, Exception left, Expression right)
    implements Expression
  {}

  record Invocation(
    Location location,
    Mention procedure,
    List<Expression> arguments) implements Expression
  {}

  record NaturalConstant(Token.NaturalConstant value) implements Expression {
    @Override
    public Location location() {
      return value.location();
    }
  }

  record StringConstant(Token.StringConstant value) implements Expression {
    @Override
    public Location location() {
      return value.location();
    }
  }

  record Mention(
    Location location,
    Optional<Token.Identifier> packageName,
    Token.Identifier name) implements Node
  {}

  Location location();
}
