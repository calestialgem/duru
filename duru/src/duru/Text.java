package duru;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
        "using",
        "struct",
        "const",
        "var",
        "fn",
        "if",
        "else",
        "for",
        "break",
        "continue",
        "return",
        "as" ->
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

  public static boolean is_identifier_body(char c) {
    return is_identifier_initial(c) || is_digit(c);
  }

  public static boolean is_identifier_initial(char c) {
    return is_letter(c) || is_underscore(c);
  }

  public static boolean is_letter(char c) {
    return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
  }

  public static boolean is_underscore(char c) {
    return c == '_';
  }

  public static boolean is_digit(char c) {
    return c >= '0' && c <= '9';
  }

  private Text() {}
}
