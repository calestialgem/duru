package duru.syntactics;

import java.util.function.BiFunction;

import duru.lectics.Token;

/** Stores information on how to parse a binary operation. */
record BinaryOperationParser<PrecedenceType extends Node.Expression>(
  Class<? extends Token> operatorClass,
  BiFunction<PrecedenceType, PrecedenceType, PrecedenceType> initializer,
  String name)
{}
