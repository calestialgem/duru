package duru.lectics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntFunction;

import duru.source.Source;

/** Transforms a source file to a list of tokens. */
public final class Lexer {
  /** Lexes a source file.. */
  public static LexedSource lex(Source source) {
    Lexer lexer = new Lexer(source);
    return lexer.lex();
  }

  /** Source file that is lexed. */
  private final Source source;

  /** Tokens that were lexed. */
  private List<Token> tokens;

  /** Index of the currently lexed character. */
  private int current;

  /** Currently lexed token's first character. */
  private int initial;

  /** Currently lexed token's first character's first byte's index. */
  private int start;

  /** Constructor. */
  private Lexer(Source source) {
    this.source = source;
  }

  /** Lexes the source file. */
  private LexedSource lex() {
    tokens  = new ArrayList<Token>();
    current = 0;
    while (hasCharacter()) {
      initial = getCharacter();
      start   = current;
      advance();
      switch (initial) {
        case ' ', '\t', '\r', '\n' -> {}
        case '#' -> {
          while (hasCharacter()) {
            int character = getCharacter();
            advance();
            if (character == '\n') {
              break;
            }
          }
        }
        case '{' -> lexSingle(Token.OpeningBrace::new);
        case '}' -> lexSingle(Token.ClosingBrace::new);
        case '(' -> lexSingle(Token.OpeningParenthesis::new);
        case ')' -> lexSingle(Token.ClosingParenthesis::new);
        case ';' -> lexSingle(Token.Semicolon::new);
        case '.' -> lexSingle(Token.Dot::new);
        case ',' -> lexSingle(Token.Comma::new);
        case '~' -> lexSingle(Token.Tilde::new);
        case ':' -> lexRepeatable(Token.Colon::new, Token.ColonColon::new);
        case '*' -> lexExtensible(Token.Star::new, Token.StarEqual::new);
        case '/' -> lexExtensible(Token.Slash::new, Token.SlashEqual::new);
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
        case '=' ->
          lexRepeatableAndExtensible(
            Token.Equal::new,
            Token.EqualEqual::new,
            Token.EqualEqual::new,
            Token.EqualEqualEqual::new);
        case '"' -> {
          StringBuilder builder = new StringBuilder();
          while (true) {
            int character;
            if (!hasCharacter() || (character = getCharacter()) == '\n') {
              throw source
                .subject(start, current)
                .diagnose("error", "Incomplete string constant!")
                .toException();
            }
            advance();
            if (character == '"') {
              break;
            }
            builder.appendCodePoint(character);
          }
          String value = builder.toString();
          tokens.add(new Token.StringConstant(start, current, value));
        }
        default -> {
          if (initial >= 'a' && initial <= 'z'
            || initial >= 'A' && initial <= 'Z')
          {
            while (hasCharacter()) {
              int     character = getCharacter();
              boolean isWord    =
                character >= 'a' && character <= 'z'
                  || character >= 'A' && character <= 'Z'
                  || character >= '0' && character <= '9'
                  || character == '_';
              if (!isWord) {
                break;
              }
              advance();
            }
            String text = source.contents().substring(start, current);
            Token  token;
            switch (text) {
              case "entrypoint" -> token = new Token.Entrypoint(start);
              case "public" -> token = new Token.Public(start);
              case "using" -> token = new Token.Using(start);
              case "as" -> token = new Token.As(start);
              case "proc" -> token = new Token.Proc(start);
              case "const" -> token = new Token.Const(start);
              case "var" -> token = new Token.Var(start);
              case "if" -> token = new Token.If(start);
              case "else" -> token = new Token.Else(start);
              case "while" -> token = new Token.While(start);
              case "break" -> token = new Token.Break(start);
              case "continue" -> token = new Token.Continue(start);
              case "return" -> token = new Token.Return(start);
              default -> token = new Token.Identifier(start, text);
            }
            tokens.add(token);
            break;
          }
          if (initial >= '0' && initial <= '9') {
            int        digit = initial - '0';
            NumberBase base  = NumberBase.of(10);
            if (digit == 0 && hasCharacter()) {
              Optional<NumberBase> givenBase = switch (getCharacter()) {
                case 'b', 'B' -> Optional.of(NumberBase.of(2));
                case 'o', 'O' -> Optional.of(NumberBase.of(8));
                case 'd', 'D' -> Optional.of(NumberBase.of(10));
                case 'x', 'X' -> Optional.of(NumberBase.of(16));
                default -> Optional.empty();
              };
              if (givenBase.isPresent()) {
                base = givenBase.get();
                advance();
                digit = enforceDigit(base);
              }
            }
            NumberBuilder builder = NumberBuilder.create(base);
            try {
              builder.insert(digit);
              while (hasCharacter()) {
                if (getCharacter() == '_') {
                  advance();
                  builder.insert(enforceDigit(base));
                }
                else {
                  OptionalInt maybeDigit = lexDigit(base);
                  if (maybeDigit.isEmpty()) {
                    break;
                  }
                  builder.insert(maybeDigit.getAsInt());
                }
              }
              if (hasCharacter() && getCharacter() == '.') {
                int start = current;
                advance();
                builder.fractionSeparator();
                OptionalInt firstDigit = lexDigit(base);
                if (firstDigit.isEmpty()) {
                  current = start;
                }
                else {
                  builder.insert(firstDigit.getAsInt());
                  while (hasCharacter()) {
                    if (getCharacter() == '_') {
                      advance();
                      builder.insert(enforceDigit(base));
                    }
                    else {
                      OptionalInt maybeDigit = lexDigit(base);
                      if (maybeDigit.isEmpty()) {
                        break;
                      }
                      builder.insert(maybeDigit.getAsInt());
                    }
                  }
                }
              }
              int exponentSeparator =
                base instanceof NumberBase.PowerOfTwo ? 'p' : 'e';
              if (hasCharacter()
                && (getCharacter() == exponentSeparator
                  || getCharacter() == exponentSeparator + 'A' - 'a'))
              {
                advance();
                boolean isNegative = hasCharacter() && getCharacter() == '-';
                if (isNegative || hasCharacter() && getCharacter() == '+') {
                  advance();
                }
                builder.exponentSeparator(isNegative);
                base = NumberBase.of(10);
                builder.insert(enforceDigit(base));
                while (hasCharacter()) {
                  if (getCharacter() == '_') {
                    advance();
                    builder.insert(enforceDigit(base));
                  }
                  else {
                    OptionalInt maybeDigit = lexDigit(base);
                    if (maybeDigit.isEmpty()) {
                      break;
                    }
                    builder.insert(maybeDigit.getAsInt());
                  }
                }
              }
              Token token;
              try {
                token =
                  new Token.NaturalConstant(
                    start,
                    current,
                    builder.buildLong());
              }
              catch (@SuppressWarnings("unused") ArithmeticException cause) {
                token =
                  new Token.RealConstant(start, current, builder.buildDouble());
              }
              tokens.add(token);
            }
            catch (ArithmeticException cause) {
              throw source
                .subject(start, current)
                .diagnose("error", "Could not lex the number constant!")
                .toException(cause);
            }
            break;
          }
          throw source
            .subject(start)
            .diagnose("error", "Unknown character `%c`!", initial)
            .toException();
        }
      }
    }
    return new LexedSource(source, tokens);
  }

