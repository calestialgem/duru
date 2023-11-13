package duru;

public final class ConfigurationLexer {
  public static List<Token> lex(String text) {
    var lexer = new ConfigurationLexer(text);
    return lexer.lex();
  }

  private final String text;

  private ConfigurationLexer(String text) {
    this.text = text;
  }

  private List<Token> lex() {
    throw Diagnostic.failure("unimplemented");
  }
}
