package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;

public record MoveCommand(Direction dir) implements Statement {

    @Override
    public void execute(StrategyContext ctx) {
        if (ctx.isDone()) return;
        ctx.move(dir);
    }
}
