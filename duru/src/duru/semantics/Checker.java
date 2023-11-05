package duru.semantics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import duru.AcyclicCache;
import duru.Name;
import duru.Namespace;
import duru.configuration.Configuration;
import duru.diagnostic.Subject;
import duru.resolution.Resolver;

/** Semantically analyzes a project. */
public final class Checker {
  /** Checks a project. */
  public static Semantic.Project check(Path project) {
    Checker checker = new Checker(project);
    return checker.check();
  }

  /** Directory of the checked project. */
  private final Path project;

  /** Cache of checked packages. */
  private AcyclicCache<Namespace, Semantic.Package, PackageCheckingException> packages;

  /** Configuration of the currently checked project. */
  private Configuration configuration;

  /** Constructor. */
  private Checker(Path project) {
    this.project = project;
  }

  /** Checks the project. */
  private Semantic.Project check() {
    packages      = new AcyclicCache<>(this::checkPackage);
    configuration = Configuration.parse(project.resolve("project.duru"));

    var artifacts = project.resolve("art");
    if (!Files.exists(artifacts)) {
      try {
        Files.createDirectory(artifacts);
      }
      catch (IOException cause) {
        throw Subject
          .of(artifacts)
          .diagnose("failure", "Could not create the artifact directory!")
          .toException(cause);
      }
    }

    var configurationRecord = artifacts.resolve("configuration.duru");
    try {
      Files.writeString(configurationRecord, configuration.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(configurationRecord)
        .diagnose("failure", "Could not record the configuration!")
        .toException(cause);
    }

    for (var reference : configuration.executables()) {
      var executable = getPackage(reference.subject(), reference.namespace());
      if (executable.entrypoint().isEmpty()) {
        throw reference
          .subject()
          .diagnose("error", "Could not find the entrypoint!")
          .toException();
      }
    }

    var semantics       = new Semantic.Project(packages.getAll());
    var semanticsRecord = artifacts.resolve("semantics.duru");
    try {
      Files.writeString(semanticsRecord, semantics.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(semanticsRecord)
        .diagnose("failure", "Could not record the semantics!")
        .toException(cause);
    }
    return semantics;
  }

  /** Returns a package after checking it if it was not cached. */
  private Semantic.Package getPackage(Subject subject, Namespace namespace) {
    var cached = packages.get(subject, namespace);
    if (cached.isPresent())
      return cached.get();
    throw subject
      .diagnose("error", "Cyclic definition with `%s`!", namespace)
      .toException();
  }

  /** Checks a package and returns the result. Use
   * {@link #getPackage(Subject, Namespace)} instead of this. */
  private Semantic.Package checkPackage(Namespace namespace)
    throws PackageCheckingException
  {
    if (!namespace.root().equals(configuration.name())) {
      throw subject
        .diagnose("failure", "Cannot access packages from other projects yet!")
        .toException();
    }
    var sources = namespace.resolveSubspaces(project.resolve("src"));
    if (!Files.exists(sources) || !Files.isDirectory(sources))
      throw subject
        .diagnose(
          "error",
          "Could not find package `%s` at `%s`!",
          namespace,
          sources.toAbsolutePath().normalize())
        .toException();
    var artifacts = namespace.resolveSubspaces(project.resolve("art"));
    if (!Files.exists(artifacts))
      try {
        Files.createDirectories(artifacts);
      }
      catch (IOException cause) {
        throw Subject
          .of(artifacts)
          .diagnose("failure", "Could not create the artifact directory!")
          .toException(cause);
      }
    var resolution     = Resolver.resolve(sources, artifacts);
    var entrypoint     = Optional.<Semantic.Entrypoint>empty();
    var globals        = new HashMap<String, Semantic.Definition>();
    var checkedGlobals = new HashSet<String>();
    var package_       = new Semantic.Package(entrypoint, globals);
    return package_;
  }

  /** Find a global symbol from the already checked packages. */
  private Semantic.Definition findGlobal(Subject subject, Name name) {
    Semantic.Package package_ = getPackage(subject, name.namespace());
    if (!package_.globals().containsKey(name.identifier())) {
      throw subject
        .diagnose("error", "Could not find the symbol `%s`!", name)
        .toException();
    }
    Semantic.Definition global = package_.globals().get(name.identifier());
    if (!global.visible()) {
      throw subject
        .diagnose(
          "error",
          "Requested symbol `%s` is not visible!",
          global.name())
        .toException();
    }
    if (global instanceof Semantic.Using using) {
      return using.aliased();
    }
    return global;
  }
}
