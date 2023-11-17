package duru;

public final class ConfigurationResolver {
  public static Configuration resolve(ConfigurationNode.Module node) {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private ConfigurationResolver() {}
}
