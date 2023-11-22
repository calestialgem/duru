package duru;

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

  sealed interface Symbol extends Semantic {
    Optional<String> externalName();
    boolean isPublic();
    Name name();
  }

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

  sealed interface Integral extends Arithmetic {
    long max();
    Expression constant(long value);

    default boolean canRepresent(long value) {
      return Long.compareUnsigned(value, max()) <= 0;
    }
  }

  sealed interface Integer extends Integral {}

  sealed interface Natural extends Integral {}

  record Struct(Optional<String> externalName, boolean isPublic, Name name)
    implements Type, Symbol
  {
    @Override
    public String toString() {
      return name().toString();
    }
  }

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

  record ConstantIntegral() implements Integral {
    @Override
    public long max() {
      return -1L;
    }

    @Override
    public IntegralConstant constant(long value) {
      return new IntegralConstant(value);
    }

    @Override
    public String toString() {
      return "constant integral";
    }
  }

  record Natural32() implements Natural, Builtin {
    @Override
    public String external() {
      return "unsigned";
    }

    @Override
    public long max() {
      return -1;
    }

    @Override
    public Natural32Constant constant(long value) {
      return new Natural32Constant((int) value);
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
    public long max() {
      return -1L;
    }

    @Override
    public Natural64Constant constant(long value) {
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
    public long max() {
      return java.lang.Integer.MAX_VALUE;
    }

    @Override
    public Integer32Constant constant(long value) {
      return new Integer32Constant((int) value);
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

  record Unit() implements Type, Builtin {
    @Override
    public String external() {
      return "char";
    }

    @Override
    public String identifier() {
      return "Unit";
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

  sealed interface Procedure extends Semantic {
    Map<String, Type> parameters();
    Type returnType();
  }

  record Proc(
    Optional<String> externalName,
    boolean isPublic,
    Name name,
    Map<String, Type> parameters,
    Type returnType,
    Optional<Statement> body) implements Procedure, Symbol
  {}

  sealed interface Statement extends Semantic {}

  record Block(List<Statement> innerStatements) implements Statement {}

  record If(
    Expression condition,
    Statement trueBranch,
    Optional<Statement> falseBranch) implements Statement
  {}

  record Return(Optional<Expression> value) implements Statement {}

  record Var(String name, Type type, Expression initialValue)
    implements Statement
  {}

  record Discard(Expression discarded) implements Statement {}

  sealed interface Expression extends Semantic {}

  record LessThan(Expression left, Expression right) implements Expression {}

  record Invocation(Name name, List<Expression> arguments)
    implements Expression
  {}

  record UnitConstant() implements Expression {}

  record IntegralConstant(long value) implements Expression {}

  record Integer32Constant(int value) implements Expression {}

  record Natural32Constant(int value) implements Expression {}

  record Natural64Constant(long value) implements Expression {}

  record StringConstant(String value) implements Expression {}

  record LocalAccess(String name) implements Expression {}

  Boolean      BOOLEAN       = new Boolean();
  UnitConstant UNIT_CONSTANT = new UnitConstant();
  Return       UNIT_RETURN   = new Return(Optional.present(UNIT_CONSTANT));
}
