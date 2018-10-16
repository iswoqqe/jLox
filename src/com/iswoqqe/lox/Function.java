package com.iswoqqe.lox;

import java.util.List;

public class Function implements Callable {
    private final Expr.Function definition;

    Function(Expr.Function definition) {
        this.definition = definition;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // interpreter should only pass arguments list of correct size

        for (int i = 0; i < definition.parameters.size(); ++i) {
            definition.resolved.get(i).value = arguments.get(i);
        }

        try {
            interpreter.interpret(definition.body);
        } catch (Return ret) {
            return ret.value;
        }

        return null;
    }

    @Override
    public int arity() {
        return definition.parameters.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<fn(");

        boolean first = true;

        for (Token param : definition.parameters) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(param.lexeme);
            first = false;
        }

        builder.append(")>");

        return builder.toString();
    }
}
