package duru;

public final class ConfigurationResolver {
  public static Configuration resolve(
    List<ConfigurationNode.PackageDeclaration> declarations)
  {
    var resolver = new ConfigurationResolver(declarations);
    return resolver.resolve();
  }

  private final List<ConfigurationNode.PackageDeclaration> declarations;
  private MapBuffer<Name, Location>                        executables;
  private MapBuffer<Name, Location>                        libraries;

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
          var name = executable.name().toName();
          checkUniqueness(name, executable.name().location());
          executables.add(name, executable.name().location());
        }
        case ConfigurationNode.Library library -> {
          var name = library.name().toName();
          checkUniqueness(name, library.name().location());
          libraries.add(name, library.name().location());
        }
      }
    }
    return new Configuration(executables.toMap(), libraries.toMap());
  }

  private void checkUniqueness(Name name, Location location) {
    if (executables.contains(name)) {
      throw Diagnostic
        .error(location, "redefinition of executable package `%s`", name);
    }
    if (libraries.contains(name)) {
      throw Diagnostic
        .error(location, "redefinition of library package `%s`", name);
    }
  }
}
