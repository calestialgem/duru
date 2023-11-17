package duru;

public final class ConfigurationResolver {
  public static Configuration resolve(ConfigurationNode.Module node) {
    var resolver = new ConfigurationResolver(node);
    return resolver.resolve();
  }

  private final ConfigurationNode.Module node;
  private SetBuffer<String>              executables;
  private SetBuffer<String>              libraries;

  private ConfigurationResolver(ConfigurationNode.Module node) {
    this.node = node;
  }

  private Configuration resolve() {
    executables = SetBuffer.create();
    libraries   = SetBuffer.create();
    for (var declaration : node.declarations()) {
      switch (declaration) {
        case ConfigurationNode.Executable executable -> {
          var text = text(executable.name());
          checkUniqueness(text);
          executables.add(text);
        }
        case ConfigurationNode.Library library -> {
          var text = text(library.name());
          checkUniqueness(text);
          libraries.add(text);
        }
      }
    }
    return new Configuration(
      node.name().text(),
      executables.toSet(),
      libraries.toSet());
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

  private void checkUniqueness(String text) {
    if (executables.contains(text)) {
      throw Subject
        .error("package `%s` is already defined as executable", text);
    }
    if (libraries.contains(text)) {
      throw Subject.error("package `%s` is already defined as library", text);
    }
  }
}
