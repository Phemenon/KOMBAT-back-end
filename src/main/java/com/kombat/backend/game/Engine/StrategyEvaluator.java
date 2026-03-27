package com.kombat.backend.game.Engine;

import com.kombat.backend.game.Exception.InvalidExpressionException;
import com.kombat.backend.game.Exception.LexicalException;
import com.kombat.backend.game.Exception.SyntaxError;
import com.kombat.backend.game.GameContext;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Strategy;
import com.kombat.backend.game.Parser_Eval.Parser.StrategyParser;

public class StrategyEvaluator {

    private StrategyEvaluator() {}

    public static Strategy compile(String source)
            throws SyntaxError, LexicalException, InvalidExpressionException {

        StrategyParser parser = new StrategyParser();
        parser.processStrategy(source);
        return parser.parse();
    }

    public static void evaluate(Strategy strategy, GameContext context) {
        strategy.execute(context);
    }
}
