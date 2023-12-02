package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class PackageChecker {
  private final Lexer lexer;
  private final Parser parser;
  private final AcyclicCache<String, Semantic.Symbol> symbols;
  private final Node_Iterator primary_iterator;
  private final Node_Iterator secondary_iterator;
  private Syntactics[] sources;
  private int source_count;
  private SetBuffer<String> externalNames;
  private Accessor<Name, Semantic.Package> accessor;
  private Name package_name;

  public PackageChecker() {
    lexer = new Lexer();
    parser = new Parser();
    symbols = AcyclicCache.create(this::checkSymbol);
    primary_iterator = new Node_Iterator();
    secondary_iterator = new Node_Iterator();
    sources = new Syntactics[0];
  }

  public Semantic.Package check(
    Explorer explorer,
    Object subject,
    SetBuffer<String> externalNames,
    Accessor<Name, Semantic.Package> accessor,
    Path root_source_directory,
    Name package_name,
    PackageType package_type)
  {
    symbols.clear();
    source_count = 0;
    this.externalNames = externalNames;
    this.accessor = accessor;
    this.package_name = package_name;
    var directory = package_name.resolve(root_source_directory);
    for (var file : Persistance.list(subject, directory)) {
      var fullFilename = file.getFileName().toString();
      if (!fullFilename.endsWith(".duru")) {
        continue;
      }
      var filename =
        fullFilename.substring(0, fullFilename.length() - ".duru".length());
      var source = new Source(file, Persistance.load(directory, file));
      explorer.recordSource(source, package_name, filename);
      var lectics = lexer.lex(source.path(), source.contents());
      explorer.record(lectics, package_name, filename);
      var syntactics = parser.parse(lectics);
      explorer.record(syntactics, package_name, filename);
      for (
        primary_iterator.iterate(syntactics);
        primary_iterator.has(); primary_iterator.advance()) {
        if (!primary_iterator.kind().is_declaration())
          continue;
        var identifier = primary_iterator.text();
        for (
secondary_iterator.iterate_remaining(primary_iterator);
          secondary_iterator.has();
          secondary_iterator.advance())
        {
          if (!secondary_iterator.kind().is_declaration())
            continue;
          var other_identifier = secondary_iterator.text();
          if (identifier.equals(other_identifier)) {
            throw Diagnostic
              .error(
                secondary_iterator.subject(),
                "redeclaration of `%s`",
                package_name.scope(other_identifier));
          }
        }
        for (
          var previous_source = 0;
          previous_source < source_count;
          previous_source++)
        {
          for (
secondary_iterator.iterate(sources[previous_source]);
           secondary_iterator.has(); secondary_iterator.advance())
          {
            if (!secondary_iterator.kind().is_declaration())
              continue;
            var other_identifier = secondary_iterator.text();
            if (identifier.equals(other_identifier)) {
              throw Diagnostic
                .error(
                  primary_iterator.subject(),
                  "redeclaration of `%s`",
                  package_name.scope(identifier));
            }
          }
        }
      }
      if (source_count == sources.length) {
        var capacity = source_count * 2;
        if (capacity == 0)
          capacity = 1;
        sources = Arrays.copyOf(sources, capacity, Syntactics[].class);
      }
      sources[source_count] = syntactics;
      source_count++;
    }
    explorer.record(sources, source_count, package_name);
    if (package_name.equals(Name.of("duru"))) {
      for (var builtin : Semantic.BUILTINS) {
        symbols.add(builtin.identifier(), builtin);
      }
    }
    for (var source = 0; source < source_count; source++) {
      for (primary_iterator.iterate(sources[source]); primary_iterator.has();
    primary_iterator.advance()) {
        if (!primary_iterator.kind().is_declaration())
          continue;
        symbols
          .get(primary_iterator.subject(), primary_iterator.text());
      }
    }
    return switch (package_type) {
      case EXECUTABLE ->
        new Semantic.Executable(package_name, symbols.getAll());
      case LIBRARY -> new Semantic.Library(package_name, symbols.getAll());
      case IMPLEMENTATION ->
        new Semantic.Implementation(package_name, symbols.getAll());
    };
  }

  private Semantic.Symbol checkSymbol(Object subject, String identifier) {
    for (var source = 0; source < source_count; source++) {
      for (primary_iterator.iterate(sources[source]); primary_iterator.has();
    primary_iterator.advance()) {
        if (!primary_iterator.kind().is_declaration())
          continue;
        if (identifier.equals(primary_iterator.text())) {
          // TODO: fix checking after rewriting SymbolChecker
          throw Diagnostic.unimplemented(subject);
        }
      }
    }
    throw Diagnostic
      .error(
        subject,
        "there is no symbol `%s`",
        package_name.scope(identifier));
  }

  private Semantic.Symbol accessSymbol(Object subject, Name mention) {
    var mentionedSymbol = mention.getSymbol();
    if (!mention.isScoped()) {
      return symbols.get(subject, mentionedSymbol);
    }
    var mentionedPackage = mention.getPackage();
    if (mentionedPackage.equals(package_name)) {
      return symbols.get(subject, mentionedSymbol);
    }
    var accessedSymbol =
      accessor.access(subject, mentionedPackage).symbols().get(mentionedSymbol);
    if (accessedSymbol.isEmpty()) {
      throw Diagnostic.error(subject, "there is no symbol `%s`", mention);
    }
    if (!accessedSymbol.getFirst().isPublic()) {
      throw Diagnostic
        .error(subject, "accessed symbol `%s` is not public", mention);
    }
    return accessedSymbol.getFirst();
  }
}
