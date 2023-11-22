package duru;

public sealed interface Node {
  sealed interface Declaration extends Node {
    Optional<Token.StringConstant> externalName();
    boolean isPublic();
    Token.Identifier name();
  }

  record Proc(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Token.Identifier name,
    List<Parameter> parameters,
    Optional<Formula> returnType,
    Optional<Statement> body) implements Declaration
  {}

  record Parameter(Location location, Token.Identifier name, Formula type)
    implements Node
  {}

  record Struct(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Token.Identifier name) implements Declaration
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

  sealed interface Precedence01 extends Expression {}

  record LessThan(Location location, Precedence01 left, Precedence01 right)
    implements Precedence01
  {}

  sealed interface Precedence00 extends Precedence01 {}

  record Access(Mention mention) implements Precedence00 {
    @Override
    public Location location() {
      return mention.location();
    }
  }

  record Invocation(
    Location location,
    Mention procedure,
    List<Expression> arguments) implements Precedence00
  {}

  record NaturalConstant(Token.NumberConstant value) implements Precedence00 {
    @Override
    public Location location() {
      return value.location();
    }
  }

  record StringConstant(Token.StringConstant value) implements Precedence00 {
    @Override
    public Location location() {
      return value.location();
    }
  }

  record Mention(Location location, List<Token.Identifier> identifiers)
    implements Node
  {
    public Name toName() {
      return new Name(identifiers.transform(Token.Identifier::text));
    }
  }

  Location location();
}
