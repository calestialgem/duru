package duru;

public final class Text {
  public static String quote(String constant) {
    var string = new StringBuilder();
    for (
      var i = 0;
      i < constant.length();
      i = constant.offsetByCodePoints(i, 1))
    {
      var character = constant.codePointAt(i);
      switch (character) {
        case '\\', '"' -> string.append('\\').append(character);
        case '\t' -> string.append('\\').append('t');
        case '\r' -> string.append('\\').append('r');
        case '\n' -> string.append('\\').append('n');
        default -> string.appendCodePoint(character);
      }
    }
    return string.toString();
  }

  public static String getModule(String name) {
    var separator = name.indexOf('.');
    if (separator == -1)
      return name;
    return name.substring(0, separator);
  }

  public static String getPackage(String name) {
    var separator = name.lastIndexOf('.');
    if (separator == -1)
      return name;
    return name.substring(0, separator);
  }

  public static String getSymbol(String name) {
    var separator = name.lastIndexOf('.');
    if (separator == -1)
      return name;
    return name.substring(separator + 1, name.length());
  }

  public static boolean isReserved(String name) {
    return switch (name) {
      case "public", "proc", "struct", "var", "if", "else", "return" -> true;
      default -> false;
    };
  }

  public static boolean isReservedForConfiguration(String name) {
    return switch (name) {
      case "executable", "library" -> true;
      default -> false;
    };
  }

  public static boolean isIdentifierBody(int codepoint) {
    return isIdentifierInitial(codepoint) || isDigit(codepoint);
  }

  public static boolean isIdentifierInitial(int codepoint) {
    return isLetter(codepoint) || isUnderscore(codepoint);
  }

  public static boolean isLetter(int codepoint) {
    return codepoint >= 'a' && codepoint <= 'z'
      || codepoint >= 'A' && codepoint <= 'Z';
  }

  public static boolean isUnderscore(int codepoint) {
    return codepoint == '_';
  }

  public static boolean isDigit(int codepoint) {
    return codepoint >= '0' && codepoint <= '9';
  }

  private Text() {}
}
