package duru;

import duru.Token.Identifier;

public sealed interface Node {
  sealed interface Declaration extends Node {
    Optional<Token.StringConstant> externalName();
    boolean isPublic();
    Token.Identifier name();
  }

  record Using(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Optional<Token.Identifier> newName,
    Mention aliased) implements Declaration
  {
    @Override
    public Identifier name() {
      return newName.getOrElse(aliased.identifiers.getLast());
    }
  }

  record Struct(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Token.Identifier name,
    List<Binding> members) implements Declaration
  {}

  record Const(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Token.Identifier name,
    Formula type,
    Expression value) implements Declaration
  {}

  record Var(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Token.Identifier name,
    Formula type,
    Expression initialValue) implements Declaration
  {}

  record Fn(
    Location location,
    Optional<Token.StringConstant> externalName,
    boolean isPublic,
    Token.Identifier name,
    List<Binding> parameters,
    Formula returnType,
    Optional<Statement> body) implements Declaration
  {}

  record Binding(Location location, Token.Identifier name, Formula type)
    implements Node
  {}

  sealed interface Statement extends Node {}

  record Block(Location location, List<Statement> innerStatements)
    implements Statement
  {}

  record If(
    Location location,
    List<Declare> initializationStatements,
    Expression condition,
    Statement trueBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record For(
    Location location,
    Optional<Token.Identifier> label,
    List<Declare> initializationStatements,
    Expression condition,
    Optional<Affect> interleavedStatement,
    Statement loopBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record Break(Location location, Optional<Token.Identifier> label)
    implements Statement
  {}

  record Continue(Location location, Optional<Token.Identifier> label)
    implements Statement
  {}

  record Return(Location location, Optional<Expression> value)
    implements Statement
  {}

  record Declare(
    Location location,
    Token.Identifier name,
    Optional<Formula> type,
    Expression initialValue) implements Statement
  {}

  sealed interface Affect extends Statement {}

  record Discard(Location location, Expression source) implements Affect {}

  sealed interface Mutate extends Affect {
    Expression target();
  }

  record Increment(Location location, Expression target) implements Mutate {}

  record Decrement(Location location, Expression target) implements Mutate {}

  sealed interface BaseAssign extends Affect {
    Expression target();
    Expression source();
  }

  record Assign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record MultiplyAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record QuotientAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record ReminderAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record AddAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record SubtractAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record ShiftLeftAssign(
    Location location,
    Expression target,
    Expression source) implements BaseAssign
  {}

  record ShiftRightAssign(
    Location location,
    Expression target,
    Expression source) implements BaseAssign
  {}

  record AndAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record XorAssign(Location location, Expression target, Expression source)
    implements BaseAssign
  {}

  record OrAssign(Location location, Expression target, Expression source)
    implements BaseAssign
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

  sealed interface BinaryOperator extends Expression {
    Expression leftOperand();
    Expression rightOperand();
  }

  record LogicalOr(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LogicalAnd(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record NotEqualTo(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record EqualTo(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record GreaterThanOrEqualTo(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record GreaterThan(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LessThanOrEqualTo(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LessThan(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record BitwiseOr(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record BitwiseXor(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record BitwiseAnd(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record RightShift(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LeftShift(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Subtraction(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Addition(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Reminder(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Quotient(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Multiplication(
    Location location,
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  sealed interface UnaryOperator extends Expression {
    Expression operand();
  }

  record LogicalNot(Location location, Expression operand)
    implements UnaryOperator
  {}

  record BitwiseNot(Location location, Expression operand)
    implements UnaryOperator
  {}

  record Negation(Location location, Expression operand)
    implements UnaryOperator
  {}

  record Promotion(Location location, Expression operand)
    implements UnaryOperator
  {}

  record MemberAccess(
    Location location,
    Expression object,
    Token.Identifier member) implements Expression
  {}

  record InfixCall(
    Location location,
    Expression firstArgument,
    Expression callee,
    List<Expression> remainingArguments) implements Expression
  {}

  record PostfixCall(
    Location location,
    Expression callee,
    List<Expression> arguments) implements Expression
  {}

  record Initialization(
    Location location,
    Mention type,
    List<MemberInitialization> memberInitializations) implements Expression
  {}

  record MemberInitialization(
    Location location,
    Token.Identifier member,
    Expression value) implements Node
  {}

  record Cast(Location location, Mention type, Expression source)
    implements Expression
  {}

  record Access(Mention mention) implements Expression {
    @Override
    public Location location() {
      return mention.location();
    }
  }

  record Grouping(Location location, Expression grouped)
    implements Expression
  {}

  record NumberConstant(Token.NumberConstant value) implements Expression {
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

  record Mention(Location location, List<Token.Identifier> identifiers)
    implements Node
  {
    public Name toName() {
      return new Name(identifiers.transform(Token.Identifier::text));
    }
  }

  Location location();
}
