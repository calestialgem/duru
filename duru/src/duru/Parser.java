package duru;

public final class Parser {
  private final Syntactics_Buffer syntactics;
  private Lectics lectics;
  private int current_token;

  public Parser() {
    syntactics = new Syntactics_Buffer();
  }

  public Syntactics parse(Lectics lectics) {
    syntactics.clear();
    this.lectics = lectics;
    current_token = 0;
    while (current_token != lectics.token_count()) {
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
    var keyword_token = take(Token.ENTRYPOINT);
    if (keyword_token == -1)
      return false;
    if (!parse_statement()) {
      throw missing("body of entrypoint");
    }
    add_node(Node.ENTRYPOINT, keyword_token);
    return true;
  }

  private boolean parse_statement() {
    if (parse_block()) {
      return true;
    }
    return false;
  }

  private boolean parse_block() {
    var opening_token = take(Token.OPENING_BRACE);
    if (opening_token == -1)
      return false;
    add_node(Node.BLOCK_END, opening_token);
    while (true) {
      if (parse_statement())
        continue;
      var closing_token = take(Token.CLOSING_BRACE);
      if (closing_token == -1)
        throw missing("`}` of block");
      add_node(Node.BLOCK_BEGIN, closing_token);
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

  private int take(Token kind) {
    if (lectics.kind_of(current_token) != kind)
      return -1;
    return current_token++;
  }

  private void add_node(Node kind, int representative_token) {
    syntactics.add(kind, lectics.begin_of(representative_token));
  }
}
