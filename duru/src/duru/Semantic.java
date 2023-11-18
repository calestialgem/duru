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
    String packageName();
    boolean isPublic();
    String name();
  }

  sealed interface Builtin extends Symbol {
    @Override
    default String packageName() {
      return "duru";
    }

    @Override
    default boolean isPublic() {
      return true;
    }
  }

  sealed interface Type extends Semantic {}

  record Struct(String packageName, boolean isPublic, String name)
    implements Type, Symbol
  {}

  record Byte() implements Type, Builtin {
    @Override
    public String name() {
      return "Byte";
    }
  }

  record Natural32() implements Type, Builtin {
    @Override
    public String name() {
      return "Natural32";
    }
  }

  record Integer32() implements Type, Builtin {
    @Override
    public String name() {
      return "Integer32";
    }
  }

  record Noreturn() implements Type, Builtin {
    @Override
    public String name() {
      return "Noreturn";
    }
  }

  record Pointer(Type pointee) implements Type {}

  sealed interface Procedure extends Semantic {
    Map<String, Type> parameters();
    Optional<Type> returnType();
  }

  record Proc(
    String packageName,
    boolean isPublic,
    String name,
    Map<String, Type> parameters,
    Optional<Type> returnType,
    Statement body) implements Procedure, Symbol
  {}

  record ExternalProc(
    String packageName,
    boolean isPublic,
    String name,
    Map<String, Type> parameters,
    Optional<Type> returnType,
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

  record Var(String name, Optional<Type> type, Expression initialValue)
    implements Statement
  {}

  record Discard(Expression discarded) implements Statement {}

  sealed interface Expression extends Semantic {}

  record LessThan(Expression left, Expression right) implements Expression {}

  record Invocation(String packageName, String name, List<Expression> arguments)
    implements Expression
  {}

  record NaturalConstant(long value) implements Expression {}

  record StringConstant(String value) implements Expression {}

  record LocalAccess(String name) implements Expression {}
}
