package com.iswoqqe.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    private static boolean hadRuntimeError = false;
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("usage: jlox [script]");
            System.exit(64); // command line usage error
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String filename) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filename));
        run(new String(bytes, StandardCharsets.UTF_8.name()));

        if (hadError) {
            System.exit(65); // data format error
        }
        if (hadRuntimeError) {
            System.exit(70); // internal software error
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            StringBuilder builder = new StringBuilder();

            while (true) {
                System.out.print("> ");
                String tmp = reader.readLine();

                if (tmp.equals("")) {
                    break;
                }

                builder.append(tmp);
                builder.append('\n');
            }
            //System.out.print("> ");
            //run(reader.readLine());
            run(builder.toString());
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        if (hadError) {
            System.out.println("Scan error.");
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) {
            System.out.println("Parse error.");
            return;
        }

        Resolver resolver = new Resolver(statements, interpreter.getGlobalsRef());
        resolver.resolve();

        if (hadError) {
            System.out.println("Resolve error.");
            return;
        }

        interpreter.defineNewGlobals(resolver.getNewGlobalsRef());

        ASTPrinter printer = new ASTPrinter();
        for (Stmt stmt : statements) {
            System.out.println(printer.getString(stmt));
        }

        interpreter.interpret(statements);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + " column " + error.token.column + "]");
        hadRuntimeError = true;
    }

    static void error(int line, int column, String message) {
        report(line, column, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, token.column, " at EOF", message);
        } else {
            report(token.line, token.column, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, int column, String where, String message) {
        hadError = true;
        System.err.println("[" + line + "," + column + "] Error" + where + ": " + message);
    }
}
