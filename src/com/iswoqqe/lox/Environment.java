package com.iswoqqe.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private Environment previous;
    private Map<String, Object> map = new HashMap<>();

    Environment() {
        this.previous = null;
    }

    Environment(Environment previous) {
        this.previous = previous;
    }

    void define(String name, Object val) {
        map.put(name, val);
    }

    void define(Token name, Object val) {
        map.put(name.lexeme, val);
    }

    void assign(Token name, Object val) {
        if (!map.containsKey(name.lexeme)) {
            if (previous != null) {
                previous.assign(name, val);
            } else {
                throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
            }
        }
        map.put(name.lexeme, val);
    }

    Object get(Token name) {
        if (!map.containsKey(name.lexeme)) {
            if (previous != null) {
                return previous.get(name);
            }
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
        return map.get(name.lexeme);
    }
}
/*
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
}*/
