package duru;

import java.nio.file.Path;

import duru.Semantic.Symbol;

public final class Builder {
  public static void build(
    Object subject,
    Path artifacts,
    Semantic.Target target)
  {
    var builder = new Builder(subject, artifacts, target);
    builder.build();
  }

  private final Object          subject;
  private final Path            artifacts;
  private final Semantic.Target target;
  private StringBuilder         string;
  private SetBuffer<String>     built;
  private int                   indentation;

  private Builder(Object subject, Path artifacts, Semantic.Target target) {
    this.subject   = subject;
    this.artifacts = artifacts;
    this.target    = target;
  }

  private void build() {
    string = new StringBuilder();
    built  = SetBuffer.create();
    var mainModule = target.modules().get(target.main()).getFirst();
    for (var package_ : mainModule.packages().values()) {
      string.setLength(0);
      built.clear();
      indentation = 0;
      switch (package_) {
        case Semantic.Executable executable ->
          buildSymbol(executable.symbols().get("main").getFirst());
        case Semantic.Library library -> {}
        case Semantic.Implementation implementation -> {}
      }
      Persistance
        .store(
          subject,
          artifacts.resolve("%s.c".formatted(package_.name())),
          string);
    }
  }

  private void buildAccess(String name) {
    var symbol = accessSymbol(name);
    switch (symbol) {
      case Semantic.ExternalProc proc -> string.append(proc.externalName());
      default -> string.append(symbol.name().replace('.', '$'));
    }
  }

  private void buildSymbol(String name) {
    buildSymbol(accessSymbol(name));
  }

  private Symbol accessSymbol(String name) {
    var module   = target.modules().get(Text.getModule(name)).getFirst();
    var package_ = module.packages().get(Text.getPackage(name)).getFirst();
    var symbol   = package_.symbols().get(Text.getSymbol(name)).getFirst();
    return symbol;
  }

  private void buildSymbol(Semantic.Symbol symbol) {
    if (built.contains(symbol.name()))
      return;
    built.add(symbol.name());
    switch (symbol) {
      case Semantic.Proc proc -> buildProc(proc);
      case Semantic.ExternalProc proc -> buildExternalProc(proc);
      case Semantic.Struct struct -> buildStruct(struct);
      case Semantic.Byte byte_ -> string.append("typedef char duru$Byte;");
      case Semantic.Boolean boolean_ ->
        string.append("typedef _Bool duru$Boolean;");
      case Semantic.Natural32 natural32 ->
        string.append("typedef unsigned duru$Natural32;");
      case Semantic.Natural64 natural64 ->
        string.append("typedef unsigned long long duru$Natural64;");
      case Semantic.Integer32 integer32 ->
        string.append("typedef int duru$Integer32;");
      case Semantic.Unit unit -> string.append("typedef char duru$Unit;");
      case Semantic.Noreturn noreturn ->
        string.append("#define duru$Noreturn [[noreturn]] void");
    }
    buildNewLine();
  }

  private void buildProc(Semantic.Proc proc) {
    for (var parameter : proc.parameters().values())
      buildTypeDependencies(parameter);
    buildTypeDependencies(proc.returnType());
    buildStatementDependencies(proc.body());
    buildType(proc.returnType());
    string.append(' ');
    buildAccess(proc.name());
    string.append('(');
    if (!proc.parameters().isEmpty()) {
      buildType(proc.parameters().values().getFirst());
      string.append(' ');
      string.append(proc.parameters().keys().getFirst());
      for (var i = 1; i < proc.parameters().length(); i++) {
        string.append(',');
        string.append(' ');
        buildType(proc.parameters().values().get(i));
        string.append(' ');
        string.append(proc.parameters().keys().get(i));
      }
    }
    string.append(')');
    string.append(' ');
    buildStatement(proc.body());
  }

  private void buildExternalProc(Semantic.ExternalProc proc) {
    for (var parameter : proc.parameters().values())
      buildTypeDependencies(parameter);
    buildTypeDependencies(proc.returnType());
    buildType(proc.returnType());
    string.append(' ');
    string.append(proc.externalName());
    string.append('(');
    if (!proc.parameters().isEmpty()) {
      buildType(proc.parameters().values().getFirst());
      string.append(' ');
      string.append(proc.parameters().keys().getFirst());
      for (var i = 1; i < proc.parameters().length(); i++) {
        string.append(',');
        string.append(' ');
        buildType(proc.parameters().values().get(i));
        string.append(' ');
        string.append(proc.parameters().keys().get(i));
      }
    }
    string.append(')');
    string.append(';');
  }

  private void buildStruct(Semantic.Struct struct) {
    string.append("typedef struct ");
    buildAccess(struct.name());
    string.append(' ');
    buildAccess(struct.name());
    string.append(';');
  }

  private void buildTypeDependencies(Semantic.Type type) {
    switch (type) {
      case Semantic.Symbol symbol -> buildSymbol(symbol);
      case Semantic.Pointer pointer -> buildTypeDependencies(pointer.pointee());
      case Semantic.Arithmetic arithmetic ->
        throw Diagnostic
          .failure(subject, "cannot build compile-time type `%s`", arithmetic);
    }
  }

