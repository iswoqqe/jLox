package com.iswoqqe.lox;

import java.util.List;

public class Function implements Callable {
    private final Stmt.Function declaration;
    private final Environment closure;

    Function(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); ++i) {
            environment.define(declaration.params.get(i), arguments.get(i));
        }

        try {
            interpreter.interpretInEnvironment(declaration.body, environment);
        } catch (Return ret) {
            return ret.value;
        }

        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<fn: ");
        builder.append(declaration.name.lexeme);
        builder.append('(');

        boolean first = true;

        for (Token param : declaration.params) {
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
