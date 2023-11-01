package duru.syntactics;

import java.util.function.Function;

import duru.lectics.Token;

/** Stores information on how to parse a unary operation. */
record UnaryOperationParser<PrecedenceType extends Node.Expression>(
  Class<? extends Token> operatorClass,
  Function<PrecedenceType, PrecedenceType> initializer,
  String name)
{}
