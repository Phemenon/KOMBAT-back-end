package com.kombat.backend.game.Parser_Eval.Parser;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Interface.Expression;
import com.kombat.backend.game.Interface.Statement;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Expression.*;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Statement.*;
import com.kombat.backend.game.Token_Tokenizer.Token;

import java.util.List;

public class ASTNodeFactory {
    private static ASTNodeFactory instance;
    private ASTNodeFactory() {}

    public static ASTNodeFactory instance() {
        if (instance == null) {
            instance = new ASTNodeFactory();
        }
        return instance;
    }

    public Expression NumberExpression(Token value) {
        return new NumberExpression(value.number());
    }
    public Expression VariableExpression(Token variable) {
        return new VariableExpression(variable.therefromFile());
    }
    public Expression BinaryExpression(Expression left, Operator op, Expression right) {
        return new BinaryExpression(left,op,right);
    }
    public Expression NearbyExpression(Direction direction) {
        return new NearbyExpression(direction);
    }
    public Expression InfoExpression(InfoType type) {
        return new InfoExpression(type);
    }
    public Statement AssignStatement(Token name, Expression expr) {
        return new AssignStatement(name, expr);
    }
    public Statement BlockStatement(List<Statement> ListStatements) {
        return new BlockStatement(ListStatements);
    }
    public Statement IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
        return new IfStatement(condition, thenBranch, elseBranch);
    }
    public Statement WhileStatement(Expression condition, Statement body){
        return new WhileStatement(condition, body);
    }
    public Statement DoneCommand() {
        return new DoneCommand();
    }
    public Statement MoveCommand(Direction dir) {
        return new MoveCommand(dir);
    }
    public Statement ShootCommand(Direction dir, Expression cost){
        return new ShootCommand(dir,cost);
    }
}
