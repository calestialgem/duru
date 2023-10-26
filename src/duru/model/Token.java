package duru.model;

public record Token(TokenType type, String text, int line, int column) {}
