package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.StrategyContext;

public record VariableExpression(String variable) implements Expression {

    @Override
    public long eval(StrategyContext ctx) {
        return ctx.getVar(variable);
    }
}
