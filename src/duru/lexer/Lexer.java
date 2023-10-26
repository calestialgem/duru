package duru.lexer;

import java.util.ArrayList;
import java.util.List;

import duru.model.Location;
import duru.model.Portion;
import duru.model.Token;

public final class Lexer {
    private static final long maxValue = 0xFFFF_FFFF_FFFF_FFFFl;

    private String contents;

    private final List<Token> tokens;

    private int index;

    private int line;

    private int column;

    private int beginIndex;

    private int beginLine;

    private int beginColumn;

    public Lexer() { tokens = new ArrayList<>(); }

    public List<Token> lex(String contents) {
        this.contents = contents;
        tokens.clear();
        index  = 0;
        line   = 1;
        column = 1;
        lex();
        return List.copyOf(tokens);
    }

    private void lex() {
        while (hasCharacter()) {
            beginIndex  = index;
            beginLine   = line;
            beginColumn = column;
            var initial = getCharacter();
            advance();
            switch (initial) {
                case ' ', '\t', '\n' -> {}
                case '\r' -> {
                    if (hasCharacter() && getCharacter() == '\n')
                        advance();
                }
                case '#' -> {
                    while (hasCharacter() && getCharacter() != '\n')
                        advance();
                    advance();
                }
                case '{' -> tokens.add(new Token.OpeningBrace(getPortion()));
                case '}' -> tokens.add(new Token.ClosingBrace(getPortion()));
                case '(' ->
                    tokens.add(new Token.OpeningParenthesis(getPortion()));
                case ')' ->
                    tokens.add(new Token.ClosingParenthesis(getPortion()));
                case ';' -> tokens.add(new Token.Semicolon(getPortion()));
                case '.' -> tokens.add(new Token.Dot(getPortion()));
                default -> {
                    if (isLetter(initial)) {
                        while (hasCharacter() && isWord(getCharacter()))
                            advance();
                        var portion = getPortion();
                        var text    = contents.substring(beginIndex, index);
                        tokens.add(switch (text) {
                            case "entrypoint" -> new Token.Entrypoint(portion);
                            default -> new Token.Identifier(portion, text);
                        });
                        break;
                    }
                    if (isDigit(initial)) {
                        var value = 0L + initial - '0';
                        while (hasCharacter()) {
                            var character = getCharacter();
                            if (!isDigit(character))
                                break;
                            var digit = character - '0';
                            if (Long
                                .compareUnsigned(
                                    value,
                                    Long.divideUnsigned(maxValue, 10))
                                > 0)
                                throw new RuntimeException(
                                    "Huge number constant!");
                            value *= 10;
                            if (Long.compareUnsigned(value, maxValue - digit)
                                > 0)
                                throw new RuntimeException(
                                    "Huge number constant!");
                            value += digit;
                            advance();
                        }
                        tokens
                            .add(new Token.NumberConstant(getPortion(), value));
                        break;
                    }
                    throw new RuntimeException(
                        "Unknown character `%c`!".formatted(initial));
                }
            }
        }
    }

    private boolean hasCharacter() { return index != contents.length(); }

    private int getCharacter() { return contents.codePointAt(index); }

    private void advance() {
        column++;
        if (getCharacter() == '\n') {
            line++;
            column = 1;
        }
        index = contents.offsetByCodePoints(index, 1);
    }

    private Portion getPortion() {
        return new Portion(
            new Location(beginIndex, beginLine, beginColumn),
            new Location(index, line, column));
    }

    private boolean isWord(int character) {
        return isLetter(character) || isDigit(character);
    }

    private boolean isLetter(int character) {
        return character >= 'a' && character <= 'z'
            || character >= 'A' && character <= 'Z';
    }

    private boolean isDigit(int character) {
        return character >= '0' && character <= '9';
    }
}
