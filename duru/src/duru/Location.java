package duru;

public record Location(
  Source source,
  int beginIndex,
  int beginLine,
  int beginColumn,
  int endIndex,
  int endLine,
  int endColumn)
{
  public static Location at(Source source, int beginIndex, int endIndex) {
    var index  = 0;
    var line   = 1;
    var column = 1;
    for (;
      index != beginIndex;
      index = source.contents().offsetByCodePoints(index, 1))
    {
      if (source.contents().codePointAt(index) != '\n') {
        column++;
        continue;
      }
      line++;
      column = 1;
    }
    var beginLine   = line;
    var beginColumn = column;
    for (;
      index != endIndex;
      index = source.contents().offsetByCodePoints(index, 1))
    {
      if (source.contents().codePointAt(index) != '\n') {
        column++;
        continue;
      }
      line++;
      column = 1;
    }
    var endLine   = line;
    var endColumn = column;
    return new Location(
      source,
      beginIndex,
      beginLine,
      beginColumn,
      endIndex,
      endLine,
      endColumn);
  }

  @Override
  public String toString() {
    if (beginLine != endLine) {
      return "%s:%d.%d-%d.%d"
        .formatted(source, beginLine, beginColumn, endLine, endColumn);
    }
    return "%s:%d.%d-%d".formatted(source, beginLine, beginColumn, endColumn);
  }
}
