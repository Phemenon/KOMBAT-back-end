package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;
import com.kombat.backend.game.Token_Tokenizer.Token;

public record AssignStatement(String name, Expression expr) implements Statement {

    public AssignStatement(Token name, Expression expr) {
        this(name.therefromFile(), expr);
    }

    public Expression expression() {
        return expr;
    }

    @Override
    public void execute(StrategyContext ctx) {
        if (ctx.isDone()) return;
        long value = expr.eval(ctx);
        ctx.setVar(name, value);
    }

    @Override
    public String toString() {
        return name + " = " + expr;
    }
}
