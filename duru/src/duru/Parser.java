package duru;

public final class Parser {
  private final Syntactics_Buffer syntactics;
  private final Token_Iterator iterator;

  public Parser() {
    syntactics = new Syntactics_Buffer();
    iterator = new Token_Iterator();
  }

  public Syntactics parse(Lectics lectics) {
    syntactics.clear();
    iterator.iterate(lectics);
    while (iterator.has()) {
      if (!parse_declaration())
        throw missing("top level declaration");
    }
    return syntactics.bake(lectics.path, lectics.content);
  }

  private boolean parse_declaration() {
    if (parse_entrypoint())
      return true;
    return false;
  }

  private boolean parse_entrypoint() {
    if (iterator.kind() != Token.ENTRYPOINT)
      return false;
    var begin = iterator.begin();
    iterator.advance();
    if (!parse_statement()) {
      throw missing("body of entrypoint");
    }
    syntactics.add(Node.ENTRYPOINT, begin);
    return true;
  }

  private boolean parse_statement() {
    if (parse_block()) {
      return true;
    }
    return false;
  }

  private boolean parse_block() {
    if (iterator.kind() != Token.OPENING_BRACE)
      return false;
    syntactics.add(Node.BLOCK_END, iterator.begin());
    iterator.advance();
    while (true) {
      if (parse_statement())
        continue;
      if (iterator.kind() != Token.CLOSING_BRACE)
        throw missing("`}` of block");
      syntactics.add(Node.BLOCK_BEGIN, iterator.begin());
      iterator.advance();
      return true;
    }
  }

  private RuntimeException missing(String explanation) {
    return Diagnostic
      .error(
        iterator.subject(),
        "expected %s instead of %s",
        explanation,
        iterator.explanation());
  }
}
