package com.iswoqqe.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {}
    private boolean parsingVars = false;
    private boolean commaOpDisabled = false;

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
                commaOpDisabled = true;
                return varDeclaration();
            }
            if (match(TokenType.FN)) {
                return funcDeclaration();
            }
            return statement();
        } catch (ParseError e) {
            parsingVars = false;
            commaOpDisabled = false;
            synchronize();
            return null;
        }
    }

    private Stmt funcDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name.");
        List<Token> parameters = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters.add(consume(TokenType.IDENTIFIER,
                        "Expected parameter name in function declaration."));
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after function parameters.");
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body.");

        List<Stmt> body = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE)) {
            body.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after function body.");

        return new Stmt.Function(name, parameters, body);
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
            commaOpDisabled = false;
        }

        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        /*if (match(TokenType.PRINT)) {
            return printStmt();
        }*/
        if (match(TokenType.LEFT_BRACE)) {
            return blockStmt();
        }
        if (match(TokenType.IF)) {
            return ifStmt();
        }
        if (match(TokenType.WHILE)) {
            return whileStmt();
        }
        if (match(TokenType.FOR)) {
            return forStmt();
        }
        if (match(TokenType.RETURN)) {
            return returnStmt();
        }
        return expressionStmt();
    }

    private Stmt returnStmt() {
        Token keyword = previous();
        Expr expression = null;

        if (!check(TokenType.SEMICOLON)) {
            expression = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after return statement.");
        return new Stmt.Return(keyword, expression);
    }

    private Stmt forStmt() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStmt();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after for loop condition.");

        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after for for clause.");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt whileStmt() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while statement condition.");

        Stmt statement = statement();

        return new Stmt.While(condition, statement);
    }

    private Stmt ifStmt() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if statement condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
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

        if (commaOpDisabled) {
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
        Expr expr = or();

        if (match(TokenType.QUESTION)) {
            Expr trueBranch = ternary();
            consume(TokenType.COLON, "Missing ':' in ternary expression");
            Expr falseBranch = ternary();
            expr = new Expr.Ternary(expr, trueBranch, falseBranch);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Expr right = or();
            expr = new Expr.Or(expr, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Expr right = and();
            expr = new Expr.And(expr, right);
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

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (match(TokenType.LEFT_PAREN)) {
            expr = finishCall(expr);
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            commaOpDisabled = true;
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
            commaOpDisabled = false;
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
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