  private void buildStatementDependencies(Semantic.Statement statement) {
    switch (statement) {
      case Semantic.Block block -> {
        for (var innerStatement : block.innerStatements())
          buildStatementDependencies(innerStatement);
      }
      case Semantic.Discard discard ->
        buildExpressionDependencies(discard.discarded());
      case Semantic.If if_ -> {
        buildExpressionDependencies(if_.condition());
        buildStatementDependencies(if_.trueBranch());
        for (var falseBranch : if_.falseBranch())
          buildStatementDependencies(falseBranch);
      }
      case Semantic.Return return_ -> {
        for (var value : return_.value())
          buildExpressionDependencies(value);
      }
      case Semantic.Var var -> {
        buildTypeDependencies(var.type());
        buildExpressionDependencies(var.initialValue());
      }
    }
  }

  private void buildExpressionDependencies(Semantic.Expression expression) {
    switch (expression) {
      case Semantic.Integer32Constant constant -> {}
      case Semantic.IntegralConstant constant ->
        throw Diagnostic
          .failure(
            subject,
            "cannot build compile-time constant `%s`",
            Long.toUnsignedString(constant.value()));
      case Semantic.Invocation invocation -> {
        buildSymbol(invocation.name());
        for (var argument : invocation.arguments())
          buildExpressionDependencies(argument);
      }
      case Semantic.LessThan binary -> {
        buildExpressionDependencies(binary.left());
        buildExpressionDependencies(binary.right());
      }
      case Semantic.LocalAccess access -> {}
      case Semantic.Natural32Constant constant -> {}
      case Semantic.Natural64Constant constant -> {}
      case Semantic.StringConstant constant -> {}
    }
  }

  private void buildType(Semantic.Type type) {
    switch (type) {
      case Semantic.Symbol symbol -> buildAccess(symbol.name());
      case Semantic.Pointer pointer -> {
        buildType(pointer.pointee());
        string.append('*');
      }
      case Semantic.Arithmetic arithmetic ->
        throw Diagnostic
          .failure(subject, "cannot build compile-time type `%s`", arithmetic);
    }
  }

  private void buildStatement(Semantic.Statement statement) {
    switch (statement) {
      case Semantic.Block block -> {
        string.append('{');
        indentation++;
        for (var innerStatement : block.innerStatements()) {
          buildNewLine();
          buildStatement(innerStatement);
        }
        indentation--;
        if (string.charAt(string.length() - 1) != '{')
          buildNewLine();
        string.append('}');
      }
      case Semantic.Discard discard -> {
        buildExpression(discard.discarded());
        string.append(';');
      }
      case Semantic.If if_ -> {
        string.append("if (");
        buildExpression(if_.condition());
        string.append(')');
        string.append(' ');
        buildStatement(if_.trueBranch());
        for (var falseBranch : if_.falseBranch()) {
          buildNewLine();
          string.append("else ");
          buildStatement(falseBranch);
        }
      }
      case Semantic.Return return_ -> {
        string.append("return");
        for (var value : return_.value()) {
          string.append(' ');
          buildExpression(value);
        }
        string.append(';');
      }
      case Semantic.Var var -> {
        buildType(var.type());
        string.append(' ');
        string.append(var.name());
        string.append(' ');
        string.append('=');
        string.append(' ');
        buildExpression(var.initialValue());
        string.append(';');
      }
    }
  }

  private void buildExpression(Semantic.Expression expression) {
    switch (expression) {
      case Semantic.Integer32Constant constant ->
        string.append(Integer.toUnsignedString(constant.value()));
      case Semantic.IntegralConstant constant ->
        throw Diagnostic
          .failure(
            subject,
            "cannot build compile-time constant `%s`",
            Long.toUnsignedString(constant.value()));
      case Semantic.Invocation invocation -> {
        buildAccess(invocation.name());
        string.append('(');
        if (!invocation.arguments().isEmpty()) {
          buildExpression(invocation.arguments().getFirst());
          for (var i = 1; i < invocation.arguments().length(); i++) {
            string.append(',');
            string.append(' ');
            buildExpression(invocation.arguments().get(i));
          }
        }
        string.append(')');
      }
      case Semantic.LessThan binary -> {
        buildExpression(binary.left());
        string.append(' ');
        string.append('<');
        string.append(' ');
        buildExpression(binary.right());
      }
      case Semantic.LocalAccess access -> string.append(access.name());
      case Semantic.Natural32Constant constant ->
        string.append(Integer.toUnsignedString(constant.value()));
      case Semantic.Natural64Constant constant ->
        string.append(Long.toUnsignedString(constant.value()));
      case Semantic.StringConstant constant ->
        Text.quote(string, constant.value());
    }
  }

  private void buildNewLine() {
    string.append(System.lineSeparator());
    for (var i = 0; i < indentation; i++)
      string.append(' ').append(' ');
  }
}
