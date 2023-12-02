package duru;

public final class Token_Iterator {
  private Lectics iterated;
  private int index;
  private int end_index;
  private int content_index;
  private int line;
  private int column;

  public Token_Iterator() {}

  public void iterate(Lectics iterated) {
    this.iterated = iterated;
    index = 0;
    end_index = 0;
    content_index = 0;
    line = 1;
    column = 1;
    recalculate();
  }

  public void advance() {
    if (kind().is_varying())
      end_index++;
    index++;
    recalculate();
  }

  private void recalculate() {
    var source_location = iterated.content.length();
    if (has()) {
      source_location = begin();
    }
    while (content_index != source_location) {
      if (iterated.content.charAt(content_index) != '\n') {
        column++;
      }
      else {
        line++;
        column = 1;
      }
      content_index++;
    }
  }

  public boolean has() {
    return index != iterated.count();
  }

  public int index() {
    return index;
  }

  public Token kind() {
    return iterated.kind(index);
  }

  public int begin() {
    return iterated.begin(index);
  }

  public int end() {
    var fixed_length = kind().length();
    if (fixed_length != 0)
      return begin() + fixed_length;
    return iterated.end(end_index);
  }

  public String text() {
    return iterated.content.substring(begin(), end());
  }

  public int length() {
    return end() - begin();
  }

  public int line() {
    return line;
  }

  public int begin_column() {
    return column;
  }

  public int end_column() {
    return column + length();
  }

  public String subject() {
    if (!has())
      return "%s:%d.%d".formatted(iterated.path, line(), begin_column());
    return "%s:%d.%d-%d"
      .formatted(iterated.path, line(), begin_column(), end_column());
  }

  public String explanation() {
    if (!has())
      return "end of file";
    switch (kind()) {
      case Token.ENTRYPOINT -> {
        return "keyword `entrypoint`";
      }
      case Token.OPENING_BRACE -> {
        return "punctuation `{`";
      }
      case Token.CLOSING_BRACE -> {
        return "punctuation `}`";
      }
      case Token.IDENTIFIER -> {
        return "identifier `%s`".formatted(text());
      }
      default ->
        throw Diagnostic
          .failure(
            "%s:%d.%d".formatted(iterated.path, line, column),
            "unknown token kind `%s`",
            kind());
    }
  }
}
