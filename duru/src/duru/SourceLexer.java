package duru;

import java.math.BigDecimal;
import java.util.function.Function;

public final class SourceLexer {
  public static List<Token> lex(Source source) {
    var lexer = new SourceLexer(source);
    return lexer.lex();
  }

  private final Source      source;
  private ListBuffer<Token> tokens;
  private int               index;
  private int               begin;
  private int               initial;

  private SourceLexer(Source source) {
    this.source = source;
  }

  private List<Token> lex() {
    tokens = ListBuffer.create();
    index  = 0;
    while (hasCharacter()) {
      begin   = index;
      initial = getCharacter();
      advance();
      switch (initial) {
        case ' ', '\r', '\n' -> {}
        case '/' -> {
          if (!hasCharacter()) {
            lex(Token.Slash::new);
            break;
          }
          if (getCharacter() == '/') {
            advance();
            while (hasCharacter() && getCharacter() != '\n') {
              advance();
            }
            break;
          }
          if (getCharacter() == '=') {
            advance();
            lex(Token.SlashEqual::new);
            break;
          }
          if (getCharacter() != '*') {
            lex(Token.Slash::new);
            break;
          }
          advance();
          var blockComments = 1;
          while (hasCharacter()) {
            var character = getCharacter();
            advance();
            if (character == '*' && hasCharacter() && getCharacter() == '/') {
              advance();
              blockComments--;
              if (blockComments == 0) {
                break;
              }
            }
            if (character == '/' && hasCharacter() && getCharacter() == '*') {
              advance();
              blockComments++;
            }
          }
          if (blockComments != 0) {
            throw Diagnostic.error(location(), "incomplete block comment");
          }
        }
        case '{' -> lex(Token.OpeningBrace::new);
        case '}' -> lex(Token.ClosingBrace::new);
        case '(' -> lex(Token.OpeningParenthesis::new);
        case ')' -> lex(Token.ClosingParenthesis::new);
        case ';' -> lex(Token.Semicolon::new);
        case '.' -> lex(Token.Dot::new);
        case ',' -> lex(Token.Comma::new);
        case '~' -> lex(Token.Tilde::new);
        case ':' -> lexRepeatable(Token.Colon::new, Token.ColonColon::new);
        case '=' -> lexRepeatable(Token.Equal::new, Token.EqualEqual::new);
        case '*' -> lexExtensible(Token.Star::new, Token.StarEqual::new);
        case '%' -> lexExtensible(Token.Percent::new, Token.PercentEqual::new);
        case '^' -> lexExtensible(Token.Caret::new, Token.CaretEqual::new);
        case '!' ->
          lexExtensible(Token.Exclamation::new, Token.ExclamationEqual::new);
        case '+' ->
          lexRepeatableOrExtensible(
            Token.Plus::new,
            Token.PlusPlus::new,
            Token.PlusEqual::new);
        case '-' ->
          lexRepeatableOrExtensible(
            Token.Minus::new,
            Token.MinusMinus::new,
            Token.MinusEqual::new);
        case '&' ->
          lexRepeatableOrExtensible(
            Token.Ampersand::new,
            Token.AmpersandAmpersand::new,
            Token.AmpersandEqual::new);
        case '|' ->
          lexRepeatableOrExtensible(
            Token.Pipe::new,
            Token.PipePipe::new,
            Token.PipeEqual::new);
        case '<' ->
          lexRepeatableAndExtensible(
            Token.Left::new,
            Token.LeftLeft::new,
            Token.LeftEqual::new,
            Token.LeftLeftEqual::new);
        case '>' ->
          lexRepeatableAndExtensible(
            Token.Right::new,
            Token.RightRight::new,
            Token.RightEqual::new,
            Token.RightRightEqual::new);
        case '"' -> {
          var value = new StringBuilder();
          while (true) {
            if (!hasCharacter()) {
              throw Diagnostic.error(location(), "incomplete string constant");
            }
            var escapeSequenceBegin = index;
            var character           = getCharacter();
            advance();
            if (character == '\n') {
              throw Diagnostic.error(location(), "incomplete string constant");
            }
            if (character == '"') {
              break;
            }
            if (character != '\\') {
              value.appendCodePoint(character);
              continue;
            }
            if (!hasCharacter()) {
              throw Diagnostic.error(location(), "incomplete escape sequence");
            }
            character = getCharacter();
            advance();
            switch (character) {
              case '\\', '"' -> value.appendCodePoint(character);
              case 't' -> value.append('\t');
              case 'r' -> value.append('\r');
              case 'n' -> value.append('\n');
              default ->
                throw Diagnostic
                  .error(
                    location(escapeSequenceBegin),
                    "unknown escape sequence `%c`",
                    character);
            }
          }
          tokens.add(new Token.StringConstant(location(), value.toString()));
        }
        default -> {
          if (Text.isDigit(initial)) {
            var value = BigDecimal.valueOf(initial - '0');
            while (hasCharacter()) {
              var separatorBegin = index;
              var character      = getCharacter();
              if (Text.isUnderscore(character)) {
                advance();
                if (!hasCharacter() || !Text.isDigit(getCharacter())) {
                  throw Diagnostic
                    .error(
                      location(separatorBegin),
                      "expected digit after separator");
                }
                character = getCharacter();
              }
              else if (!Text.isDigit(character)) {
                break;
              }
              advance();
              value = value.scaleByPowerOfTen(1);
              value = value.add(BigDecimal.valueOf(character - '0'));
            }
            tokens.add(new Token.NumberConstant(location(), value));
            break;
          }

          if (Text.isIdentifierInitial(initial)) {
            while (hasCharacter() && Text.isIdentifierBody(getCharacter())) {
              advance();
            }
            var text = source.contents().substring(begin, index);
            switch (text) {
              case "extern" -> lex(Token.Extern::new);
              case "public" -> lex(Token.Public::new);
              case "using" -> lex(Token.Using::new);
              case "type" -> lex(Token.Type::new);
              case "const" -> lex(Token.Const::new);
              case "var" -> lex(Token.Var::new);
              case "fn" -> lex(Token.Fn::new);
              case "if" -> lex(Token.If::new);
              case "else" -> lex(Token.Else::new);
              case "for" -> lex(Token.For::new);
              case "break" -> lex(Token.Break::new);
              case "continue" -> lex(Token.Continue::new);
              case "return" -> lex(Token.Return::new);
              default -> tokens.add(new Token.Identifier(location(), text));
            }
            break;
          }
          throw Diagnostic.error(location(), "unknown character `%c`", initial);
        }
      }
    }
    return tokens.toList();
  }

