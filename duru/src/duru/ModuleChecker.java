package duru;

import java.nio.file.Path;

public final class ModuleChecker {
  public static Semantic.Module check(
    CompilerDebugger debugger,
    Object subject,
    SetBuffer<String> externalNames,
    Accessor<String, Semantic.Module> accessor,
    Path directory)
  {
    var checker =
      new ModuleChecker(debugger, subject, externalNames, accessor, directory);
    return checker.check();
  }

  private final CompilerDebugger debugger;
  private final Object subject;
  private final SetBuffer<String> externalNames;
  private final Accessor<String, Semantic.Module> accessor;
  private final Path directory;
  private String moduleIdentifier;
  private Path sources;
  private Path artifacts;
  private Configuration configuration;
  private AcyclicCache<Name, Semantic.Package> packages;

  private ModuleChecker(
    CompilerDebugger debugger,
    Object subject,
    SetBuffer<String> externalNames,
    Accessor<String, Semantic.Module> accessor,
    Path directory)
  {
    this.debugger = debugger;
    this.subject = subject;
    this.externalNames = externalNames;
    this.accessor = accessor;
    this.directory = directory;
  }

  private Semantic.Module check() {
    moduleIdentifier = directory.getFileName().toString();
    sources = directory.resolve("src");
    artifacts = directory.resolve("art");
    Persistance.ensure(directory, artifacts);
    resolveConfiguration();
    packages = AcyclicCache.create(this::checkPackage);
    checkPackageDeclarations();
    return new Semantic.Module(moduleIdentifier, packages.getAll());
  }

  private void checkPackageDeclarations() {
    for (var executable : configuration.executables()) {
      var main =
        packages
          .get(executable.value(), executable.key())
          .symbols()
          .get("main");
      if (main.isEmpty() || !(main.getFirst() instanceof Semantic.Fn fn)) {
        throw Diagnostic
          .error(
            executable.value(),
            "executable package `%s` does not have a main function",
            executable.key());
      }
      if (!fn.parameters().isEmpty()) {
        throw Diagnostic
          .error(
            executable.value(),
            "executable package `%s` has a main function with parameters",
            executable.key());
      }
      if (!(fn.returnType() instanceof Semantic.Void)) {
        throw Diagnostic
          .error(
            executable.value(),
            "executable package `%s` has a main function with a non-void return type",
            executable.key());
      }
    }
    for (var library : configuration.libraries()) {
      var main =
        packages.get(library.value(), library.key()).symbols().get("main");
      if (!main.isEmpty() && main.getFirst() instanceof Semantic.Fn) {
        throw Diagnostic
          .error(
            library.value(),
            "library package `%s` has a main function",
            library.key());
      }
    }
  }

  private void resolveConfiguration() {
    var configurationFile = directory.resolve("module.duru");
    var configurationSource =
      new Source(
        configurationFile,
        Persistance.load(subject, configurationFile));
    debugger.recordConfigurationSource(configurationSource, moduleIdentifier);
    var configurationTokens = ConfigurationLexer.lex(configurationSource);
    debugger.recordConfigurationTokens(configurationTokens, moduleIdentifier);
    var configurationNode = ConfigurationParser.parse(configurationTokens);
    debugger
      .recordConfigurationDeclarations(configurationNode, moduleIdentifier);
    configuration = ConfigurationResolver.resolve(configurationNode);
    debugger.recordConfiguration(configuration, moduleIdentifier);
  }

  private Semantic.Package checkPackage(Object subject, Name packageName) {
    PackageType type;
    if (configuration.executables().contains(packageName)) {
      type = PackageType.EXECUTABLE;
    }
    else if (configuration.libraries().contains(packageName)) {
      type = PackageType.LIBRARY;
    }
    else {
      type = PackageType.IMPLEMENTATION;
    }
    return PackageChecker
      .check(
        debugger,
        subject,
        externalNames,
        this::accessPackage,
        sources,
        type,
        packageName);
  }

  private Semantic.Package accessPackage(
    Object subject,
    Name mentionedPackage)
  {
    var mentionedModule = mentionedPackage.getModule();
    if (mentionedModule.equals(moduleIdentifier)) {
      if (!(packages
        .get(subject, mentionedPackage) instanceof Semantic.Library library))
      {
        throw Diagnostic
          .error(
            subject,
            "accessed package `%s` is not a library",
            mentionedPackage);
      }
      return library;
    }
    var accessedPackage =
      accessor
        .access(subject, mentionedModule)
        .packages()
        .get(mentionedPackage);
    if (accessedPackage.isEmpty()) {
      throw Diagnostic
        .error(subject, "there is no package `%s`", mentionedPackage);
    }
    if (!(accessedPackage.getFirst() instanceof Semantic.Library library)) {
      throw Diagnostic
        .error(
          subject,
          "accessed package `%s` is not a library",
          mentionedPackage);
    }
    return library;
  }
}
