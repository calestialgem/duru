package duru.configuration;

import java.util.ArrayList;
import java.util.List;

public final class ConfigurationLexer {
    private String contents;

    private final List<ConfigurationToken> tokens;

    private int index;

    public ConfigurationLexer() { tokens = new ArrayList<>(); }

    public List<ConfigurationToken> lex(String contents)
        throws ConfigurationException
    {
        this.contents = contents;
        tokens.clear();
        index = 0;
        lex();
        return List.copyOf(tokens);
    }

    private void lex() throws ConfigurationException {
        while (hasCharacter()) {
            var start   = index;
            var initial = getCharacter();
            advance();
            switch (initial) {
                case ' ', '\t', '\n' -> {}
                case '\r' ->
                    { if (hasCharacter() && getCharacter() == '\n')
                    { advance(); } }
                case '#' -> {
                    while (hasCharacter() && getCharacter() != '\n') {
                        advance();
                    }
                    advance();
                }
                case '{' ->
                    tokens.add(new ConfigurationToken.OpeningBrace(start));
                case '}' ->
                    tokens.add(new ConfigurationToken.ClosingBrace(start));
                case ';' -> tokens.add(new ConfigurationToken.Semicolon(start));
                case '.' -> tokens.add(new ConfigurationToken.Dot(start));
                default -> {
                    if (isLetter(initial)) {
                        while (hasCharacter() && isWord(getCharacter())) {
                            advance();
                        }
                        var word = getText(start);
                        tokens.add(switch (word) {
                            case "project" ->
                                new ConfigurationToken.Project(start);
                            case "executable" ->
                                new ConfigurationToken.Executable(start);
                            default ->
                                new ConfigurationToken.Identifier(
                                    start,
                                    word.length());
                        });
                        break;
                    }
                    throw new ConfigurationException(
                        contents,
                        start,
                        1,
                        "Unknown character `%c`!",
                        initial);
                }
            }
        }
    }

    private boolean hasCharacter() { return index != contents.length(); }

    private int getCharacter() { return contents.codePointAt(index); }

    private void advance() { index = contents.offsetByCodePoints(index, 1); }

    private boolean isWord(int character) {
        return isLetter(character) || character >= '0' && character <= '9';
    }

    private boolean isLetter(int character) {
        return character >= 'a' && character <= 'z'
            || character >= 'A' && character <= 'Z';
    }

    private String getText(int start) {
        return contents.substring(start, index);
    }
}
