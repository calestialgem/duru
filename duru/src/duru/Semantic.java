package duru;

import java.math.BigInteger;

public sealed interface Semantic {
  record Target(String main, Map<String, Module> modules) {}

  record Module(String name, Map<Name, Package> packages) implements Semantic {}

  sealed interface Package extends Semantic {
    Name name();
    Map<String, Symbol> symbols();
  }

  record Executable(Name name, Map<String, Symbol> symbols)
    implements Package
  {}

  record Library(Name name, Map<String, Symbol> symbols) implements Package {}

  record Implementation(Name name, Map<String, Symbol> symbols)
    implements Package
  {}

  sealed interface Builtin extends Symbol {
    String external();
    String identifier();

    @Override
    default Optional<String> externalName() {
      return Optional.present(external());
    }

    @Override
    default boolean isPublic() {
      return true;
    }

    @Override
    default Name name() {
      return Name.of("duru", identifier());
    }
  }

  sealed interface Type extends Semantic {
    default boolean coerces(Type target) {
      return equals(target);
    }
  }

  sealed interface Arithmetic extends Type {}

  sealed interface ConstantArithmetic extends Arithmetic {}

  sealed interface Integral extends Arithmetic {
    BigInteger max();
    Expression constant(BigInteger value);

    default boolean canRepresent(BigInteger value) {
      return value.compareTo(max()) <= 0;
    }
  }

  sealed interface Integer extends Integral {}

  sealed interface Natural extends Integral {}

  record Byte() implements Arithmetic, Builtin {
    @Override
    public String external() {
      return "char";
    }

