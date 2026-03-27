package com.kombat.backend.game.Parser_Eval.Parser;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Exception.InvalidExpressionException;
import com.kombat.backend.game.Exception.LexicalException;
import com.kombat.backend.game.Exception.SyntaxError;
import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression.InfoType;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression.Operator;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Strategy;
import com.kombat.backend.game.Token_Tokenizer.Token;
import com.kombat.backend.game.Token_Tokenizer.TokenType;
import com.kombat.backend.game.Token_Tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StrategyParser {
    private static final ASTNodeFactory factory = ASTNodeFactory.instance();
    private Tokenizer tk;

    public void processStrategy(String strategy) throws LexicalException {
        this.tk = new Tokenizer(strategy);
    }

    public Strategy parse() throws SyntaxError, InvalidExpressionException {
        List<Statement> statements = new ArrayList<>();

        if (!tk.hasNextToken())
            throw new SyntaxError("Empty strategy");

        while (tk.hasNextToken()) {
            statements.add(parseStatement());
        }

        return new Strategy(statements);
    }

    private Statement parseStatement() throws SyntaxError, InvalidExpressionException {

        if (tk.peek(TokenType.LBRACE)) return parseBlock();
        if (tk.peek(TokenType.IF)) return parseIf();
        if (tk.peek(TokenType.WHILE)) return parseWhile();

        if (tk.peek(TokenType.DONE) || tk.peek(TokenType.MOVE) || tk.peek(TokenType.SHOOT))
            return parseCommand();

        if (tk.peek(TokenType.IDENT)) {
            return parseAssignment();
        }

        throw new SyntaxError("Invalid statement");
    }

    private Statement parseAssignment() throws SyntaxError, InvalidExpressionException{
        Token name = tk.consume();

        if (isReservedWord(name)) throw new SyntaxError("Reserved word cannot be variable");

        tk.consume(TokenType.ASSIGN);

        Expression value = parseExpression();

        return factory.AssignStatement(name,value);
    }

    private static final Set<TokenType> RESERVED = Set.of(
            TokenType.ALLY,TokenType.DONE,TokenType.DOWN,TokenType.DOWN_LEFT,TokenType.DOWN_RIGHT,
            TokenType.ELSE,TokenType.IF,TokenType.MOVE,TokenType.NEARBY,TokenType.OPPONENT,
            TokenType.SHOOT,TokenType.THEN,TokenType.UP,TokenType.UP_LEFT,TokenType.UP_RIGHT,TokenType.WHILE
    );

    private boolean isReservedWord(Token token) {
        return RESERVED.contains(token.type());
    }

    private Statement parseCommand() throws SyntaxError, InvalidExpressionException{
        if (tk.peek(TokenType.DONE)) {
            tk.consume(TokenType.DONE);
            return factory.DoneCommand();
        }

        if (tk.peek(TokenType.MOVE)) {
            tk.consume(TokenType.MOVE);
            Direction dir = parseDirection();
            return factory.MoveCommand(dir);
        }

        if (tk.peek(TokenType.SHOOT)) {
            tk.consume(TokenType.SHOOT);
            Direction dir = parseDirection();
            Expression cost = parseExpression();
            return factory.ShootCommand(dir, cost);
        }

        throw new SyntaxError("Invalid command");
    }

    private Direction parseDirection()  throws SyntaxError{
        Token dir = tk.consume();

        return switch (dir.type()) {
            case  UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case UP_LEFT -> Direction.UP_LEFT;
            case UP_RIGHT -> Direction.UP_RIGHT;
            case DOWN_LEFT -> Direction.DOWN_LEFT;
            case DOWN_RIGHT -> Direction.DOWN_RIGHT;
            default -> throw new SyntaxError("Invalid direction");
        };
    }

    private Statement parseWhile() throws SyntaxError, InvalidExpressionException{
        tk.consume(TokenType.WHILE);
        tk.consume(TokenType.LPAREN);

        Expression cond = parseExpression();

        tk.consume(TokenType.RPAREN);

        Statement body = parseStatement();

        return factory.WhileStatement(cond, body);
    }

    private Statement parseIf() throws SyntaxError, InvalidExpressionException{
        tk.consume(TokenType.IF);
        tk.consume(TokenType.LPAREN);

        Expression cond = parseExpression();

        tk.consume(TokenType.RPAREN);
        tk.consume(TokenType.THEN);

        Statement thenStmt = parseStatement();

        tk.consume(TokenType.ELSE);

        Statement elseStmt = parseStatement();

        return factory.IfStatement(cond, thenStmt, elseStmt);
    }

    private Expression parseExpression() throws SyntaxError, InvalidExpressionException{
        Expression node = parseTerm();

        while (tk.peek(TokenType.PLUS) || tk.peek(TokenType.MINUS)) {
            Token token = tk.consume();
            Operator op = Operator.fromToken(token);
            Expression rhs = parseTerm();
            node = factory.BinaryExpression(node,op,rhs);
        }

        return node;
    }

    private Expression parseTerm() throws SyntaxError, InvalidExpressionException{
        Expression node = parseFactor();

        while (tk.peek(TokenType.MUL) || tk.peek(TokenType.DIV) || tk.peek(TokenType.MOD)) {
            Token token = tk.consume();
            Operator op = Operator.fromToken(token);
            Expression rhs = parseFactor();
            node = factory.BinaryExpression(node,op,rhs);
        }

        return node;

    }

    private Expression parseFactor() throws SyntaxError, InvalidExpressionException{
        Expression node = parsePower();

        while (tk.peek(TokenType.POW)) {
            Token token = tk.consume();
            Operator op = Operator.fromToken(token);
            Expression rhs = parseFactor();
            node = factory.BinaryExpression(node,op,rhs);
        }

        return node;
    }

    private Expression parsePower() throws SyntaxError, InvalidExpressionException {
        if (tk.peek(TokenType.LPAREN)) {
            tk.consume(TokenType.LPAREN);
            Expression node = parseExpression();
            tk.consume(TokenType.RPAREN);
            return node;
        }

        Token token = tk.consume();

        if (isNumber(token)) {
            return factory.NumberExpression(token);
        }

        if (token.type() == TokenType.ALLY || token.type() == TokenType.OPPONENT) {
            InfoType info = InfoType.fromToken(token);
            return factory.InfoExpression(info);
        }

        if (token.type().equals(TokenType.NEARBY)) {
            Direction dir = parseDirection();
            return factory.NearbyExpression(dir);
        }

        if (token.type() == TokenType.RANDOM) {
            return factory.VariableExpression(token);
        }

        if (isIdentifier(token)) return factory.VariableExpression(token);

        throw new InvalidExpressionException("Invalid factor");
    }

    private boolean isNumber(Token token) {
        return token.type() == TokenType.NUMBER;
    }

    private boolean isIdentifier(Token token){
        return token.type() == TokenType.IDENT;
    }

    private Statement parseBlock() throws SyntaxError, InvalidExpressionException{
        tk.consume(TokenType.LBRACE);

        List<Statement> list = new ArrayList<>();

        while (!tk.peek(TokenType.RBRACE)) {
            if (!tk.hasNextToken()) throw new SyntaxError("Missing }");
            list.add(parseStatement());
        }

        tk.consume(TokenType.RBRACE);
        return factory.BlockStatement(list);
    }
}
