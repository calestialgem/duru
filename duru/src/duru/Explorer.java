package duru;

import java.nio.file.Path;
import java.util.Formatter;

import duru.ConfigurationNode.PackageDeclaration;
import duru.Semantic.Target;

public final class Explorer {
  private final Path directory;
  private final boolean active;
  private boolean did_create_directory;

  public Explorer(Path artifacts, boolean active) {
    directory = artifacts.resolve("exploration");
    this.active = active;
  }

  private void store(String name, Object text) {
    if (!did_create_directory) {
      did_create_directory = true;
      Persistance.recreate("explorer", directory);
    }
    Persistance.store("explorer", directory.resolve(name), text);
  }

  public void record(Lectics lectics, Name packageName, String sourceName) {
    if (!active)
      return;
    var string = new StringBuilder();
    try (var f = new Formatter(string)) {
      f
        .format(
          "'%s's lexical representation.%n%nHash: %X%n%n",
          lectics.path,
          lectics.hashCode());
      var line = 1;
      var column = 1;
      var index = 0;
      for (var token = 0; token < lectics.token_count(); token++) {
        while (index != lectics.begin_of(token)) {
          if (lectics.content.charAt(index) == '\n') {
            line++;
            column = 1;
          }
          else {
            column++;
          }
          index++;
        }
        f
          .format(
            "%04d: %04d.%04d-%04d: %s%n",
            token,
            line,
            column,
            column + lectics.length_of(token),
            lectics.explain(token));
      }
    }
    store(
      "%s.%s-lectics.duru".formatted(packageName.joined("."), sourceName),
      string);
  }

  public void record(
    Syntactics syntactics,
    Name packageName,
    String sourceName)
  {
    if (!active)
      return;
    var string = new StringBuilder();
    try (var f = new Formatter(string)) {
      f
        .format(
          "'%s's syntactical representation.%n%nHash: %X%n%n",
          syntactics.path,
          syntactics.hashCode());
      for (var node = 0; node < syntactics.node_count(); node++) {
        var line = 1;
        var column = 1;
        var index = 0;
        while (index != syntactics.begin_of(node)) {
          if (syntactics.contents.charAt(index) == '\n') {
            line++;
            column = 1;
          }
          else {
            column++;
          }
          index++;
        }
        f
          .format(
            "%04d: %04d.%04d-%04d: %s%n",
            node,
            line,
            column,
            column + syntactics.length_of(node),
            syntactics.explain(node));
      }
    }
    store(
      "%s.%s-syntactics.duru".formatted(packageName.joined("."), sourceName),
      string);
  }

  public void record(Syntactics[] sources, int source_count, Name packageName) {
    if (!active)
      return;
    var hash = 1;
    for (var source = 0; source < source_count; source++) {
      hash *= 31;
      hash += sources[source].hashCode();
    }
    var string = new StringBuilder();
    try (var f = new Formatter(string)) {
      f.format("`%s`s declarations.%n%nHash: %X%n%n", packageName, hash);
      for (var source = 0; source < source_count; source++) {
        var filename = sources[source].path.getFileName().toString();
        filename = filename.substring(0, filename.length() - ".duru".length());
        var line = 1;
        var column = 1;
        var index = 0;
        for (var node = 0; node < sources[source].node_count(); node++) {
          String type;
          switch (sources[source].type_of(node)) {
            case Syntactics.ENTRYPOINT_DECLARATION -> type = "entrypoint";
            default -> {
              continue;
            }
          }
          while (index != sources[source].begin_of(node)) {
            if (sources[source].contents.charAt(index) == '\n') {
              line++;
              column = 1;
            }
            else {
              column++;
            }
            index++;
          }
          f
            .format(
              "%s:%04d.%04d-%04d: %s `%s`%n",
              filename,
              line,
              column,
              column + sources[source].length_of(node),
              type,
              sources[source].text_of(node));
        }
      }
    }
    store("%s-resolution.duru".formatted(packageName.joined(".")), string);
  }

  public void recordConfigurationSource(
    Source source,
    String moduleIdentifier)
  {
    if (!active)
      return;
    var string = new StringBuilder();
    string
      .append(Integer.toUnsignedString(source.hashCode(), 16).toUpperCase());
    string.append(System.lineSeparator());
    string.append(source.path());
    string.append(System.lineSeparator());
    string.append(source.contents());
    store("%s-config.source.duru".formatted(moduleIdentifier), string);
  }

  public void recordConfigurationTokens(
    List<ConfigurationToken> tokens,
    String moduleIdentifier)
  {
    if (!active)
      return;
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
    store("%s-config.tokens.duru".formatted(moduleIdentifier), string);
  }

  public void recordConfigurationDeclarations(
    List<PackageDeclaration> declarations,
    String moduleIdentifier)
  {
    if (!active)
      return;
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
    store("%s-config.declarations.duru".formatted(moduleIdentifier), string);
  }

  public void recordConfiguration(
    Configuration configuration,
    String moduleIdentifier)
  {
    if (!active)
      return;
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
    store("%s-config.resolution.duru".formatted(moduleIdentifier), string);
  }

  public void recordSource(Source source, Name packageName, String sourceName) {
    if (!active)
      return;
    var string = new StringBuilder();
    string
      .append(Integer.toUnsignedString(source.hashCode(), 16).toUpperCase());
    string.append(System.lineSeparator());
    string.append(source.path());
    string.append(System.lineSeparator());
    string.append(source.contents());
    store(
      "%s.%s-source.duru".formatted(packageName.joined("."), sourceName),
      string);
  }

  public void recordTarget(Target target) {
    if (!active)
      return;
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
    store("target.duru", string);
  }
}
