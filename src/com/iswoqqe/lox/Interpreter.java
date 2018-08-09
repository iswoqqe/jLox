package com.iswoqqe.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Enviroment enviroment = new Enviroment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        enviroment.pushScope();

        try {
            for (Stmt statement : stmt.statements) {
                execute(statement);
            }
        } finally {
            enviroment.popScope();
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        /*if (enviroment.isDefined(var.name)) {
            throw new RuntimeError(var.name, "Redefinition of vars not allowed.");
        }*/
        enviroment.define(stmt.name, evaluate(stmt.initializer));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object val = evaluate(stmt.expression);
        System.out.println(stringify(val));
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object val = evaluate(expr.value);

        enviroment.assign(expr.name, val);
        return null;
    }

    @Override
    public Object visitVarExpr(Expr.Var expr) {
        return enviroment.get(expr.name);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case COMMA:
                return right;
            case MINUS:
                checkNumbers(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumbers(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumbers(expr.operator, left, right);
                return (double) left / (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + right;
                }
                throw new RuntimeError(expr.operator, "Operands must be numbers or strings.");
            case GREATER:
                checkNumbers(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumbers(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumbers(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumbers(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.condition);

        if (isTruthy(condition)) {
            return evaluate(expr.trueBranch);
        } else {
            return evaluate(expr.falseBranch);
        }
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                checkNumber(expr.operator, right);
                return -(double) right;
            case BANG:
                checkNumber(expr.operator, right);
                return !isTruthy(right);
        }

        return null;
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private String stringify(Object obj) {
        if (obj == null) {
            return "nil";
        }
        return obj.toString();
    }

    private boolean isTruthy(Object val) {
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean) {
            return (boolean) val;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    private void checkNumber(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumbers(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
}
