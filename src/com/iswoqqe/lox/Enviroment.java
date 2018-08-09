package com.iswoqqe.lox;

import com.iswoqqe.lox.lib.PersistentHashMap;

import java.util.Stack;

class Enviroment {
    private static final Object NOT_FOUND = new Object();
    private PersistentHashMap<String, Object> map = new PersistentHashMap<>();
    private Stack<PersistentHashMap<String, Object>> scopes = new Stack<>();

    void pushScope() {
        scopes.push(map);
    }

    void popScope() {
        map = scopes.pop();
    }

    boolean isDefined(Token name) {
        return map.hasKey(name.lexeme);
    }

    void define(Token name, Object val) {
        map = map.with(name.lexeme, val);
    }

    void assign(Token name, Object val) {
        if (!map.hasKey(name.lexeme)) {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
        map = map.with(name.lexeme, val);
    }

    Object get(Token name) {
        Object o = map.get(name.lexeme, NOT_FOUND);

        if (NOT_FOUND.equals(o)) {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }

        return o;
    }
}
