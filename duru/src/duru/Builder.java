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
  private SetBuffer<Name>       built;
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
        case Semantic.Executable executable -> {
          var mainProcedure = executable.name().scope("main");
          var entrypoint    =
            new Semantic.Discard(
              new Semantic.Invocation(mainProcedure, List.of()));
          buildStatementDependencies(entrypoint);
          string.append("int main() {");
          indentation++;
          buildNewLine();
          buildStatement(entrypoint);
          indentation--;
          buildNewLine();
          string.append('}');
          buildNewLine();
          var code   = artifacts.resolve("%s.c".formatted(package_.name()));
          var binary = artifacts.resolve("%s.exe".formatted(package_.name()));
          Persistance.store(subject, code, string);
          var exitCode =
            Processes.execute(subject, false, "clang", "-o", binary, code);
          if (exitCode != 0) {
            throw Diagnostic
              .failure(
                subject,
                "compiler exited with %d for `%s`",
                exitCode,
                code);
          }
        }
        case Semantic.Library library -> {}
        case Semantic.Implementation implementation -> {}
      }
    }
  }

  private void buildAccess(Name name) {
    var symbol = accessSymbol(name);
    for (var externalName : symbol.externalName()) {
      string.append(externalName);
      return;
    }
    name.joined("$");
  }

  private void buildSymbol(Name name) {
    buildSymbol(accessSymbol(name));
  }

  private Symbol accessSymbol(Name name) {
    var module   = target.modules().get(name.getModule()).getFirst();
    var package_ = module.packages().get(name.getPackage()).getFirst();
    var symbol   = package_.symbols().get(name.getSymbol()).getFirst();
    return symbol;
  }

  private void buildSymbol(Semantic.Symbol symbol) {
    if (built.contains(symbol.name())) {
      return;
    }
    built.add(symbol.name());
    switch (symbol) {
      case Semantic.Proc proc -> buildProc(proc);
      case Semantic.Struct struct -> buildStruct(struct);
      case Semantic.Builtin builtin -> {}
    }
  }

  private void buildProc(Semantic.Proc proc) {
    for (var parameter : proc.parameters().values()) {
      buildTypeDependencies(parameter);
    }
    buildTypeDependencies(proc.returnType());
    for (var body : proc.body()) {
      buildStatementDependencies(body);
    }
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
    if (proc.body().isEmpty()) {
      string.append(';');
    }
    else {
      string.append(' ');
      buildStatement(proc.body().getFirst());
    }
    buildNewLine();
  }

  private void buildStruct(Semantic.Struct struct) {
    string.append("typedef struct ");
    buildAccess(struct.name());
    string.append(' ');
    buildAccess(struct.name());
    string.append(';');
    buildNewLine();
  }

  private void buildTypeDependencies(Semantic.Type type) {
    switch (type) {
      case Semantic.Symbol symbol -> buildSymbol(symbol);
      case Semantic.Pointer pointer -> buildTypeDependencies(pointer.pointee());
      case Semantic.ConstantArithmetic constant ->
        throw Diagnostic
          .failure(subject, "cannot build compile-time type `%s`", constant);
      default -> { /* Java compiler bug. */}
    }
  }

  private void buildStatementDependencies(Semantic.Statement statement) {
    switch (statement) {
      case Semantic.Block block -> {
        for (var innerStatement : block.innerStatements()) {
          buildStatementDependencies(innerStatement);
        }
      }
      case Semantic.Discard discard ->
        buildExpressionDependencies(discard.discarded());
      case Semantic.If if_ -> {
        buildExpressionDependencies(if_.condition());
        buildStatementDependencies(if_.trueBranch());
        for (var falseBranch : if_.falseBranch()) {
          buildStatementDependencies(falseBranch);
        }
      }
      case Semantic.Return return_ -> {
        for (var value : return_.value()) {
          buildExpressionDependencies(value);
        }
      }
      case Semantic.Var var -> {
        buildTypeDependencies(var.type());
        buildExpressionDependencies(var.initialValue());
      }
    }
  }

  private void buildExpressionDependencies(Semantic.Expression expression) {
    switch (expression) {
      case Semantic.UnitConstant constant -> {}
      case Semantic.Integer32Constant constant -> {}
      case Semantic.IntegralConstant constant ->
        throw Diagnostic
          .failure(
            subject,
            "cannot build compile-time constant `%s`",
            constant.value());
      case Semantic.Invocation invocation -> {
        buildSymbol(invocation.name());
        for (var argument : invocation.arguments()) {
          buildExpressionDependencies(argument);
        }
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
        if (string.charAt(string.length() - 1) != '{') {
          buildNewLine();
        }
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
      case Semantic.UnitConstant constant -> string.append('0');
      case Semantic.Integer32Constant constant ->
        string.append(constant.value());
      case Semantic.IntegralConstant constant ->
        throw Diagnostic
          .failure(
            subject,
            "cannot build compile-time constant `%s`",
            constant.value());
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
        string.append(constant.value());
      case Semantic.Natural64Constant constant ->
        string.append(constant.value());
      case Semantic.StringConstant constant ->
        Text.quote(string, constant.value());
    }
  }

  private void buildNewLine() {
    string.append(System.lineSeparator());
    for (var i = 0; i < indentation; i++) {
      string.append(' ').append(' ');
    }
  }
}
