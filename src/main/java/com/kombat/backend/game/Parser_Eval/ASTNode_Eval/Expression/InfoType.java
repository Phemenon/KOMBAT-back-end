package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Token_Tokenizer.Token;

public enum InfoType {
    ALLY,
    OPPONENT;

    public static InfoType fromToken(Token token) {
        return switch (token.type()) {
            case ALLY -> ALLY;
            case OPPONENT -> OPPONENT;
            default -> throw new IllegalArgumentException("Invalid Expression");
        };
    }

}
