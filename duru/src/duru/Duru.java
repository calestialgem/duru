package duru;

import duru.diagnostic.Diagnostic;

public final class Duru {
  public static boolean init(
    Diagnostic diagnostic,
    Configuration configuration)
  {
    var duru = new Duru(diagnostic, configuration);
    return duru.init();
  }

  private final Diagnostic diagnostic;

  private final Configuration configuration;

  private Duru(Diagnostic diagnostic, Configuration configuration) {
    this.diagnostic = diagnostic;
    this.configuration = configuration;
  }

  private boolean init() {
    diagnostic.begin();
    if (Persistance.create(diagnostic, configuration.get_workspace())) {
      diagnostic.failure("Could not create the workspace directory!");
      return true;
    }
    if (Persistance.create(diagnostic, configuration.get_artifacts())) {
      diagnostic.failure("Could not create the artifact directory!");
      return true;
    }
    if (Persistance.create(diagnostic, configuration.get_sources())) {
      diagnostic.failure("Could not create the source directory!");
      return true;
    }
    if (Persistance
      .write(diagnostic, configuration.get_workspace_configuration(), ""))
    {
      diagnostic.failure("Could not write the workspace configuration file!");
      return true;
    }
    diagnostic.skip();
    return false;
  }
}
