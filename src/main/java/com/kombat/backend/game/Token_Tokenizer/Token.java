package com.kombat.backend.game.Token_Tokenizer;

/**
 * @param number only used if NUMBER
 */
public record Token(TokenType type, String therefromFile, long number) {
    public Token(TokenType type, String therefromFile) {
        this(type, therefromFile, 0);
    }

    @Override
    public String toString() {
        return type + (type == TokenType.NUMBER ? "(" + number + ")" : "");
    }
}
