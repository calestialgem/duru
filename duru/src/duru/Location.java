package duru;

public record Location(Source source, int begin, int end) {
  @Override
  public String toString() {
    var string = new StringBuilder();
    var index  = 0;
    var line   = 1;
    var column = 1;
    for (;
      index != begin;
      index = source.contents().offsetByCodePoints(index, 1))
    {
      if (source.contents().codePointAt(index) != '\n') {
        column++;
        continue;
      }
      line++;
      column = 1;
    }
    string.append(source);
    string.append(':');
    string.append(line);
    string.append('.');
    string.append(column);
    if (end - begin < 2) {
      return string.toString();
    }
    var beginLine = line;
    for (;
      index != end;
      index = source.contents().offsetByCodePoints(index, 1))
    {
      if (source.contents().codePointAt(index) != '\n') {
        column++;
        continue;
      }
      line++;
      column = 1;
    }
    string.append('-');
    if (beginLine == line) {
      string.append(column);
      return string.toString();
    }
    string.append(line);
    string.append('.');
    string.append(column);
    return string.toString();
  }
}
