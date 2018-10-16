package com.iswoqqe.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<T> {
        T visitBinaryExpr(Binary expr);
        T visitCallExpr(Call expr);
        T visitGroupingExpr(Grouping expr);
        T visitLiteralExpr(Literal expr);
        T visitTernaryExpr(Ternary expr);
        T visitUnaryExpr(Unary expr);
        T visitVarExpr(Var expr);
        T visitAssignExpr(Assign expr);
        T visitOrExpr(Or expr);
        T visitAndExpr(And expr);
        T visitFunctionExpr(Function expr);
    }

    abstract <T> T accept(Visitor<T> visitor);

    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;

        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Ternary extends Expr {
        final Expr condition;
        final Expr trueBranch;
        final Expr falseBranch;

        Ternary(Expr condition, Expr trueBranch, Expr falseBranch) {
            this.condition = condition;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Var extends Expr {
        final Token name;
        Variable resolved;

        Var(Token name, Variable resolved) {
            this.name = name;
            this.resolved = resolved;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVarExpr(this);
        }
    }

    static class Assign extends Expr {
        final Token name;
        Variable resolved;
        final Expr value;

        Assign(Token name, Variable resolved, Expr value) {
            this.name = name;
            this.resolved = resolved;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Or extends Expr {
        final Expr left;
        final Expr right;

        Or(Expr left, Expr right) {
            this.left = left;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitOrExpr(this);
        }
    }

    static class And extends Expr {
        final Expr left;
        final Expr right;

        And(Expr left, Expr right) {
            this.left = left;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAndExpr(this);
        }
    }

    static class Function extends Expr {
        final List<Token> parameters;
        List<Variable> resolved;
        final List<Stmt> body;

        Function(List<Token> parameters, List<Variable> resolved, List<Stmt> body) {
            this.parameters = parameters;
            this.resolved = resolved;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFunctionExpr(this);
        }
    }
}
