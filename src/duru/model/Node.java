package duru.model;

import java.util.List;

public sealed interface Node {
    sealed interface Declaration extends Node {}

    record Entrypoint(Portion portion, Statement body) implements Declaration {}

    sealed interface Statement extends Node {}

    record Block(Portion portion, List<Statement> innerStatements)
        implements Statement
    {}

    record Discard(Portion portion, Expression discarded)
        implements Statement
    {}

    sealed interface Expression extends Node {}

    record Scoping(Portion portion, Expression namespace, Token.Identifier name)
        implements Expression
    {}

    record Calling(
        Portion portion,
        Expression callee,
        List<Expression> arguments) implements Expression
    {}

    record NumberConstant(Portion portion, Token.NumberConstant token)
        implements Expression
    {}

    Portion portion();
}
