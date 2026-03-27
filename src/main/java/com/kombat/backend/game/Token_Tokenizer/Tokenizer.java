package com.kombat.backend.game.Token_Tokenizer;

import com.kombat.backend.game.Exception.LexicalException;
import com.kombat.backend.game.Exception.SyntaxError;
import com.kombat.backend.game.Interface.NextPeek;

import java.util.NoSuchElementException;

public class Tokenizer implements NextPeek {

    private final String src;
    private Token next;
    private int pos;


    public Tokenizer(String src) {
        this.src = src;
        this.pos = 0;
        computeNext();
    }

    public boolean hasNextToken() {
        return next != null && next.type() != TokenType.EOF;
    }

    public void checkNextToken() {
        if (!hasNextToken()) throw new NoSuchElementException("no more tokens");
    }


    public Token consume() {
        checkNextToken();
        Token result = next;
        computeNext();
        return result;
    }

    @Override
    public Token Peek() {
        if (next == null) throw new NoSuchElementException("no more tokens");
        return next;
    }

    public boolean peek(TokenType type) {
        return next != null && next.type() == type;
    }

    /** Consume s or throw ExceptionPack.SyntaxError */
    public Token consume(TokenType expected) throws SyntaxError {
        if (!hasNextToken())
            throw new SyntaxError("Unexpected end of input");

        if (Peek().type() == expected) {
            return consume();
        }

        throw new SyntaxError("Expected " + expected + " but found " + Peek().type()
        );
    }


    @Override
    public void computeNext() {
        // skip whitespace
        while (pos < src.length() &&
                (src.charAt(pos) == ' ' || src.charAt(pos) == '\n' || src.charAt(pos) == '\t' || src.charAt(pos) == '\r')) {
            pos++;
        }

        if (pos >= src.length()) {
            next = new Token(TokenType.EOF, "");
            return;
        }

        char c = src.charAt(pos);

        // comment
        if (c == '#') {
            while (pos < src.length() && src.charAt(pos) != '\n') pos++;
            computeNext();
            return;
        }

        // number
        if (Character.isDigit(c)) {
            StringBuilder s = new StringBuilder();
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                s.append(src.charAt(pos++));
            }
            long value = Long.parseLong(s.toString());
            next = new Token(TokenType.NUMBER, s.toString(), value);
            return;
        }

        // identifier / keyword
        if (Character.isLetter(c)) {
            StringBuilder s = new StringBuilder();
            while (pos < src.length() &&
                    (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) {
                s.append(src.charAt(pos++));
            }

            String word = s.toString();

            next = switch (word) {
                case "if" -> new Token(TokenType.IF, word);
                case "then" -> new Token(TokenType.THEN, word);
                case "else" -> new Token(TokenType.ELSE, word);
                case "while" -> new Token(TokenType.WHILE, word);
                case "move" -> new Token(TokenType.MOVE, word);
                case "up" -> new Token(TokenType.UP, word);
                case "down" -> new Token(TokenType.DOWN, word);
                case "upleft" -> new Token(TokenType.UP_LEFT, word);
                case "upright" -> new Token(TokenType.UP_RIGHT, word);
                case "downleft" -> new Token(TokenType.DOWN_LEFT, word);
                case "downright" -> new Token(TokenType.DOWN_RIGHT, word);
                case "shoot" -> new Token(TokenType.SHOOT, word);
                case "done" -> new Token(TokenType.DONE, word);
                case "opponent" -> new Token(TokenType.OPPONENT, word);
                case "ally" -> new Token(TokenType.ALLY, word);
                case "nearby" -> new Token(TokenType.NEARBY, word);
                case "random" -> new Token(TokenType.RANDOM, word);


                default -> new Token(TokenType.IDENT, word);
            };
            return;
        }

        // single char tokens
        switch (c) {
            case '+': next = new Token(TokenType.PLUS, "+"); break;
            case '-': next = new Token(TokenType.MINUS, "-"); break;
            case '*': next = new Token(TokenType.MUL, "*"); break;
            case '/': next = new Token(TokenType.DIV, "/"); break;
            case '%': next = new Token(TokenType.MOD, "%"); break;
            case '^': next = new Token(TokenType.POW, "^"); break;
            case '=': next = new Token(TokenType.ASSIGN, "="); break;
            case '(': next = new Token(TokenType.LPAREN, "("); break;
            case ')': next = new Token(TokenType.RPAREN, ")"); break;
            case '{': next = new Token(TokenType.LBRACE, "{"); break;
            case '}': next = new Token(TokenType.RBRACE, "}"); break;
            default:
                System.out.println((int) c);
                throw new LexicalException("Illegal character: " + c);
        }

        pos++;

    }

}
