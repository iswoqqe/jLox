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
    public Void visitBlockStmt(Stmt.Block block) {
        enviroment.pushScope();

        try {
            for (Stmt statement : block.statements) {
                execute(statement);
            }
        } finally {
            enviroment.popScope();
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var var) {
        /*if (enviroment.isDefined(var.name)) {
            throw new RuntimeError(var.name, "Redefinition of vars not allowed.");
        }*/
        enviroment.define(var.name, evaluate(var.initializer));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression expression) {
        evaluate(expression.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print print) {
        Object val = evaluate(print.expression);
        System.out.println(stringify(val));
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign assign) {
        Object val = evaluate(assign.value);

        enviroment.assign(assign.name, val);
        return null;
    }

    @Override
    public Object visitVarExpr(Expr.Var var) {
        return enviroment.get(var.name);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        switch (binary.operator.type) {
            case COMMA:
                return right;
            case MINUS:
                checkNumbers(binary.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumbers(binary.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumbers(binary.operator, left, right);
                return (double) left / (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + right;
                }
                throw new RuntimeError(binary.operator, "Operands must be numbers or strings.");
            case GREATER:
                checkNumbers(binary.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumbers(binary.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumbers(binary.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumbers(binary.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping grouping) {
        return evaluate(grouping.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary ternary) {
        Object condition = evaluate(ternary.condition);

        if (isTruthy(condition)) {
            return evaluate(ternary.trueBranch);
        } else {
            return evaluate(ternary.falseBranch);
        }
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary unary) {
        Object right = evaluate(unary.right);

        switch(unary.operator.type) {
            case MINUS:
                checkNumber(unary.operator, right);
                return -(double) right;
            case BANG:
                checkNumber(unary.operator, right);
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
