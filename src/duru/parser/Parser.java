package duru.parser;

import java.util.ArrayList;
import java.util.List;

import duru.lexer.Lexer;
import duru.model.Node;
import duru.model.Token;

public final class Parser {
    private final Lexer lexer;

    private String contents;

    private List<Token> tokens;

    private final List<Node.Declaration> declarations;

    private int index;

    public Parser() {
        lexer        = new Lexer();
        declarations = new ArrayList<>();
    }

    public List<Node.Declaration> parse(String contents) {
        this.contents = contents;
        tokens        = lexer.lex(contents);
        declarations.clear();
        index = 0;
        parse();
        return List.copyOf(declarations);
    }

    private void parse() {}
}
