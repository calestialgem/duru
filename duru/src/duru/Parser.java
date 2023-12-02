package duru;

import java.util.Arrays;

public final class Parser {
  private byte[] node_types;
  private int[] node_begins;
  private int node_count;
  private Lectics lectics;
  private int current_token;

  public Parser() {
    node_types = new byte[0];
    node_begins = new int[0];
  }

  public Syntactics parse(Lectics lectics) {
    node_count = 0;
    this.lectics = lectics;
    current_token = 0;
    while (current_token != lectics.token_count()) {
      if (!parse_declaration())
        throw missing("top level declaration");
    }
    return new Syntactics(
      lectics.path,
      lectics.content,
      node_types,
      node_begins,
      node_count);
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
    var opening_token = take(Token.OPENING_BRACE);
    if (opening_token == -1)
      return false;
    add_node(Syntactics.BLOCK_STATEMENT_END, opening_token);
    while (true) {
      if (parse_statement())
        continue;
      var closing_token = take(Token.CLOSING_BRACE);
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

  private int take(Token kind) {
    if (lectics.kind_of(current_token) != kind)
      return -1;
    return current_token++;
  }

  private void add_node(byte node_type, int representative_token) {
    if (node_count == node_types.length) {
      var new_capacity = node_count * 2;
      if (new_capacity == 0)
        new_capacity = 1;
      node_types = Arrays.copyOf(node_types, new_capacity);
      node_begins = Arrays.copyOf(node_begins, new_capacity);
    }
    node_types[node_count] = node_type;
    node_begins[node_count] = lectics.begin_of(representative_token);
    node_count++;
  }
}
