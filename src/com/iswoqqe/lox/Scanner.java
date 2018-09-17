package com.iswoqqe.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
    private final String source;
    private final List<Token>  tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int savedColumn;
    private boolean scanned = false;
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fn",    TokenType.FN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        //keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        if (scanned) {
            return tokens;
        }

        while (!isAtEnd()) {
            start = current;
            savedColumn = column;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        scanned = true;
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '?': addToken(TokenType.QUESTION); break;
            case ':': addToken(TokenType.COLON); break;
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '"': string(); break;
            case '.':
                if (isDigit(peekNext())) {
                    number();
                } else {
                    addToken(TokenType.DOT);
                }
                break;
            case '/':
                if (match('/')) {
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                    addToken(TokenType.COMMENT);
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case '#':
                if (match('t')) {
                    addToken(TokenType.TRUE);
                } else if (match('f')) {
                    addToken(TokenType.FALSE);
                } else {
                    Lox.error(line, column, "'#' must be followed by 't' or 'f'");
                }
            case ' ':
            case '\t':
            case '\n':
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c) || c == '_') {
                    identifier();
                } else {
                    Lox.error(line, column, "Unexpected character '" + c + "' while scanning.");
                }
                break;
        }
    }

    private void string() {
        while (!isAtEnd() && peek() != '"') {
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, column, "Unterminated string");
            return;
        }

        advance();

        String str = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, str);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        if (peek() == 'e' || peek() == 'E') {
            advance();

            if (peek() == '-') {
                advance();
            }

            while (isDigit(peek())) {
                advance();
            }
        }

        double num = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, num);
    }

    private void identifier() {
        while (isDigit(peek()) || isAlpha(peek())) {
            advance();
        }

        String str = source.substring(start, current);
        TokenType type = keywords.getOrDefault(str, TokenType.IDENTIFIER);
        addToken(type);
    }

    private void blockComment() {
        char c = advance();

        while (!isAtEnd()) {
            if (c == '*' && match('/')) {
                break;
            }

            c = advance();
        }

        addToken(TokenType.COMMENT);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current += 1;
        column += 1;

        char c = source.charAt(current - 1);

        if (c == '\n') {
            column = 1;
            line += 1;
        }

        return c;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 < source.length()) {
            return source.charAt(current + 1);
        }
        return '\0';
    }

    private boolean match(char expected) {
        if (isAtEnd() || peek() != expected) {
            return false;
        }

        advance();
        return true;
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <='9');
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line, savedColumn));
    }
}
