package duru.model;

public record TokenType() {
    public static final TokenType openingBrace = new TokenType();

    public static final TokenType closingBrace = new TokenType();

    public static final TokenType openingParenthesis = new TokenType();

    public static final TokenType closingParenthesis = new TokenType();

    public static final TokenType semicolon = new TokenType();

    public static final TokenType dot = new TokenType();

    public static final TokenType entrypointKeyword = new TokenType();

    public static final TokenType identifier = new TokenType();

    public static final TokenType numberConstant = new TokenType();
}
