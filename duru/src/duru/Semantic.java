package duru;

public sealed interface Semantic {
  record Target(Module main, Map<String, Module> dependencies) {}

  record Module(
    String name,
    Map<String, Package> packages,
    Set<String> executables) implements Semantic
  {}

  record Package(
    boolean isExecutable,
    String name,
    Map<String, Symbol> symbols,
    Set<String> publics) implements Semantic
  {}

  sealed interface Symbol extends Semantic {
    boolean isPublic();
    String name();
  }

  record Function(
    boolean isPublic,
    Type returnType,
    String name,
    Map<String, Type> parameters,
    Statement body) implements Symbol
  {}

  sealed interface Statement extends Semantic {}

  record Block(List<Statement> innerStatements) implements Statement {}

  record Discard(Expression discarded) implements Statement {}

  sealed interface Expression extends Semantic {}

  record Invocation(Function callee, List<Expression> arguments)
    implements Expression
  {}

  record StringConstant(String value) implements Expression {}

  sealed interface Type extends Semantic {}

  record Void() implements Type {}

  record ConstantString() implements Type {}
}
