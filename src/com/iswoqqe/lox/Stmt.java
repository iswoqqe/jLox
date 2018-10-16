package com.iswoqqe.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<T> {
        T visitExpressionStmt(Expression stmt);
        T visitPrintStmt(Print stmt);
        T visitVarStmt(Var stmt);
        T visitBlockStmt(Block stmt);
        T visitIfStmt(If stmt);
        T visitWhileStmt(While stmt);
        T visitReturnStmt(Return stmt);
    }

    abstract <T> T accept(Visitor<T> visitor);

    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    static class Var extends Stmt {
        final Token name;
        Variable resolved;
        final Expr initializer;

        Var(Token name, Variable resolved, Expr initializer) {
            this.name = name;
            this.resolved = resolved;
            this.initializer = initializer;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class While extends Stmt {
        final Expr condition;
        final Stmt statement;

        While(Expr condition, Stmt statement) {
            this.condition = condition;
            this.statement = statement;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    static class Return extends Stmt {
        final Expr expression;

        Return(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }
}
