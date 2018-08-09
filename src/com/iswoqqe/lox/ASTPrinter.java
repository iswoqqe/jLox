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
        return parenthesize("def", stmt.name.lexeme, stmt.initializer);
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
        return parenthesize("redef", expr.name.lexeme, expr.value);
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
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize("ternary", expr.condition, expr.trueBranch, expr.falseBranch);
    }

    private String parenthesize(Object ...args) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(asString(args[0]));

        for (int i = 1; i < args.length; ++i) {
            builder.append(" ");
            builder.append(asString(args[i]));
        }

        builder.append(")");

        return builder.toString();
    }

    private String asString(Object obj) {
        if (obj instanceof Expr) {
            return ((Expr) obj).accept(this);
        }
        if (obj instanceof Stmt) {
            return ((Stmt) obj).accept(this);
        }
        return obj.toString();
    }
}
