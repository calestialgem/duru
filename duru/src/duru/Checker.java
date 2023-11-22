package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Checker {
  public static Semantic.Target check(
    CompilerDebugger debugger,
    Object subject,
    Path directory,
    List<Path> moduleBases)
  {
    var checker = new Checker(debugger, subject, directory, moduleBases);
    return checker.check();
  }

  private final CompilerDebugger                debugger;
  private final Object                          subject;
  private final Path                            directory;
  private final List<Path>                      moduleBases;
  private String                                main;
  private AcyclicCache<String, Semantic.Module> modules;
  private SetBuffer<String>                     externalNames;

  private Checker(
    CompilerDebugger debugger,
    Object subject,
    Path directory,
    List<Path> moduleBases)
  {
    this.debugger    = debugger;
    this.subject     = subject;
    this.directory   = directory;
    this.moduleBases = moduleBases;
  }

  private Semantic.Target check() {
    main          = directory.getFileName().toString();
    modules       = AcyclicCache.create(this::checkModule);
    externalNames = SetBuffer.create();
    modules.get(subject, main);
    var target = new Semantic.Target(main, modules.getAll());
    debugger.recordTarget(target);
    return target;
  }

  private Semantic.Module checkModule(Object subject, String name) {
    if (name.equals(main)) {
      return checkModule(subject, directory);
    }
    for (var moduleBase : moduleBases) {
      if (moduleBase.getFileName().toString().equals(name)
        && Files.exists(moduleBase.resolve("module.duru")))
      {
        return checkModule(subject, moduleBase);
      }
      var checkedDirectory = moduleBase.resolve(name);
      if (Files.exists(checkedDirectory.resolve("module.duru"))) {
        return checkModule(subject, checkedDirectory);
      }
    }
    throw Diagnostic
      .error(subject, "no module `%s` in bases `%s`", name, moduleBases);
  }

  private Semantic.Module checkModule(Object subject, Path directory) {
    return ModuleChecker
      .check(debugger, subject, externalNames, this::accessModule, directory);
  }

  private Semantic.Module accessModule(Object subject, String name) {
    return modules.get(subject, name);
  }
}
