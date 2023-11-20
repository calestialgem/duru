package duru;

import java.nio.file.Path;

import duru.ConfigurationNode.PackageDeclaration;
import duru.Node.Declaration;
import duru.Semantic.Target;

public sealed interface CompilerDebugger {
  record Active() implements CompilerDebugger {
    @Override
    public void recordConfigurationSource(Path artifacts, Source source) {
      var string = new StringBuilder();
      string
        .append(Integer.toUnsignedString(source.hashCode(), 16).toUpperCase());
      string.append(System.lineSeparator());
      string.append(source.path());
      string.append(System.lineSeparator());
      string.append(source.contents());
      Persistance
        .store(
          "compiler debugger",
          artifacts.resolve("module.source.duru"),
          string);
    }

    @Override
    public void recordConfigurationTokens(
      Path artifacts,
      List<ConfigurationToken> tokens)
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
          "compiler debugger",
          artifacts.resolve("module.tokens.duru"),
          string);
    }

    @Override
    public void recordConfigurationDeclarations(
      Path artifacts,
      List<PackageDeclaration> declarations)
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
          "compiler debugger",
          artifacts.resolve("module.declarations.duru"),
          string);
    }

    @Override
    public void recordConfiguration(
      Path artifacts,
      Configuration configuration)
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
          "compiler debugger",
          artifacts.resolve("module.resolution.duru"),
          string);
    }

    @Override
    public void recordSource(
      Path artifacts,
      Source source,
      String packageName,
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
          "compiler debugger",
          artifacts
            .resolve("%s.%s.source.duru".formatted(packageName, sourceName)),
          string);
    }

    @Override
    public void recordTokens(
      Path artifacts,
      List<Token> tokens,
      String packageName,
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
          "compiler debugger",
          artifacts
            .resolve("%s.%s.tokens.duru".formatted(packageName, sourceName)),
          string);
    }

    @Override
    public void recordDeclarations(
      Path artifacts,
      List<Declaration> declarations,
      String packageName,
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
          "compiler debugger",
          artifacts
            .resolve(
              "%s.%s.declarations.duru".formatted(packageName, sourceName)),
          string);
    }

    @Override
    public void recordResolution(
      Path artifacts,
      Map<String, Declaration> resolution,
      String packageName)
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
          "compiler debugger",
          artifacts.resolve("%s.resolution.duru".formatted(packageName)),
          string);
    }

    @Override
    public void recordTarget(Path artifacts, Target target) {
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
        .store("compiler debugger", artifacts.resolve("target.duru"), string);
    }
  }

  record Inactive() implements CompilerDebugger {
    @Override
    public void recordConfigurationSource(Path artifacts, Source source) {}

    @Override
    public void recordConfigurationTokens(
      Path artifacts,
      List<ConfigurationToken> tokens)
    {}

    @Override
    public void recordConfigurationDeclarations(
      Path artifacts,
      List<PackageDeclaration> declarations)
    {}

    @Override
    public void recordConfiguration(
      Path artifacts,
      Configuration configuration)
    {}

    @Override
    public void recordSource(
      Path artifacts,
      Source source,
      String packageName,
      String sourceName)
    {}

    @Override
    public void recordTokens(
      Path artifacts,
      List<Token> tokens,
      String packageName,
      String sourceName)
    {}

    @Override
    public void recordDeclarations(
      Path artifacts,
      List<Declaration> declarations,
      String packageName,
      String sourceName)
    {}

    @Override
    public void recordResolution(
      Path artifacts,
      Map<String, Declaration> resolution,
      String packageName)
    {}

    @Override
    public void recordTarget(Path artifacts, Target target) {}
  }

  static CompilerDebugger active() {
    return new Active();
  }

  static CompilerDebugger inactive() {
    return new Inactive();
  }

  void recordConfigurationSource(Path artifacts, Source source);
  void recordConfigurationTokens(
    Path artifacts,
    List<ConfigurationToken> tokens);
  void recordConfigurationDeclarations(
    Path artifacts,
    List<ConfigurationNode.PackageDeclaration> declarations);
  void recordConfiguration(Path artifacts, Configuration configuration);
  void recordSource(
    Path artifacts,
    Source source,
    String packageName,
    String sourceName);
  void recordTokens(
    Path artifacts,
    List<Token> tokens,
    String packageName,
    String sourceName);
  void recordDeclarations(
    Path artifacts,
    List<Node.Declaration> declarations,
    String packageName,
    String sourceName);
  void recordResolution(
    Path artifacts,
    Map<String, Node.Declaration> resolution,
    String packageName);
  void recordTarget(Path artifacts, Semantic.Target target);
}
