package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class PackageChecker {
  private final Lexer lexer;
  private final Parser parser;
  private final AcyclicCache<String, Semantic.Symbol> symbols;
  private Syntactics[] sources;
  private int source_count;
  private SetBuffer<String> externalNames;
  private Accessor<Name, Semantic.Package> accessor;
  private Name package_name;

  public PackageChecker() {
    lexer = Lexer.create();
    parser = new Parser();
    symbols = AcyclicCache.create(this::checkSymbol);
    sources = new Syntactics[0];
  }

  public Semantic.Package check(
    CompilerDebugger debugger,
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
      debugger.recordSource(source, package_name, filename);
      var lectics = lexer.lex(source.path(), source.contents());
      debugger.record(lectics, package_name, filename);
      var syntactics = parser.parse(lectics);
      debugger.record(syntactics, package_name, filename);
      for (var node = 0; node < syntactics.node_count(); node++) {
        if (!Syntactics.is_declaration(syntactics.type_of(node)))
          continue;
        var identifier = syntactics.text_of(node);
        for (
          var other_node = node + 1;
          other_node < syntactics.node_count();
          other_node++)
        {
          if (!Syntactics.is_declaration(syntactics.type_of(other_node)))
            continue;
          var other_identifier = syntactics.text_of(other_node);
          if (identifier.equals(other_identifier)) {
            throw Diagnostic
              .error(
                syntactics.subject_of(other_node),
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
            var other_node = 0;
            other_node < sources[previous_source].node_count();
            other_node++)
          {
            if (!Syntactics
              .is_declaration(sources[previous_source].type_of(other_node)))
              continue;
            var other_identifier = sources[previous_source].text_of(other_node);
            if (identifier.equals(other_identifier)) {
              throw Diagnostic
                .error(
                  syntactics.subject_of(node),
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
    debugger.record(sources, source_count, package_name);
    if (package_name.equals(Name.of("duru"))) {
      for (var builtin : Semantic.BUILTINS) {
        symbols.add(builtin.identifier(), builtin);
      }
    }
    for (var source = 0; source < source_count; source++) {
      for (var node = 0; node < sources[source].node_count(); node++) {
        if (!Syntactics.is_declaration(sources[source].type_of(node)))
          continue;
        symbols
          .get(sources[source].subject_of(node), sources[source].text_of(node));
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
      for (var node = 0; node < sources[source].node_count(); node++) {
        if (!Syntactics.is_declaration(sources[source].type_of(node)))
          continue;
        if (identifier.equals(sources[source].text_of(node))) {
          // TODO: fix checking after rewriting SymbolChecker
          return SymbolChecker
            .check(externalNames, this::accessSymbol, package_name, null);
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
