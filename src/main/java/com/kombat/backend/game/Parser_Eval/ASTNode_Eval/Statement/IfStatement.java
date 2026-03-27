package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;

public record IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) implements Statement {

    @Override
    public void execute(StrategyContext ctx) {
        if (ctx.isDone()) return;

        long cond = condition.eval(ctx);
        if (cond > 0) thenBranch.execute(ctx);
        else elseBranch.execute(ctx);
    }
}
