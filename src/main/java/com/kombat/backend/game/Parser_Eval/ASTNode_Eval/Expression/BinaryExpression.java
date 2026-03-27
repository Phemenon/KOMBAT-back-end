package com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression;

import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.StrategyContext;

public record BinaryExpression(Expression left, Operator operator, Expression right) implements Expression {

    @Override
    public long eval(StrategyContext ctx) {
        long l = left.eval(ctx);
        long r = right.eval(ctx);

        return switch (operator) {
            case PLUS -> l + r;
            case MINUS -> l - r;
            case MUL -> l * r;
            case DIV -> {
                if (r == 0) throw new ArithmeticException();
                yield l / r;
            }
            case MOD -> {
                if (r == 0) throw new ArithmeticException();
                yield l % r;
            }
            case POW -> (long) Math.pow(l, r);
        };
    }
}
