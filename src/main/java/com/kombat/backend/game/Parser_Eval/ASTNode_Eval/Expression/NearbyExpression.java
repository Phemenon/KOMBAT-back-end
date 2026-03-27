package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.StrategyContext;

public record NearbyExpression(Direction direction) implements Expression {

    @Override
    public long eval(StrategyContext ctx) {
        return ctx.nearby(direction);
    }
}
