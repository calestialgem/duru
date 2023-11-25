package duru;

import java.math.BigDecimal;
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

  sealed interface Alias extends Symbol {
    Name aliased();
  }

  sealed interface Type extends Semantic {}

  sealed interface Constant extends Type {}

  record ConstantString() implements Constant {
    @Override
    public String toString() {
      return "constant-string";
    }
  }

  sealed interface Arithmetic extends Type {}

  sealed interface Real extends Arithmetic {
    boolean canRepresent(BigDecimal value);

    default boolean canRepresent(BigInteger value) {
      return canRepresent(new BigDecimal(value));
    }
  }

  record Float32() implements Real, Builtin {
    @Override
    public String external() {
      return "float";
    }

    @Override
    public String identifier() {
      return "Float_32";
    }

    @Override
    public boolean canRepresent(BigDecimal value) {
      return Float.isFinite(value.floatValue());
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Float64() implements Real, Builtin {
    @Override
    public String external() {
      return "float";
    }

    @Override
    public String identifier() {
      return "Float_64";
    }

    @Override
    public boolean canRepresent(BigDecimal value) {
      return Double.isFinite(value.doubleValue());
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  sealed interface Integral extends Arithmetic {
    int width();
    boolean isSigned();

    default BigInteger max() {
      var magnitude = width();
      if (isSigned()) {
        magnitude--;
      }
      return BigInteger.ONE.shiftLeft(magnitude).subtract(BigInteger.ONE);
    }

    default boolean canRepresent(BigInteger value) {
      return value.compareTo(max()) <= 0;
    }
  }

  sealed interface Natural extends Integral {
    @Override
    default boolean isSigned() {
      return false;
    }
  }

  record Boolean() implements Natural, Builtin {
    @Override
    public int width() {
      return 1;
    }

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

  record ConstantReal() implements Constant {
    @Override
    public String toString() {
      return "constant-real";
    }
  }

  record ConstantIntegral() implements Constant {
    @Override
    public String toString() {
      return "constant-integral";
    }
  }

  record Natural8() implements Natural, Builtin {
    @Override
    public int width() {
      return 8;
    }

    @Override
    public String external() {
      return "unsigned char";
    }

    @Override
    public String identifier() {
      return "Natural_8";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Natural16() implements Natural, Builtin {
    @Override
    public int width() {
      return 16;
    }

    @Override
    public String external() {
      return "unsigned short";
    }

    @Override
    public String identifier() {
      return "Natural_16";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Natural32() implements Natural, Builtin {
    @Override
    public int width() {
      return 32;
    }

    @Override
    public String external() {
      return "unsigned";
    }

    @Override
    public String identifier() {
      return "Natural_32";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Natural64() implements Natural, Builtin {
    @Override
    public int width() {
      return 64;
    }

    @Override
    public String external() {
      return "unsigned long long";
    }

    @Override
    public String identifier() {
      return "Natural_64";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  sealed interface Integer extends Integral {
    @Override
    default boolean isSigned() {
      return true;
    }
  }

  record Integer8() implements Integer, Builtin {
    @Override
    public int width() {
      return 8;
    }

    @Override
    public String external() {
      return "signed char";
    }

    @Override
    public String identifier() {
      return "Integer_8";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Integer16() implements Integer, Builtin {
    @Override
    public int width() {
      return 16;
    }

    @Override
    public String external() {
      return "short";
    }

    @Override
    public String identifier() {
      return "Integer_16";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Integer32() implements Integer, Builtin {
    @Override
    public int width() {
      return 32;
    }

    @Override
    public String external() {
      return "int";
    }

    @Override
    public String identifier() {
      return "Integer_32";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Integer64() implements Integer, Builtin {
    @Override
    public int width() {
      return 64;
    }

    @Override
    public String external() {
      return "int";
    }

    @Override
    public String identifier() {
      return "Integer_64";
    }

    @Override
    public String toString() {
      return name().toString();
    }
  }

  record Byte() implements Alias, Builtin {
    @Override
    public Name aliased() {
      return NATURAL_8.name();
    }

    @Override
    public String external() {
      return NATURAL_8.external();
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

  record Pointer(Type pointee) implements Natural {
    @Override
    public int width() {
      return 64;
    }

    @Override
    public String toString() {
      return "*%s".formatted(pointee);
    }
  }

  record Callable(List<Type> parameters, Type returnType) implements Type {}

  sealed interface Symbol extends Semantic {
    Optional<String> externalName();
    boolean isPublic();
    Name name();
  }

  record Using(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Name aliased) implements Alias
  {
    @Override
    public String toString() {
      return name.toString();
    }
  }

  record Struct(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Map<String, Type> members) implements Symbol, Type
  {
    @Override
    public String toString() {
      return name.toString();
    }
  }

  record Const(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Type type,
    Expression value) implements Symbol
  {
    @Override
    public String toString() {
      return name.toString();
    }
  }

  record Var(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Type type,
    Expression initialValue) implements Symbol
  {
    @Override
    public String toString() {
      return name.toString();
    }
  }

  record Fn(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Map<String, Type> parameters,
    Type returnType,
    Optional<Statement> body) implements Symbol
  {
    @Override
    public String toString() {
      return name.toString();
    }
  }

  sealed interface Statement extends Semantic {}

  record Block(List<Statement> innerStatements) implements Statement {}

  record If(
    List<Statement> initializationStatements,
    Expression condition,
    Statement trueBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record For(
    Optional<String> label,
    List<Statement> initializationStatements,
    Expression condition,
    Optional<Statement> interleavedStatement,
    Statement loopBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record Break(Optional<String> label) implements Statement {}

  record Continue(Optional<String> label) implements Statement {}

  record Return(Optional<Expression> value) implements Statement {}

  record Declare(String name, Type type, Expression initialValue)
    implements Statement
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

  record Assign(Expression target, Expression source) implements BaseAssign {}

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

  record ShiftLeftAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record ShiftRightAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record AndAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record XorAssign(Expression target, Expression source)
    implements BaseAssign
  {}

  record OrAssign(Expression target, Expression source) implements BaseAssign {}

  sealed interface Expression extends Semantic {}

  sealed interface BinaryOperator extends Expression {
    Expression leftOperand();
    Expression rightOperand();
  }

  record LogicalOr(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record LogicalAnd(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record NotEqualTo(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record EqualTo(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record GreaterThanOrEqualTo(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record GreaterThan(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record LessThanOrEqualTo(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record LessThan(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record BitwiseOr(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record BitwiseXor(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record BitwiseAnd(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record RightShift(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record LeftShift(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record Subtraction(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record Addition(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record Reminder(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record Quotient(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  record Multiplication(Expression leftOperand, Expression rightOperand)
    implements BinaryOperator
  {}

  sealed interface UnaryOperator extends Expression {
    Expression operand();
  }

  record LogicalNot(Expression operand) implements UnaryOperator {}

  record BitwiseNot(Expression operand) implements UnaryOperator {}

  record Negation(Expression operand) implements UnaryOperator {}

  record Promotion(Expression operand) implements UnaryOperator {}

  record MemberAccess(Expression object, String member) implements Expression {}

  record Calling(Expression callee, List<Expression> arguments)
    implements Expression
  {}

  record Initialization(Type type, List<Expression> members)
    implements Expression
  {}

  record GlobalAccess(Name name) implements Expression {}

  record LocalAccess(String name) implements Expression {}

  record Grouping(Expression grouped) implements Expression {}

  record RealConstant(BigDecimal value) implements Expression {}

  record IntegralConstant(BigInteger value) implements Expression {}

  record StringConstant(String value) implements Expression {}

  record Conversion(Expression source, Type target) implements Expression {}

  Float32          FLOAT_32          = new Float32();
  Float64          FLOAT_64          = new Float64();
  Boolean          BOOLEAN           = new Boolean();
  Natural8         NATURAL_8         = new Natural8();
  Natural16        NATURAL_16        = new Natural16();
  Natural32        NATURAL_32        = new Natural32();
  Natural64        NATURAL_64        = new Natural64();
  Integer8         INTEGER_8         = new Integer8();
  Integer16        INTEGER_16        = new Integer16();
  Integer32        INTEGER_32        = new Integer32();
  Integer64        INTEGER_64        = new Integer64();
  Byte             BYTE              = new Byte();
  Void             VOID              = new Void();
  Noreturn         NORETURN          = new Noreturn();
  List<Builtin>    BUILTINS          =
    List
      .of(
        FLOAT_32,
        FLOAT_64,
        BOOLEAN,
        NATURAL_8,
        NATURAL_16,
        NATURAL_32,
        NATURAL_64,
        INTEGER_8,
        INTEGER_16,
        INTEGER_32,
        INTEGER_64,
        BYTE,
        VOID,
        NORETURN);
  Pointer          BYTE_POINTER      = new Pointer(NATURAL_8);
  ConstantString   CONSTANT_STRING   = new ConstantString();
  ConstantIntegral CONSTANT_INTEGRAL = new ConstantIntegral();
  ConstantReal     CONSTANT_REAL     = new ConstantReal();
  IntegralConstant ZERO              = new IntegralConstant(BigInteger.ZERO);
}
