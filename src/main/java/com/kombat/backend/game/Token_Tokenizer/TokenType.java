package com.kombat.backend.game.Token_Tokenizer;

public enum TokenType {
    // literals
    NUMBER,
    IDENT,

    // operators
    PLUS, MINUS, MUL, DIV, MOD, POW,
    ASSIGN,

    // punctuation
    LPAREN, RPAREN,
    LBRACE, RBRACE,

    // keywords
    IF, THEN, ELSE, WHILE,
    MOVE, SHOOT, DONE,
    OPPONENT, ALLY, NEARBY, RANDOM,

    // directions
    UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT,

    EOF
}
