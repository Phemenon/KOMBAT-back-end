package com.kombat.backend.game.Engine.Info_Engine.GameEvent;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;

public class GameEventFactory {
    private static GameEventFactory instance;
    private GameEventFactory() {}

    public static GameEventFactory instance() {
        if (instance == null) {
            instance = new GameEventFactory();
        }
        return instance;
    }

    public GameEvent gameStart(){
        return GameEvent.create(Action.GAME_START,null,null,
                null,null,null,null,null);
    }

    public GameEvent gameEnd(){
        return GameEvent.create(Action.GAME_END,null,null,
                null,null,null,null,null);
    }

    public GameEvent turnStart(Player currentPlayer){
        return GameEvent.create(Action.TURN_START,currentPlayer,null,
                null,null,null,null,null);
    }

    public GameEvent turnEnd(Player currentPlayer){
        return GameEvent.create(Action.TURN_END,currentPlayer,null,
                null,null,null,null,null);
    }

    public GameEvent buyingHex(Player currentPlayer, int AtRow, int AtCol,boolean success){
        if(success)
        return GameEvent.create(Action.BUY_HEX_SUCCESS,currentPlayer,null,
                AtRow,AtCol,null,null,null);

        else return GameEvent.create(Action.BUY_HEX_FAILED,currentPlayer,null,
                AtRow,AtCol,null,null,null);
    }

    public GameEvent spawningSuccess(Minion minion, int AtRow, int AtCol){
            return GameEvent.create(Action.SPAWN_SUCCESS,minion.getOwner(),minion,
                    AtRow,AtCol,null,null,null);
    }

    public GameEvent spawnFailedNoMoney(Player player, int AtRow, int AtCol){
        return GameEvent.create(Action.SPAWN_FAILED_NO_MONEY,player,null,
                AtRow,AtCol,null,null,null);
    }

    public GameEvent spawning_occupied_failed(Player player, int AtRow, int AtCol){
        return GameEvent.create(Action.SPAWN_FAILED_HEX_OCCUPIED,player,null,
                AtRow,AtCol,null,null,null);
    }

    public GameEvent spawning_limit_failed(Player player, int AtRow, int AtCol){
        return GameEvent.create(Action.SPAWN_FAILED_LIMIT_REACHED,player,null,
                AtRow,AtCol,null,null,null);
    }

    public GameEvent interestGained(Player current ,long budget){
        return GameEvent.create(Action.INTEREST_GAINED,current,null,
                null,null,null,null,budget);
    }

    public GameEvent moving(Minion minion,
                            int atRow, int atCol,
                            int toRow, int toCol) {

        return GameEvent.create(Action.MOVE, minion.getOwner(), minion,
                atRow, atCol, toRow, toCol, null
        );
    }

    public GameEvent stuck(Minion minion, int AtRow, int AtCol) {
        return GameEvent.create(Action.STUCK, minion.getOwner(), minion,
                AtRow, AtCol, AtRow, AtCol, null
        );
    }

    public GameEvent shootNoBudget(Minion self) {
        return GameEvent.create(
                Action.SHOOT_NO_BUDGET,
                self.getOwner(), self, self.getRow(),
                self.getCol(), null, null, null
        );
    }

    public GameEvent shootHit(Minion minion, Direction direction, long damage) {

        int atRow = minion.getRow();
        int atCol = minion.getCol();
        int[] target = resolveTarget(minion, direction);

        return GameEvent.create(
                Action.SHOOT_HIT,
                minion.getOwner(), minion,
                atRow, atCol, target[0], target[1],
                damage
        );
    }

    public GameEvent shootKill(Minion minion, Direction direction, long damage) {

        int atRow = minion.getRow();
        int atCol = minion.getCol();
        int[] target = resolveTarget(minion, direction);

        return GameEvent.create(
                Action.SHOOT_KILL,
                minion.getOwner(), minion,
                atRow, atCol, target[0], target[1],
                damage
        );
    }

    public GameEvent shootMiss(Minion minion, Direction direction) {

        int atRow = minion.getRow();
        int atCol = minion.getCol();
        int[] target = resolveTarget(minion, direction);

        return GameEvent.create(
                Action.SHOOT_MISS,
                minion.getOwner(), minion,
                atRow, atCol, target[0], target[1],
                null
        );
    }

    private int[] resolveTarget(Minion minion, Direction direction) {
        int atRow = minion.getRow();
        int atCol = minion.getCol();
        int[] delta = direction.delta(atCol);

        return new int[] {
                atRow + delta[0],
                atCol + delta[1]
        };
    }

    public GameEvent die(Minion dead) {
        return GameEvent.create(
                Action.DIE,
                dead.getOwner(),
                dead,
                dead.getRow(),
                dead.getCol(),
                null,
                null,
                null
        );
    }
}
