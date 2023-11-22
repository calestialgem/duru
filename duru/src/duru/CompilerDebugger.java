package duru;

import java.nio.file.Path;

import duru.ConfigurationNode.PackageDeclaration;
import duru.Node.Declaration;
import duru.Semantic.Target;

public sealed interface CompilerDebugger {
  record Active(Path directory) implements CompilerDebugger {
    @Override
    public void recordConfigurationSource(
      Source source,
      String moduleIdentifier)
    {
      var string = new StringBuilder();
      string
        .append(Integer.toUnsignedString(source.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      string.append(source.path());
      string.append(System.lineSeparator());
      string.append(source.contents());
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve("%s-config.source.duru".formatted(moduleIdentifier)),
          string);
    }

    @Override
    public void recordConfigurationTokens(
      List<ConfigurationToken> tokens,
      String moduleIdentifier)
    {
      var string = new StringBuilder();
      string
        .append(Integer.toUnsignedString(tokens.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var token : tokens) {
        string
          .append(
            "%04d.%04d-%04d.%04d"
              .formatted(
                token.location().beginLine(),
                token.location().beginColumn(),
                token.location().endLine(),
                token.location().endColumn()));
        string.append(':');
        string.append(' ');
        string.append(token);
        string.append(System.lineSeparator());
      }
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve("%s-config.tokens.duru".formatted(moduleIdentifier)),
          string);
    }

    @Override
    public void recordConfigurationDeclarations(
      List<PackageDeclaration> declarations,
      String moduleIdentifier)
    {
      var string = new StringBuilder();
      string
        .append(
          Integer.toUnsignedString(declarations.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var declaration : declarations) {
        string
          .append(
            "%04d.%04d-%04d.%04d"
              .formatted(
                declaration.name().location().beginLine(),
                declaration.name().location().beginColumn(),
                declaration.name().location().endLine(),
                declaration.name().location().endColumn()));
        string.append(':');
        string.append(' ');
        string.append(declaration.getClass().getSimpleName());
        string.append(' ');
        string.append('`');
        string.append(declaration.name());
        string.append('`');
        string.append(System.lineSeparator());
      }
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve("%s-config.declarations.duru".formatted(moduleIdentifier)),
          string);
    }

    @Override
    public void recordConfiguration(
      Configuration configuration,
      String moduleIdentifier)
    {
      var string = new StringBuilder();
      string
        .append(
          Integer.toUnsignedString(configuration.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var executable : configuration.executables()) {
        string
          .append(
            "%04d.%04d-%04d.%04d"
              .formatted(
                executable.value().beginLine(),
                executable.value().beginColumn(),
                executable.value().endLine(),
                executable.value().endColumn()));
        string.append(':');
        string.append(' ');
        string.append("executable");
        string.append(' ');
        string.append('`');
        string.append(executable.key());
        string.append('`');
        string.append(System.lineSeparator());
      }
      for (var library : configuration.libraries()) {
        string
          .append(
            "%04d.%04d-%04d.%04d"
              .formatted(
                library.value().beginLine(),
                library.value().beginColumn(),
                library.value().endLine(),
                library.value().endColumn()));
        string.append(':');
        string.append(' ');
        string.append("library");
        string.append(' ');
        string.append('`');
        string.append(library.key());
        string.append('`');
        string.append(System.lineSeparator());
      }
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve("%s-config.resolution.duru".formatted(moduleIdentifier)),
          string);
    }

    @Override
    public void recordSource(
      Source source,
      Name packageName,
      String sourceName)
    {
      var string = new StringBuilder();
      string
        .append(Integer.toUnsignedString(source.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      string.append(source.path());
      string.append(System.lineSeparator());
      string.append(source.contents());
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve(
              "%s.%s-source.duru"
                .formatted(packageName.joined("."), sourceName)),
          string);
    }

    @Override
    public void recordTokens(
      List<Token> tokens,
      Name packageName,
      String sourceName)
    {
      var string = new StringBuilder();
      string
        .append(Integer.toUnsignedString(tokens.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var token : tokens) {
        string
          .append(
            "%04d.%04d-%04d.%04d"
              .formatted(
                token.location().beginLine(),
                token.location().beginColumn(),
                token.location().endLine(),
                token.location().endColumn()));
        string.append(':');
        string.append(' ');
        string.append(token);
        string.append(System.lineSeparator());
      }
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve(
              "%s.%s-tokens.duru"
                .formatted(packageName.joined("."), sourceName)),
          string);
    }

    @Override
    public void recordDeclarations(
      List<Declaration> declarations,
      Name packageName,
      String sourceName)
    {
      var string = new StringBuilder();
      string
        .append(
          Integer.toUnsignedString(declarations.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var declaration : declarations) {
        string
          .append(
            "%04d.%04d-%04d.%04d"
              .formatted(
                declaration.name().location().beginLine(),
                declaration.name().location().beginColumn(),
                declaration.name().location().endLine(),
                declaration.name().location().endColumn()));
        string.append(':');
        string.append(' ');
        for (var externalName : declaration.externalName()) {
          string.append("extern");
          string.append(' ');
          Text.quote(string, externalName.value());
          string.append(' ');
        }
        if (declaration.isPublic()) {
          string.append("public");
          string.append(' ');
        }
        string.append(declaration.getClass().getSimpleName());
        string.append(' ');
        string.append('`');
        string.append(declaration.name().text());
        string.append('`');
        string.append(System.lineSeparator());
      }
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve(
              "%s.%s-declarations.duru"
                .formatted(packageName.joined("."), sourceName)),
          string);
    }

    @Override
    public void recordResolution(
      Map<String, Declaration> resolution,
      Name packageName)
    {
      var string = new StringBuilder();
      string
        .append(
          Integer.toUnsignedString(resolution.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var declaration : resolution.values()) {
        var filename =
          declaration.location().source().path().getFileName().toString();
        string
          .append(filename.substring(0, filename.length() - ".duru".length()));
        string
          .append(
            ":%04d.%04d-%04d.%04d"
              .formatted(
                declaration.name().location().beginLine(),
                declaration.name().location().beginColumn(),
                declaration.name().location().endLine(),
                declaration.name().location().endColumn()));
        string.append(':');
        string.append(' ');
        for (var externalName : declaration.externalName()) {
          string.append("extern");
          string.append(' ');
          Text.quote(string, externalName.value());
          string.append(' ');
        }
        if (declaration.isPublic()) {
          string.append("public");
          string.append(' ');
        }
        string.append(declaration.getClass().getSimpleName());
        string.append(' ');
        string.append('`');
        string.append(declaration.name().text());
        string.append('`');
        string.append(System.lineSeparator());
      }
      Persistance
        .store(
          "compiler-debugger",
          directory
            .resolve("%s.resolution.duru".formatted(packageName.joined("."))),
          string);
    }

    @Override
    public void recordTarget(Target target) {
      var string = new StringBuilder();
      string
        .append(Integer.toUnsignedString(target.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      for (var module : target.modules().values()) {
        for (var package_ : module.packages().values()) {
          for (var declaration : package_.symbols().values()) {
            for (var externalName : declaration.externalName()) {
              string.append("extern");
              string.append(' ');
              Text.quote(string, externalName);
              string.append(' ');
            }
            if (declaration.isPublic()) {
              string.append("public");
              string.append(' ');
            }
            string.append(declaration.getClass().getSimpleName());
            string.append(' ');
            string.append('`');
            string.append(declaration.name());
            string.append('`');
            string.append(System.lineSeparator());
          }
        }
      }
      Persistance
        .store("compiler-debugger", directory.resolve("target.duru"), string);
    }
  }

  record Inactive() implements CompilerDebugger {
    @Override
    public void recordConfigurationSource(
      Source source,
      String moduleIdentifier)
    {}

    @Override
    public void recordConfigurationTokens(
      List<ConfigurationToken> tokens,
      String moduleIdentifier)
    {}

    @Override
    public void recordConfigurationDeclarations(
      List<PackageDeclaration> declarations,
      String moduleIdentifier)
    {}

    @Override
    public void recordConfiguration(
      Configuration configuration,
      String moduleIdentifier)
    {}

    @Override
    public void recordSource(
      Source source,
      Name packageName,
      String sourceName)
    {}

    @Override
    public void recordTokens(
      List<Token> tokens,
      Name packageName,
      String sourceName)
    {}

    @Override
    public void recordDeclarations(
      List<Declaration> declarations,
      Name packageName,
      String sourceName)
    {}

    @Override
    public void recordResolution(
      Map<String, Declaration> resolution,
      Name packageName)
    {}

    @Override
    public void recordTarget(Target target) {}
  }

  static CompilerDebugger active(Path artifacts) {
    var directory = artifacts.resolve("compiler-debugger");
    Persistance.recreate("compiler-debugger", directory);
    return new Active(directory);
  }

  static CompilerDebugger inactive() {
    return new Inactive();
  }

  void recordConfigurationSource(Source source, String moduleIdentifier);
  void recordConfigurationTokens(
    List<ConfigurationToken> tokens,
    String moduleIdentifier);
  void recordConfigurationDeclarations(
    List<ConfigurationNode.PackageDeclaration> declarations,
    String moduleIdentifier);
  void recordConfiguration(
    Configuration configuration,
    String moduleIdentifier);
  void recordSource(Source source, Name packageName, String sourceName);
  void recordTokens(List<Token> tokens, Name packageName, String sourceName);
  void recordDeclarations(
    List<Node.Declaration> declarations,
    Name packageName,
    String sourceName);
  void recordResolution(
    Map<String, Node.Declaration> resolution,
    Name packageName);
  void recordTarget(Semantic.Target target);
}
