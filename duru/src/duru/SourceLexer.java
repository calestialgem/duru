package duru;

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
          if (hasCharacter() && getCharacter() == '/') {
            advance();
            while (hasCharacter() && getCharacter() != '\n') {
              advance();
            }
            break;
          }
          if (!hasCharacter() || getCharacter() != '*') {
            throw Subject.error("incomplete comment");
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
            throw Subject.error("incomplete block comment");
          }
        }
        case '{' -> tokens.add(new Token.OpeningBrace(location()));
        case '}' -> tokens.add(new Token.ClosingBrace(location()));
        case '(' -> tokens.add(new Token.OpeningParenthesis(location()));
        case ')' -> tokens.add(new Token.ClosingParenthesis(location()));
        case ';' -> tokens.add(new Token.Semicolon(location()));
        case '.' -> tokens.add(new Token.Dot(location()));
        case ',' -> tokens.add(new Token.Comma(location()));
        case '=' -> tokens.add(new Token.Equal(location()));
        case '*' -> tokens.add(new Token.Star(location()));
        case '<' -> tokens.add(new Token.Left(location()));
        case '"' -> {
          var value = new StringBuilder();
          while (true) {
            if (!hasCharacter())
              throw Subject.error("incomplete string constant");
            var character = getCharacter();
            advance();
            if (character == '\n')
              throw Subject.error("incomplete string constant");
            if (character == '"')
              break;
            if (character != '\\') {
              value.appendCodePoint(character);
              continue;
            }
            if (!hasCharacter())
              throw Subject.error("incomplete escape sequence");
            character = getCharacter();
            advance();
            switch (character) {
              case '\\', '"' -> value.appendCodePoint(character);
              case 't' -> value.append('\t');
              case 'r' -> value.append('\r');
              case 'n' -> value.append('\n');
              default ->
                throw Subject.error("unknown escape sequence `%c`", character);
            }
          }
          tokens.add(new Token.StringConstant(location(), value.toString()));
        }
        default -> {
          if (Text.isDigit(initial)) {
            var value = 0L;
            value += initial - '0';
            while (hasCharacter()) {
              var character = getCharacter();
              if (Text.isUnderscore(character)) {
                if (!hasCharacter() || !Text.isDigit(getCharacter())) {
                  throw Subject.error("expected a digit");
                }
                character = getCharacter();
              }
              else if (!Text.isDigit(character)) {
                break;
              }
              var digit = character - '0';
              if (Long.compareUnsigned(value, Long.divideUnsigned(-1L, 10)) > 0)
                throw Subject.error("huge number");
              value *= 10;
              if (Long.compareUnsigned(value, -1L - digit) > 0)
                throw Subject.error("huge number");
              value += digit;
            }
            tokens.add(new Token.NaturalConstant(location(), value));
            break;
          }

          if (Text.isIdentifierInitial(initial)) {
            while (hasCharacter() && Text.isIdentifierBody(getCharacter())) {
              advance();
            }
            var text = source.contents().substring(begin, index);
            switch (text) {
              case "public" -> tokens.add(new Token.Public(location()));
              case "proc" -> tokens.add(new Token.Proc(location()));
              case "struct" -> tokens.add(new Token.Struct(location()));
              case "var" -> tokens.add(new Token.Var(location()));
              case "if" -> tokens.add(new Token.If(location()));
              case "else" -> tokens.add(new Token.Else(location()));
              case "return" -> tokens.add(new Token.Return(location()));
              default -> tokens.add(new Token.Identifier(location(), text));
            }
            break;
          }
          throw Subject.error("unknown character `%c`", initial);
        }
      }
    }
    return tokens.toList();
  }

  private Location location() {
    return new Location(source, begin, index);
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
