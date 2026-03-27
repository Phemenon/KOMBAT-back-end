package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.StrategyContext;

public record InfoExpression(InfoType type) implements Expression {

    @Override
    public long eval(StrategyContext ctx) {
        return switch (type) {
            case ALLY -> ctx.ally();
            case OPPONENT -> ctx.opponent();
        };
    }
}
