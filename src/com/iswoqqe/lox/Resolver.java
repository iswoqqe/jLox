package com.iswoqqe.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {
    private final List<Stmt> statements;
    private final List<Map<String, Variable>> environments = new ArrayList<>();

    Resolver(List<Stmt> statements, Map<String, Variable> builtins) {
        Map<String, Variable> globalEnv = new HashMap<>();

        for (Stmt stmt : statements) {
            if (stmt instanceof Stmt.Var) {
                Stmt.Var var = (Stmt.Var) stmt;

                if (globalEnv.containsKey(var.name.lexeme)) {
                    error(var.name, "Variable '" + var.name.lexeme + "' already defined in this scope.");
                } else {
                    globalEnv.put(var.name.lexeme, new Variable());
                }
            }
        }

        this.statements = statements;
        this.environments.add(builtins);
        this.environments.add(globalEnv);
    }

    Map<String, Variable> getNewGlobalsRef() {
        return environments.get(1);
    }

    void resolve() {
        for (Stmt stmt : statements) {
            resolveStmt(stmt);
        }
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolveExpr(expr.left);
        resolveExpr(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolveExpr(expr.callee);
        for (Expr e : expr.arguments) {
            resolveExpr(e);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolveExpr(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolveExpr(expr.condition);
        resolveExpr(expr.trueBranch);
        resolveExpr(expr.falseBranch);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolveExpr(expr.right);
        return null;
    }

    @Override
    public Void visitVarExpr(Expr.Var expr) {
        expr.resolved = getVar(expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        expr.resolved = getVar(expr.name);
        resolveExpr(expr.value);
        return null;
    }

    @Override
    public Void visitOrExpr(Expr.Or expr) {
        resolveExpr(expr.left);
        resolveExpr(expr.right);
        return null;
    }

    @Override
    public Void visitAndExpr(Expr.And expr) {
        resolveExpr(expr.left);
        resolveExpr(expr.right);
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        Map<String, Variable> newScope = new HashMap<>();

        for (int i = 0; i < expr.parameters.size(); ++i) {
            Variable var = new Variable();
            expr.resolved.set(i, var);
            newScope.put(expr.parameters.get(i).lexeme, var);
        }

        environments.add(newScope);

        for (Stmt stmt : expr.body) {
            resolveStmt(stmt);
        }

        environments.remove(environments.size() - 1);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolveExpr(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolveExpr(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        resolveExpr(stmt.initializer);

        if (!inGlobalScope()) {
            Map<String, Variable> env = environments.get(environments.size() - 1);

            if (env.containsKey(stmt.name.lexeme)) {
                error(stmt.name, "Variable '" + stmt.name.lexeme + "' already defined in this scope.");
                return null;
            }

            env.put(stmt.name.lexeme, new Variable());
        }
        stmt.resolved = getVar(stmt.name);

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        environments.add(new HashMap<>());

        for (Stmt s : stmt.statements) {
            resolveStmt(s);
        }

        environments.remove(environments.size() - 1);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolveExpr(stmt.condition);
        resolveStmt(stmt.thenBranch);
        resolveStmt(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolveExpr(stmt.condition);
        resolveStmt(stmt.statement);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        resolveExpr(stmt.expression);
        return null;
    }

    private void resolveExpr(Expr expr) {
        if (expr == null) {
            return;
        }
        expr.accept(this);
    }

    private void resolveStmt(Stmt stmt) {
        if (stmt == null) {
            return;
        }
        stmt.accept(this);
    }

    private boolean inGlobalScope() {
        return environments.size() <= 2;
    }

    private Variable getVar(Token identifier) {
        Variable resolved = null;

        for (Map<String, Variable> env : environments) {
            resolved = env.getOrDefault(identifier.lexeme, resolved);
        }

        if (resolved == null) {
            Lox.error(identifier, "Cannot resolve variable '" + identifier.lexeme + "' in this scope.");
        }

        return resolved;
    }

    private void error(Token token, String message) {
        Lox.error(token, message);
    }
}
