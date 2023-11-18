package duru;

public sealed interface Token {
  record OpeningBrace(Location location) implements Token {}

  record ClosingBrace(Location location) implements Token {}

  record OpeningParenthesis(Location location) implements Token {}

  record ClosingParenthesis(Location location) implements Token {}

  record Semicolon(Location location) implements Token {}

  record Dot(Location location) implements Token {}

  record Comma(Location location) implements Token {}

  record Equal(Location location) implements Token {}

  record Star(Location location) implements Token {}

  record Left(Location location) implements Token {}

  record Public(Location location) implements Token {}

  record Proc(Location location) implements Token {}

  record Struct(Location location) implements Token {}

  record Var(Location location) implements Token {}

  record If(Location location) implements Token {}

  record Return(Location location) implements Token {}

  record Identifier(Location location, String text) implements Token {}

  record NaturalConstant(Location location, long value) implements Token {}

  record StringConstant(Location location, String value) implements Token {}

  Location location();
}
