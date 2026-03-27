package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Token_Tokenizer.Token;

public enum Operator {
    PLUS,
    MINUS,
    MUL,
    DIV,
    MOD,
    POW;

    public static Operator fromToken(Token token) {
        return switch (token.type()) {
            case PLUS -> PLUS;
            case MINUS -> MINUS;
            case MUL -> MUL;
            case DIV -> DIV;
            case MOD -> MOD;
            case POW -> POW;
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }
}
