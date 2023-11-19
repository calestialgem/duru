package duru;

import java.nio.file.Path;

public sealed interface CompilerDebugger {
  record Active() implements CompilerDebugger {
    @Override
    public void record(Path artifacts, Object recorded, Object... names) {
      var string = new StringBuilder();
      for (var name : names) {
        string.append(name);
        string.append('.');
      }
      string.append("duru");
      var file = artifacts.resolve(string.toString());
      Persistance.store("compiler debugger", file, recorded);
    }
  }

  record Inactive() implements CompilerDebugger {
    @Override
    public void record(Path artifacts, Object recorded, Object... names) {}
  }

  static CompilerDebugger active() {
    return new Active();
  }

  static CompilerDebugger inactive() {
    return new Inactive();
  }

  void record(Path artifacts, Object recorded, Object... names);
}
