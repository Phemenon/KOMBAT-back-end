package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement;

import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;

public class DoneCommand implements Statement {
    @Override
    public void execute(StrategyContext ctx) {
        ctx.done();
    }
}
