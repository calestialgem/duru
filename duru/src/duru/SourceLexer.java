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
  private int               base;
  private BigDecimal        value;

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
            value = BigDecimal.valueOf(initial - '0');
            base  = 10;
            if (initial == '0' && hasCharacter()) {
              var givenBase = switch (getCharacter()) {
                case 'b', 'B' -> 2;
                case 'o', 'O' -> 8;
                case 'd', 'D' -> 10;
                case 'x', 'X' -> 16;
                default -> -1;
              };
              if (givenBase != -1) {
                base = givenBase;
                advance();
                value = BigDecimal.valueOf(enforceDigit());
              }
            }
            while (hasCharacter()) {
              if (Text.isUnderscore(getCharacter())) {
                advance();
                appendDigit(enforceDigit());
                continue;
              }
              var digit = lexDigit();
              if (digit.isEmpty()) {
                break;
              }
              appendDigit(digit.getFirst());
            }
            var fractionLength = 0;
            if (take('.')) {
              while (hasCharacter()) {
                if (Text.isUnderscore(getCharacter())) {
                  advance();
                  appendDigit(enforceDigit());
                  fractionLength++;
                  continue;
                }
                var digit = lexDigit();
                if (digit.isEmpty()) {
                  break;
                }
                appendDigit(digit.getFirst());
                fractionLength++;
              }
            }
            var exponent                 = 0;
            var exponentSeparator        = base == 10 ? 'e' : 'p';
            var capitalExponentSeparator = exponentSeparator + 'A' - 'a';
            if (take(exponentSeparator) || take(capitalExponentSeparator)) {
              var isNegative = take('-');
              if (!isNegative) {
                take('+');
              }
              while (hasCharacter()) {
                if (Text.isUnderscore(getCharacter())) {
                  advance();
                  var digit = enforceDigit();
                  if (exponent > Integer.MAX_VALUE / 10) {
                    throw Diagnostic.error(location(), "huge exponent");
                  }
                  exponent *= 10;
                  if (exponent > Integer.MAX_VALUE - digit) {
                    throw Diagnostic.error(location(), "huge exponent");
                  }
                  exponent += digit;
                  continue;
                }
                var digit = lexDigit();
                if (digit.isEmpty()) {
                  break;
                }
                if (exponent > Integer.MAX_VALUE / 10) {
                  throw Diagnostic.error(location(), "huge exponent");
                }
                exponent *= 10;
                if (exponent > Integer.MAX_VALUE - digit.getFirst()) {
                  throw Diagnostic.error(location(), "huge exponent");
                }
                exponent += digit.getFirst();
              }
              if (isNegative) {
                exponent = -exponent;
              }
            }
            if (base == 10) {
              if (exponent < Integer.MIN_VALUE + fractionLength) {
                throw Diagnostic.error(location(), "too precise");
              }
              exponent -= fractionLength;
              try {
                value = value.scaleByPowerOfTen(exponent);
              }
              catch (@SuppressWarnings("unused") ArithmeticException cause) {
                throw Diagnostic.error(location(), "too precise");
              }
            }
            else {
              var power = switch (base) {
                case 2 -> 1;
                case 8 -> 3;
                case 16 -> 4;
                default -> -1;
              };
              if (fractionLength > Integer.MAX_VALUE / power) {
                throw Diagnostic.error(location(), "too precise");
              }
              fractionLength *= power;
              if (exponent < Integer.MIN_VALUE + fractionLength) {
                throw Diagnostic.error(location(), "too precise");
              }
              exponent -= fractionLength;
              try {
                for (var i = 0; i < exponent; i++) {
                  value = value.multiply(BigDecimal.valueOf(2));
                }
                for (var i = 0; i > exponent; i--) {
                  value = value.divide(BigDecimal.valueOf(2));
                }
              }
              catch (@SuppressWarnings("unused") ArithmeticException cause) {
                throw Diagnostic.error(location(), "too precise");
              }
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
              case "struct" -> lex(Token.Struct::new);
              case "const" -> lex(Token.Const::new);
              case "var" -> lex(Token.Var::new);
              case "fn" -> lex(Token.Fn::new);
              case "if" -> lex(Token.If::new);
              case "else" -> lex(Token.Else::new);
              case "for" -> lex(Token.For::new);
              case "break" -> lex(Token.Break::new);
              case "continue" -> lex(Token.Continue::new);
              case "return" -> lex(Token.Return::new);
              case "as" -> lex(Token.As::new);
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

  private void appendDigit(int digit) {
    try {
      value = value.multiply(BigDecimal.valueOf(base));
      value = value.add(BigDecimal.valueOf(digit));
    }
    catch (@SuppressWarnings("unused") ArithmeticException cause) {
      throw Diagnostic.error(location(), "huge number");
    }
  }

  private int enforceDigit() {
    var digit = lexDigit();
    if (!digit.isEmpty()) {
      return digit.getFirst();
    }
    if (hasCharacter()) {
      throw Diagnostic
        .failure(
          location(),
          "expected base-%d digit instead of `%c`",
          base,
          getCharacter());
    }
    throw Diagnostic
      .failure(location(), "expected base-%d digit at end of file", base);
  }

  private Optional<Integer> lexDigit() {
    if (!hasCharacter()) {
      return Optional.absent();
    }
    var character = getCharacter();
    return switch (base) {
      case 2, 8, 10 -> {
        var digit = character - '0';
        if (digit >= 0 && digit < base) {
          advance();
          yield Optional.present(digit);
        }
        yield Optional.absent();
      }
      case 16 -> {
        if (character >= '0' && character <= '9') {
          advance();
          yield Optional.present(character - '0');
        }
        if (character >= 'a' && character <= 'f') {
          advance();
          yield Optional.present(character - 'a');
        }
        if (character >= 'A' && character <= 'F') {
          advance();
          yield Optional.present(character - 'A');
        }
        yield Optional.absent();
      }
      default -> Optional.absent();
    };
  }

  private Location location() {
    return location(begin);
  }

  private Location location(int begin) {
    return Location.at(source, begin, index);
  }

  private boolean take(int character) {
    if (!hasCharacter() || getCharacter() != character) {
      return false;
    }
    advance();
    return true;
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
