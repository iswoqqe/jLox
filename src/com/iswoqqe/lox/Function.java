package com.iswoqqe.lox;

import java.util.List;

public class Function implements Callable {
    private final Environment closure;
    private final List<Token> params;
    private final Stmt body;

    Function(Environment closure, List<Token> params, Stmt body) {
        this.closure = closure;
        this.params = params;
        this.body = body;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < params.size(); ++i) {
            environment.define(params.get(i), arguments.get(i));
        }

        try {
            interpreter.interpretInEnvironment(body, environment);
        } catch (Return ret) {
            return ret.value;
        }

        return null;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<fn(");

        boolean first = true;

        for (Token param : params) {
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
