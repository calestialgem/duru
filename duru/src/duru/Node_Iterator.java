package duru;

public final class Node_Iterator {
  private Syntactics iterated;
  private int index;
  private int end_index;
  private int line;
  private int column;

  public Node_Iterator() {}

  public void iterate(Syntactics iterated) {
    this.iterated = iterated;
    index = 0;
    end_index = 0;
    line = 1;
    column = 1;
    recalculate();
  }

  public void iterate_remaining(Node_Iterator other) {
    iterated = other.iterated;
    index = other.index;
    end_index = other.end_index;
    line = other.line;
    column = other.column;
    advance();
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
    for (var content_index = 0; content_index != source_location; content_index++) {
      if (iterated.content.charAt(content_index) != '\n') {
        column++;
      }
      else {
        line++;
        column = 1;
      }
    }
  }

  public boolean has() {
    return index != iterated.count();
  }

  public int index() {
    return index;
  }

  public Node kind() {
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
      case Node.ENTRYPOINT -> {
        return "entrypoint declaration";
      }
      case Node.BLOCK_BEGIN -> {
        return "block statement begin";
      }
      case Node.BLOCK_END -> {
        return "block statement end";
      }
      case Node.IDENTIFIER -> {
        return "identifier `%s`".formatted(text());
      }
      default ->
        throw Diagnostic
          .failure(
            "%s:%d.%d".formatted(iterated.path, line, column),
            "unknown node kind `%s`",
            kind());
    }
  }
}
