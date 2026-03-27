package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;

public record WhileStatement(Expression condition, Statement body) implements Statement {

    @Override
    public void execute(StrategyContext ctx) {
        int counter = 0;

        while (!ctx.isDone() && counter < 10000 && condition.eval(ctx) > 0) {
            body.execute(ctx);
            counter++;
        }
    }
}
