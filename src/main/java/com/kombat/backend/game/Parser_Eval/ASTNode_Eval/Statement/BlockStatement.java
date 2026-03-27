package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement;

import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Interface.StrategyContext;

import java.util.List;

public record BlockStatement(List<Statement> ListStatements) implements Statement {

    @Override
    public void execute(StrategyContext ctx) {
        for (Statement statement : ListStatements) {
            statement.execute(ctx);
        }
    }
}
