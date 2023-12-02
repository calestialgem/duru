package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Checker {
  public static Semantic.Target check(
    Explorer explorer,
    Object subject,
    Path directory,
    List<Path> moduleBases)
  {
    var checker = new Checker(explorer, subject, directory, moduleBases);
    return checker.check();
  }

  private final Explorer explorer;
  private final Object subject;
  private final Path directory;
  private final List<Path> moduleBases;
  private String main;
  private AcyclicCache<String, Semantic.Module> modules;
  private SetBuffer<String> externalNames;

  private Checker(
    Explorer explorer,
    Object subject,
    Path directory,
    List<Path> moduleBases)
  {
    this.explorer = explorer;
    this.subject = subject;
    this.directory = directory;
    this.moduleBases = moduleBases;
  }

  private Semantic.Target check() {
    main = directory.getFileName().toString();
    modules = AcyclicCache.create(this::checkModule);
    externalNames = SetBuffer.create();
    modules.get(subject, main);
    var target = new Semantic.Target(main, modules.getAll());
    explorer.recordTarget(target);
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
    if (name.equals("duru")) {
      var symbols = MapBuffer.<String, Semantic.Symbol>create();
      for (var builtin : Semantic.BUILTINS) {
        symbols.add(builtin.identifier(), builtin);
      }
      var package_ = new Semantic.Library(Name.of(name), symbols.toMap());
      var packages = MapBuffer.<Name, Semantic.Package>create();
      packages.add(package_.name(), package_);
      return new Semantic.Module(name, packages.toMap());
    }
    throw Diagnostic
      .error(subject, "no module `%s` in bases `%s`", name, moduleBases);
  }

  private Semantic.Module checkModule(Object subject, Path directory) {
    return ModuleChecker
      .check(explorer, subject, externalNames, this::accessModule, directory);
  }

  private Semantic.Module accessModule(Object subject, String name) {
    return modules.get(subject, name);
  }
}
