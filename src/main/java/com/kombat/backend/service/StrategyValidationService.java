package com.kombat.backend.service;

import com.kombat.backend.game.Exception.InvalidExpressionException;
import com.kombat.backend.game.Exception.LexicalException;
import com.kombat.backend.game.Exception.SyntaxError;
import com.kombat.backend.game.Parser_Eval.Parser.StrategyParser;
import org.springframework.stereotype.Service;

@Service
public class StrategyValidationService {

    public void validate(String strategySource) {
        if (strategySource == null || strategySource.isBlank()) {
            throw new RuntimeException("Strategy must not be empty");
        }

        StrategyParser parser = new StrategyParser();

        try {
            parser.processStrategy(strategySource);
            parser.parse();
        } catch (LexicalException | SyntaxError | InvalidExpressionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}