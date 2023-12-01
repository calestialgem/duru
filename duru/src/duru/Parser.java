package duru;

public final class Parser {
  public static Parser create() {
    return new Parser(Syntactics_Buffer.create(), null, 0);
  }

  private final Syntactics_Buffer syntactics;
  private Lectics lectics;
  private int current_token;

  private Parser(
    Syntactics_Buffer syntactics,
    Lectics lectics,
    int current_token)
  {
    this.syntactics = syntactics;
    this.lectics = lectics;
    this.current_token = current_token;
  }

  public Syntactics parse(Lectics lectics) {
    syntactics.clear();
    this.lectics = lectics;
    current_token = 0;
    while (current_token != lectics.token_count()) {
      if (!parse_declaration())
        throw missing("top level declaration");
    }
    return syntactics.bake(lectics.path, lectics.contents);
  }

  private boolean parse_declaration() {
    if (parse_entrypoint())
      return true;
    return false;
  }

  private boolean parse_entrypoint() {
    var keyword_token = take(Lectics.KEYWORD_ENTRYPOINT);
    if (keyword_token == -1)
      return false;
    if (!parse_statement()) {
      throw missing("body of entrypoint");
    }
    add_node(Syntactics.ENTRYPOINT_DECLARATION, keyword_token);
    return true;
  }

  private boolean parse_statement() {
    if (parse_block()) {
      return true;
    }
    return false;
  }

  private boolean parse_block() {
    var opening_token = take(Lectics.OPENING_BRACE);
    if (opening_token == -1)
      return false;
    add_node(Syntactics.BLOCK_STATEMENT_END, opening_token);
    while (true) {
      if (parse_statement())
        continue;
      var closing_token = take(Lectics.CLOSING_BRACE);
      if (closing_token == -1)
        throw missing("`}` of block");
      add_node(Syntactics.BLOCK_STATEMENT_BEGIN, closing_token);
      return true;
    }
  }

  private RuntimeException missing(String explanation) {
    if (current_token == lectics.token_count()) {
      return Diagnostic
        .error(
          lectics.subject_of(current_token - 1),
          "expected %s after %s",
          explanation,
          lectics.explain(current_token - 1));
    }
    return Diagnostic
      .error(
        lectics.subject_of(current_token),
        "expected %s instead of %s",
        explanation,
        lectics.explain(current_token));
  }

  private int take(byte token_type) {
    if (lectics.type_of(current_token) != token_type)
      return -1;
    return current_token++;
  }

  private void add_node(byte node_type, int representative_token) {
    syntactics.add_node(node_type, lectics.begin_of(representative_token));
  }
}
