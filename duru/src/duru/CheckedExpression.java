package duru;

public record CheckedExpression(
  Semantic.Expression expression,
  Semantic.Type type)
{}
