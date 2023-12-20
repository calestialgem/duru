package duru;

import java.nio.file.Path;

public final class Checker {
  public Checker() {}

  private Explorer explorer;
  private Path module_path;
  private List<Path> module_bases;

  public Semantics check(
    Explorer explorer,
    Path module_path,
    List<Path> module_bases)
  {
    this.explorer = explorer;
    this.module_path = module_path;
    this.module_bases = module_bases;
    throw Diagnostic.unimplemented(module_path);
  }

  private void check_module(String name) {
    if (module_path.getFileName().toString().equals(name)) {
      check_module(module_path);
      return;
    }
    for (var base : module_bases) {}
  }

  private void check_module(Path module_path) {}
}
