package duru;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.math.BigDecimal;
import java.util.Locale;

public final class Text {
  private static final DecimalFormat formatter;

  static {
    formatter = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.US));
    formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
  }

  public static String format(BigDecimal number) {
    return formatter.format(number);
  }

  public static String quote(String constant) {
    var string = new StringBuilder();
    quote(string, constant);
    return string.toString();
  }

  public static void quote(StringBuilder string, String constant) {
    string.append('"');
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
    string.append('"');
  }

  public static boolean isReserved(String name) {
    return switch (name) {
      case
        "extern",
        "public",
        "proc",
        "struct",
        "var",
        "if",
        "else",
        "return" ->
        true;
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