  private void lex(Function<Location, Token> singleLexer) {
    tokens.add(singleLexer.apply(location()));
  }

  private void lexRepeatable(
    Function<Location, Token> singleLexer,
    Function<Location, Token> repeatedLexer)
  {
    if (hasCharacter() && getCharacter() == initial) {
      advance();
      lex(repeatedLexer);
      return;
    }
    lex(singleLexer);
  }

  private void lexExtensible(
    Function<Location, Token> singleLexer,
    Function<Location, Token> extendedLexer)
  {
    if (hasCharacter() && getCharacter() == '=') {
      advance();
      lex(extendedLexer);
      return;
    }
    lex(singleLexer);
  }

  private void lexRepeatableOrExtensible(
    Function<Location, Token> singleLexer,
    Function<Location, Token> repeatedLexer,
    Function<Location, Token> extendedLexer)
  {
    if (hasCharacter() && getCharacter() == initial) {
      advance();
      lex(repeatedLexer);
      return;
    }
    lexExtensible(singleLexer, extendedLexer);
  }

  private void lexRepeatableAndExtensible(
    Function<Location, Token> singleLexer,
    Function<Location, Token> repeatedLexer,
    Function<Location, Token> extendedLexer,
    Function<Location, Token> repeatedAndExtendedLexer)
  {
    if (hasCharacter() && getCharacter() == initial) {
      advance();
      lexExtensible(repeatedLexer, repeatedAndExtendedLexer);
      return;
    }
    lexExtensible(singleLexer, extendedLexer);
  }

  private Location location() {
    return location(begin);
  }

  private Location location(int begin) {
    return Location.at(source, begin, index);
  }

  private boolean hasCharacter() {
    return index != source.contents().length();
  }

  private int getCharacter() {
    return source.contents().codePointAt(index);
  }

  private void advance() {
    index = source.contents().offsetByCodePoints(index, 1);
  }
}
