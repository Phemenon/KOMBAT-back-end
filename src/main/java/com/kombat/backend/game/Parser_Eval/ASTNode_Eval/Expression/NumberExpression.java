package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.StrategyContext;

public record NumberExpression(long value) implements Expression {

    @Override
    public long eval(StrategyContext ctx) {
        return value;
    }
}
