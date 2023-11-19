package duru;

public final class ConfigurationResolver {
  public static Configuration resolve(
    List<ConfigurationNode.PackageDeclaration> declarations)
  {
    var resolver = new ConfigurationResolver(declarations);
    return resolver.resolve();
  }

  private final List<ConfigurationNode.PackageDeclaration> declarations;
  private MapBuffer<String, Location>                      executables;
  private MapBuffer<String, Location>                      libraries;

  private ConfigurationResolver(
    List<ConfigurationNode.PackageDeclaration> declarations)
  {
    this.declarations = declarations;
  }

  private Configuration resolve() {
    executables = MapBuffer.create();
    libraries   = MapBuffer.create();
    for (var declaration : declarations) {
      switch (declaration) {
        case ConfigurationNode.Executable executable -> {
          var text = text(executable.name());
          checkUniqueness(text, executable.name().location());
          executables.add(text, executable.name().location());
        }
        case ConfigurationNode.Library library -> {
          var text = text(library.name());
          checkUniqueness(text, library.name().location());
          libraries.add(text, library.name().location());
        }
      }
    }
    return new Configuration(executables.toMap(), libraries.toMap());
  }

  private String text(ConfigurationNode.PackageName name) {
    var string = new StringBuilder();
    string.append(name.subspaces().getFirst().text());
    for (var i = 1; i < name.subspaces().length(); i++) {
      string.append('.');
      string.append(name.subspaces().get(i).text());
    }
    return string.toString();
  }

  private void checkUniqueness(String text, Location location) {
    if (executables.contains(text))
      throw Diagnostic
        .error(location, "redefinition of executable package `%s`", text);
    if (libraries.contains(text))
      throw Diagnostic
        .error(location, "redefinition of library package `%s`", text);
  }
}
