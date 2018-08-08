package com.iswoqqe.lox;

class ASTPrinter implements Expr.Visitor<String> {
    String getString(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary tenary) {
        return parenthesize("ternary", tenary.condition, tenary.trueBranch, tenary.falseBranch);
    }

    private String parenthesize(String fname, Expr ...args) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(fname);

        for (Expr expr : args) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }

        builder.append(")");

        return builder.toString();
    }
}
