package duru;

import java.nio.file.Path;

public final class Configuration {
  public static Configuration create() { return new Configuration(); }

  private Path workspace;

  private Configuration() {}

  public Path get_workspace() { return workspace; }

  public void set_workspace(Path workspace) { this.workspace = workspace; }

  public Path get_artifacts() { return get_workspace().resolve("art"); }

  public Path get_sources() { return get_workspace().resolve("src"); }

  public Path get_workspace_configuration() {
    return get_workspace().resolve("config.duru");
  }
}
