package com.iswoqqe.lox;

class ASTPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    String getString(Stmt stmt) {
        return stmt.accept(this);
    }

    String getString(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block");

        for (Stmt statement : stmt.statements) {
            builder.append(' ');
            builder.append(statement.accept(this));
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return parenthesize("def " + stmt.name.lexeme, stmt.initializer);
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("redef " + expr.name.lexeme, expr.value);
    }

    @Override
    public String visitVarExpr(Expr.Var expr) {
        return "@" + expr.name.lexeme;
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
        if (expr.value instanceof String) {
            return '"' + (String) expr.value +'"';
        }
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
