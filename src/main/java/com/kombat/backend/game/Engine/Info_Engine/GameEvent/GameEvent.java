package com.kombat.backend.game.Engine.Info_Engine.GameEvent;

import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;

/**
 * Immutable Event Object ที่ใช้ represent การกระทำหนึ่งครั้งของ Minion
 * ภายใน 1 turn ของ GameEngine
 * <p>
 * GameEvent read-only
 * ใช้สำหรับ:
 * - ส่งข้อมูลไปยัง Frontend
 * - Replay ระบบเกมย้อนหลัง
 * - Logging / Debugging
 *
 * @param value ใช้กับ damage / interest / budget change
 */
public record GameEvent(
        Action action,
        int playerId,
        String playerName,
        String minionType,
        Integer fromRow,
        Integer fromCol,
        Integer toRow,
        Integer toCol,
        Long value
)  {

    public static GameEvent create(Action action,
                                   Player player,
                                   Minion minion,
                                   Integer fromRow,
                                   Integer fromCol,
                                   Integer toRow,
                                   Integer toCol,
                                   Long value) {

        return new GameEvent(
                action,
                player != null ? player.getPlayerId() : -1,
                player != null ? player.getName() : "System",
                minion != null ? minion.getType().getTypeName() : "N/A",
                fromRow,
                fromCol,
                toRow,
                toCol,
                value
        );
    }

    public String describe() {

        return switch (action) {

            case GAME_START ->
                    "Game started.";

            case GAME_END ->
                    "Game ended.";

            case TURN_START ->
                    "Player " + playerName + "'s turn begins.";

            case TURN_END ->
                    "Player " + playerName + "'s turn ends.";

            case BUY_HEX_SUCCESS ->
                    "Player " + playerName +
                            " successfully bought hex at (" +
                            fromRow + "," + fromCol + ").";

            case BUY_HEX_FAILED ->
                    "Player " + playerName +
                            " failed to buy hex at (" +
                            fromRow + "," + fromCol + ").";

            case SPAWN_SUCCESS ->
                    "Player " + playerName +
                            " spawned " + minionType +
                            " at (" + fromRow + "," + fromCol + ").";

            case SPAWN_FAILED_NO_MONEY ->
                    "Player " + playerName +
                            " failed to spawn at (" +
                            fromRow + "," + fromCol +
                            ") due to insufficient budget.";

            case SPAWN_FAILED_HEX_OCCUPIED ->
                    "Player " + playerName +
                            " failed to spawn at (" +
                            fromRow + "," + fromCol +
                            ") because the hex is occupied.";

            case SPAWN_FAILED_LIMIT_REACHED ->
                    "Player " + playerName +
                            " cannot spawn more units (limit reached).";

            case INTEREST_GAINED ->
                    "Player " + playerName +
                            " gained interest: +" + value + " budget.";

            case MOVE ->
                    "Player " + playerName +
                            "'s " + minionType +
                            " moved from (" +
                            fromRow + "," + fromCol +
                            ") to (" +
                            toRow + "," + toCol + ").";

            case STUCK ->
                    "Player " + playerName +
                            "'s " + minionType +
                            " is stuck at (" +
                            fromRow + "," + fromCol + ").";

            case SHOOT_NO_BUDGET ->
                    "Player " + playerName +
                            "'s " + minionType +
                            " tried to shoot but has no budget.";

            case SHOOT_HIT ->
                    "Player " + playerName +
                            "'s " + minionType +
                            " shot at (" +
                            toRow + "," + toCol +
                            ") dealing " + value + " damage.";

            case SHOOT_KILL ->
                    "Player " + playerName +
                            "'s " + minionType +
                            " shot at (" +
                            toRow + "," + toCol +
                            ") and killed the target dealing " +
                            value + " damage.";

            case SHOOT_MISS ->
                    "Player " + playerName +
                            "'s " + minionType +
                            " shot at (" +
                            toRow + "," + toCol +
                            ") but missed.";

            case DIE ->"Player " + playerName +
                    "'s " + minionType +
                    " has fallen at (" +
                    fromRow + "," + fromCol + ").";
        };
    }
}
