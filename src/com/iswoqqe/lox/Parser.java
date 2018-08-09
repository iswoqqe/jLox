package com.iswoqqe.lox;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {}
    private boolean parsingVars = false;

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    /*Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }*/

    private Stmt declaration() {
        try {
            if (parsingVars || match(TokenType.VAR)) {
                parsingVars = true;
                return varDeclaration();
            }
            return statement();
        } catch (ParseError e) {
            parsingVars = false;
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        Expr initializer = new Expr.Literal(null);

        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        if (!match(TokenType.COMMA)) {
            consume(TokenType.SEMICOLON, "Expected ';' after var statement");
            parsingVars = false;
        }

        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStmt();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return blockStmt();
        }
        return expressionStmt();
    }

    private Stmt blockStmt() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected matching '}' after '{'.");
        return new Stmt.Block(statements);
    }

    private Stmt printStmt() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression in print statement.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expr = assignment();

        if (parsingVars) {
            return expr;
        }

        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = comma();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Var) {
                Token name = ((Expr.Var) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = equality();

        if (match(TokenType.QUESTION)) {
            Expr trueBranch = ternary();
            consume(TokenType.COLON, "Missing ':' in ternary expression");
            Expr falseBranch = ternary();
            expr = new Expr.Ternary(expr, trueBranch, falseBranch);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (match(TokenType.LESS, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Var(previous());
        }

        if (match(TokenType.COMMA, TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL, TokenType.LESS, TokenType.GREATER,
                TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL, TokenType.PLUS, TokenType.STAR, TokenType.SLASH)) {
            ParseError err = error(previous(), "Binary operator at start of expression.");
            expression();
            throw err;
        }

        throw error(peek(), "Expected expression.");
    }

    private void synchronize() {
        if (isAtEnd()) {
            return;
        }

        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            if (match(TokenType.CLASS, TokenType.FN, TokenType.FOR, TokenType.IF, TokenType.VAR, TokenType.WHILE,
                    TokenType.PRINT, TokenType.RETURN)) {
                return;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean match(TokenType ...types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        current += 1;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
