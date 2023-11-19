package duru;

public sealed interface Semantic {
  record Target(String main, Map<String, Module> modules) {}

  record Module(String name, Map<String, Package> packages)
    implements Semantic
  {}

  sealed interface Package extends Semantic {
    String name();
    Map<String, Symbol> symbols();
  }

  record Executable(String name, Map<String, Symbol> symbols)
    implements Package
  {}

  record Library(String name, Map<String, Symbol> symbols) implements Package {}

  record Implementation(String name, Map<String, Symbol> symbols)
    implements Package
  {}

  sealed interface Symbol extends Semantic {
    boolean isPublic();
    String name();
  }

  sealed interface Builtin extends Symbol {
    String identifier();

    @Override
    default boolean isPublic() {
      return true;
    }

    @Override
    default String name() {
      return "duru.%s".formatted(identifier());
    }
  }

  sealed interface Type extends Semantic {}

  sealed interface Arithmetic extends Type {}

  record Struct(boolean isPublic, String name) implements Type, Symbol {
    @Override
    public String toString() {
      return name();
    }
  }

  record Byte() implements Arithmetic, Builtin {
    @Override
    public String identifier() {
      return "Byte";
    }

    @Override
    public String toString() {
      return name();
    }
  }

  record Boolean() implements Arithmetic, Builtin {
    @Override
    public String identifier() {
      return "Boolean";
    }

    @Override
    public String toString() {
      return name();
    }
  }

  record Natural32() implements Arithmetic, Builtin {
    @Override
    public String identifier() {
      return "Natural32";
    }

    @Override
    public String toString() {
      return name();
    }
  }

  record Integer32() implements Arithmetic, Builtin {
    @Override
    public String identifier() {
      return "Integer32";
    }

    @Override
    public String toString() {
      return name();
    }
  }

  record Unit() implements Type, Builtin {
    @Override
    public String identifier() {
      return "Unit";
    }

    @Override
    public String toString() {
      return name();
    }
  }

  record Noreturn() implements Type, Builtin {
    @Override
    public String identifier() {
      return "Noreturn";
    }

    @Override
    public String toString() {
      return name();
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
    boolean isPublic,
    String name,
    Map<String, Type> parameters,
    Type returnType,
    Statement body) implements Procedure, Symbol
  {}

  record ExternalProc(
    boolean isPublic,
    String name,
    Map<String, Type> parameters,
    Type returnType,
    String externalName) implements Procedure, Symbol
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

  record Invocation(String name, List<Expression> arguments)
    implements Expression
  {}

  record NaturalConstant(long value) implements Expression {}

  record StringConstant(String value) implements Expression {}

  record LocalAccess(String name) implements Expression {}
}
