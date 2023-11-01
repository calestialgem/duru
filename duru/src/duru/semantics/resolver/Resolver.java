package duru.semantics.resolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import duru.diagnostic.Subject;
import duru.lectics.LexedSource;
import duru.lectics.Lexer;
import duru.source.Loader;
import duru.source.Source;
import duru.syntactics.Node;
import duru.syntactics.ParsedSource;
import duru.syntactics.Parser;

/** First pass of the semantic analysis. Records down all the declarations in a
 * package. */
public final class Resolver {
  /** Resolves a package. */
  public static ResolvedPackage resolve(Path sources, Path artifacts) {
    Resolver resolver = new Resolver(sources, artifacts);
    return resolver.resolve();
  }

  /** Path to the resolved package's sources. */
  private final Path sources;

  /** Path to the directory where debug artifacts can be recorded to. */
  private final Path artifacts;

  /** Entrypoint in the package, if there is any. */
  private Optional<Node.Entrypoint> entrypoint;

  /** Package's symbol table. */
  private Map<String, Node.Definition> globals;

  /** Source files the declarations came from. */
  private Map<Node.Declaration, ParsedSource> origins;

  /** Constructor. */
  private Resolver(Path sources, Path artifacts) {
    this.sources   = sources;
    this.artifacts = artifacts;
  }

  /** Resolves the package. */
  private ResolvedPackage resolve() {
    entrypoint = Optional.empty();
    globals    = new HashMap<>();
    origins    = new HashMap<>();
    try {
      Files
        .list(sources)
        .filter(p -> p.getFileName().toString().endsWith(Source.EXTENSION))
        .forEach(this::resolveFile);
    }
    catch (IOException cause) {
      throw Subject
        .of(sources)
        .diagnose("failure", "Could not list the directory's entries!")
        .toException(cause);
    }
    return new ResolvedPackage(entrypoint, globals, origins);
  }

  /** Resolves a source file. */
  private void resolveFile(Path file) {
    Source source = Loader.load(file);
    recordRepresentation(source.name(), "contents", source.contents());
    LexedSource lectics = Lexer.lex(source);
    recordRepresentation(source.name(), "tokens", lectics.tokens());
    ParsedSource syntactics = Parser.parse(lectics);
    recordRepresentation(
      source.name(),
      "declarations",
      syntactics.declarations());
    for (Node.Declaration node : syntactics.declarations()) {
      switch (node) {
        case Node.Entrypoint e -> {
          if (entrypoint.isPresent()) {
            throw syntactics
              .subject(node)
              .diagnose("error", "Redeclaration of entrypoint!")
              .toException();
          }
          entrypoint = Optional.of(e);
        }
        case Node.Definition g -> {
          String identifier = g.identifier().text();
          if (globals.containsKey(identifier)) {
            throw syntactics
              .subject(g)
              .diagnose("error", "Redeclaration of `%s`!", identifier)
              .toException();
          }
          globals.put(identifier, g);
        }
      }
      origins.put(node, syntactics);
    }
  }

  /** Records a representation of the source file. Used for debugging the
   * compiler. */
  private void recordRepresentation(
    String sourceName,
    String representationName,
    Object representation)
  {
    Path representationPath =
      artifacts
        .resolve(
          "%s.%s%s"
            .formatted(sourceName, representationName, Source.EXTENSION));
    try {
      Files.writeString(representationPath, representation.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(representationPath)
        .diagnose(
          "failure",
          "Could not record the %s of source `%s`",
          representationName,
          sourceName)
        .toException(cause);
    }
  }
}