  /** Takes a digit or throws. */
  private int enforceDigit(NumberBase base) {
    OptionalInt digit = lexDigit(base);
    if (digit.isPresent()) {
      return digit.getAsInt();
    }
    throw source
      .subject(start, current)
      .diagnose(
        "error",
        "Expected a digit %s!",
        hasCharacter()
          ? "instead of `%c`".formatted(getCharacter())
          : "at the end of the file")
      .toException();
  }

  /** Takes a new digit if it is of the given base. */
  private OptionalInt lexDigit(NumberBase base) {
    int         start = current;
    OptionalInt digit = lexDigit();
    if (digit.isPresent() && digit.getAsInt() >= base.radix()) {
      current = start;
      return OptionalInt.empty();
    }
    return digit;
  }

  /** Takes the next character as a digit if it exists. */
  private OptionalInt lexDigit() {
    if (!hasCharacter()) {
      return OptionalInt.empty();
    }
    int character = getCharacter();
    if (character >= '0' && character <= '9') {
      advance();
      return OptionalInt.of(character - '0');
    }
    if (character >= 'a' && character <= 'f') {
      advance();
      return OptionalInt.of(character - 'a' + 10);
    }
    if (character >= 'A' && character <= 'F') {
      advance();
      return OptionalInt.of(character - 'A' + 10);
    }
    return OptionalInt.empty();
  }

  /** Lexes a single punctuation. */
  private void lexSingle(IntFunction<Token> lexerFunction) {
    Token token = lexerFunction.apply(start);
    tokens.add(token);
  }

  /** Lexes a single or repeated punctuation. */
  private void lexRepeatable(
    IntFunction<Token> lexerFunction,
    IntFunction<Token> repeatedLexerFunction)
  {
    if (hasCharacter() && getCharacter() == initial) {
      advance();
      lexSingle(repeatedLexerFunction);
      return;
    }
    lexSingle(lexerFunction);
  }

  /** Lexes a single or extended punctuation. */
  private void lexExtensible(
    IntFunction<Token> lexerFunction,
    IntFunction<Token> extendedLexerFunction)
  {
    if (hasCharacter() && getCharacter() == '=') {
      advance();
      lexSingle(extendedLexerFunction);
      return;
    }
    lexSingle(lexerFunction);
  }

  /** Lexes a single, repeated, or extended punctuation. */
  private void lexRepeatableOrExtensible(
    IntFunction<Token> lexerFunction,
    IntFunction<Token> repeatedLexerFunction,
    IntFunction<Token> extendedLexerFunction)
  {
    if (hasCharacter() && getCharacter() == initial) {
      advance();
      lexSingle(repeatedLexerFunction);
      return;
    }
    if (hasCharacter() && getCharacter() == '=') {
      advance();
      lexSingle(extendedLexerFunction);
      return;
    }
    lexSingle(lexerFunction);
  }

  /** Lexes a single, repeated, extended, or repeated and extended
   * punctuation. */
  private void lexRepeatableAndExtensible(
    IntFunction<Token> lexerFunction,
    IntFunction<Token> repeatedLexerFunction,
    IntFunction<Token> extendedLexerFunction,
    IntFunction<Token> repeatedExtendedLexerFunction)
  {
    if (hasCharacter() && getCharacter() == initial) {
      advance();
      if (hasCharacter() && getCharacter() == '=') {
        advance();
        lexSingle(repeatedExtendedLexerFunction);
        return;
      }
      lexSingle(repeatedLexerFunction);
      return;
    }
    if (hasCharacter() && getCharacter() == '=') {
      advance();
      lexSingle(extendedLexerFunction);
      return;
    }
    lexSingle(lexerFunction);
  }

  /** Skips over the currently lexed character. */
  private void advance() {
    current = source.contents().offsetByCodePoints(current, 1);
  }

  /** Returns the currently lexed character. */
  private int getCharacter() {
    return source.contents().codePointAt(current);
  }

  /** Returns whether there is a character at the current index. */
  private boolean hasCharacter() {
    return current != source.contents().length();
  }
}
