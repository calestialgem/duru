package duru;

import java.nio.file.Path;

public final class Checker {
  public Checker() {}

  public Semantics check(
    Explorer explorer,
    Path module_path,
    List<Path> module_bases)
  {
    throw Diagnostic.unimplemented(module_path);
  }
}