    @Override
    public String identifier() {
      return "Byte";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Boolean() implements Arithmetic, Builtin {
    @Override
    public String external() {
      return "_Bool";
    }

    @Override
    public String identifier() {
      return "Boolean";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record ConstantReal() implements ConstantArithmetic {}
  record ConstantIntegral() implements ConstantArithmetic {}

  record Natural32() implements Natural, Builtin {
    @Override
    public String external() {
      return "unsigned";
    }

    @Override
    public BigInteger max() {
      return BigInteger.valueOf(0xffff_ffffL);
    }

    @Override
    public Natural32Constant constant(BigInteger value) {
      return new Natural32Constant(value);
    }

    @Override
    public String identifier() {
      return "Natural32";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Natural64() implements Natural, Builtin {
    @Override
    public String external() {
      return "unsigned long long";
    }

    @Override
    public BigInteger max() {
      return BigInteger.valueOf(0xffff_ffff_ffff_ffffL);
    }

    @Override
    public Natural64Constant constant(BigInteger value) {
      return new Natural64Constant(value);
    }

    @Override
    public String identifier() {
      return "Natural64";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Integer32() implements Integer, Builtin {
    @Override
    public String external() {
      return "int";
    }

    @Override
    public BigInteger max() {
      return BigInteger.valueOf(0x7fff_ffffL);
    }

    @Override
    public Integer32Constant constant(BigInteger value) {
      return new Integer32Constant(value);
    }

    @Override
    public String identifier() {
      return "Integer32";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Void() implements Type, Builtin {
    @Override
    public String external() {
      return "void";
    }

    @Override
    public String identifier() {
      return "Void";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Noreturn() implements Type, Builtin {
    @Override
    public String external() {
      return "[[noreturn]] void";
    }

    @Override
    public String identifier() {
      return "Noreturn";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Pointer(Type pointee) implements Type {
    @Override
    public String toString() {
      return "*%s".formatted(pointee);
    }
  }

  sealed interface Symbol extends Semantic {
    Optional<String> externalName();
    boolean isPublic();
    Name name();
  }

  record Using(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Name aliased) implements Symbol
  {
    @Override public String toString() { return name.toString(); }
  }

  record Struct(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Map<String, Type> members) implements Symbol, Type
  {
    @Override public String toString() { return name.toString(); }
  }

  sealed interface GlobalVariable extends Symbol {
    Name name();
    Type type();
  }

  record Const(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Type type,
    Expression value) implements GlobalVariable
  {
    @Override public String toString() { return name.toString(); }
  }

  record Var(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Type type,
    Expression initialValue) implements GlobalVariable
  {
    @Override public String toString() { return name.toString(); }
  }

  record Fn(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Map<String, Type> parameters,
    Type returnType,
    Optional<Statement> body) implements Symbol
  {
    @Override public String toString() { return name.toString(); }
  }

  sealed interface Statement extends Semantic {}

  record Block(List<Statement> innerStatements)
    implements Statement
  {}

  record If(
    List<Declare> initializationStatements,
    Expression condition,
    Statement trueBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record For(
    Optional<String> label,
    List<Declare> initializationStatements,
    Expression condition,
    Optional<Affect> interleavedStatement,
    Statement loopBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record Break(Optional<String> label)
    implements Statement
  {}

  record Continue(Optional<String> label)
    implements Statement
  {}

  record Return(Optional<Expression> value)
    implements Statement
  {}

  record Declare(
    String name,
    Type type,
    Expression initialValue) implements Statement
  {}

  sealed interface Affect extends Statement {}

  record Discard(Expression source) implements Affect {}

  sealed interface Mutate extends Affect {
    Expression target();
  }

  record Increment(Expression target) implements Mutate {}

  record Decrement(Expression target) implements Mutate {}

  sealed interface BaseAssign extends Affect {
    Expression target();
    Expression source();
  }

  record Assign(Expression target, Expression source)
    implements BaseAssign
  {}

  record MultiplyAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record QuotientAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record ReminderAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record AddAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record SubtractAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record ShiftLeftAssign(
    Expression target,
    Expression source) implements BaseAssign
  {}

  record ShiftRightAssign(
    Expression target,
    Expression source) implements BaseAssign
  {}

  record AndAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record XorAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record OrAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  sealed interface Expression extends Semantic {}

  sealed interface BinaryOperator extends Expression {
    Expression leftOperand();
    Expression rightOperand();
  }

  record LogicalOr(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LogicalAnd(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record NotEqualTo(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record EqualTo(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record GreaterThanOrEqualTo(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record GreaterThan(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LessThanOrEqualTo(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LessThan(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record BitwiseOr(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record BitwiseXor(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record BitwiseAnd(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record RightShift(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record LeftShift(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Subtraction(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Addition(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Reminder(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Quotient(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  record Multiplication(
    Expression leftOperand,
    Expression rightOperand) implements BinaryOperator
  {}

  sealed interface UnaryOperator extends Expression {
    Expression operand();
  }

  record LogicalNot(Expression operand)
    implements UnaryOperator
  {}

  record BitwiseNot(Expression operand)
    implements UnaryOperator
  {}

  record Negation(Expression operand)
    implements UnaryOperator
  {}

  record Promotion(Expression operand)
    implements UnaryOperator
  {}

  record MemberAccess(
    Expression object,
    String member) implements Expression
  {}

  record InfixCall(
    Expression firstArgument,
    Name callee,
    List<Expression> remainingArguments) implements Expression
  {}

  record PostfixCall(
    Expression callee,
    List<Expression> arguments) implements Expression
  {}

  record Initialization(
    Type type,
    List<Expression> members) implements Expression
  {}

  record GlobalAccess(Name name) implements Expression {}

  record LocalAccess(String name) implements Expression {}

  record Grouping(Expression grouped)
    implements Expression
  {}

  record RealConstant(BigDecimal value) implements Expression {}

  record IntegralConstant(BigInteger value) implements Expression {}

  record Integer32Constant(BigInteger value) implements Expression {}

  record Natural32Constant(BigInteger value) implements Expression {}

  record Natural64Constant(BigInteger value) implements Expression {}

  record StringConstant(String value) implements Expression {}

  Byte BYTE = new Byte();
  Boolean BOOLEAN = new Boolean();
  Natural32 NATURAL_32 = new Natural32();
  Integer32 INTEGER_32 = new Integer32();
  Void VOID = new Void();
  Noreturn NORETURN = new Noreturn();

  List<Builtin> BUILTINS = List.of(
    BYTE,
    BOOLEAN,
    NATURAL_32,
    INTEGER_32,
    VOID,
    NORETURN
  );

  Pointer BYTE_POINTER = new Pointer(BYTE);
  ConstantIntegral CONSTANT_INTEGRAL = new ConstantIntegral();
  ConstantReal CONSTANT_REAL = new ConstantReal();
}
