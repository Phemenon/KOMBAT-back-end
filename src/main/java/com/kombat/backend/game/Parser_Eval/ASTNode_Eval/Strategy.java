package com.kombat.backend.game.Parser_Eval.ASTNode_Eval;

import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;

import java.util.List;

public record Strategy(List<Statement> statements) {

    public void execute(StrategyContext ctx) {
        for (Statement state : statements) {
            try {
                state.execute(ctx);
            } catch (ArithmeticException e) {
                ctx.done();
                throw e;
            }
        }
    }

}
