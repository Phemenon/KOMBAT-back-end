package com.kombat.backend.game.Engine.GameState.Mode_State;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Board;
import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;
import lombok.Getter;

/**
 * คลาส GameState ทำหน้าที่เก็บ "สถานะปัจจุบันของเกม"
 * โดยไม่มีตรรกะของกติกาเกมอยู่
 * หน้าที่ของคลาสนี้คือเก็บข้อมูลที่เปลี่ยนแปลงระหว่างการเล่น เช่น
 * - Board
 * - ผู้เล่นทั้งสองฝ่าย
 * - ผู้เล่นที่กำลังเล่นอยู่
 * - จำนวนเทิร์นที่ผ่านไปแล้ว
 * <p>
 * การตัดสินกติกา เช่น เกมจบหรือไม่, ใช้งบได้หรือไม่
 * เป็นหน้าที่ของ GameEngine ไม่ใช่ GameState
 */
public class GameState {
    /**
     * -- GETTER --
     *  คืนค่ากระดานของเกม
     *
     *
     *
     */
    @Getter
    private final Board board;
    private final Player[] players;

    private int currentPlayer; // 0 or 1
    @Getter
    private final GameMode mode;

    private final int[] playerTurns; // จำนวนเทิร์นที่แต่ละผู้เล่นเล่นไปแล้ว


    /**
     * สร้างสถานะเริ่มต้นของเกม
     *
     * @param board กระดานเกม (ต้องไม่เป็น null)
     * @param p1 ผู้เล่นคนที่ 1 (ต้องไม่เป็น null)
     * @param p2 ผู้เล่นคนที่ 2 (ต้องไม่เป็น null)
     * @param mode โหมดเกม (ต้องไม่เป็น null)
     *
     * @postcondition
     *  - currentPlayer เริ่มต้นที่ 0 (Player 1)
     *  - playerTurns ของทั้งสองฝ่ายเริ่มที่ 1
     *
     * @sideEffect
     *  - ไม่มีการเปลี่ยนแปลงภายนอก
     */
    public GameState(Board board, Player p1, Player p2, GameMode mode) {
        this.board = board;
        this.mode = mode;
        this.players = new Player[]{p1, p2};
        this.currentPlayer = 0;
        this.playerTurns = new int[]{1, 1};
    }

    /**
     * คืนค่าผู้เล่นที่กำลังเล่นอยู่ในเทิร์นปัจจุบัน
     * @return Player ปัจจุบัน
     */
    public Player getCurrentPlayer() {
        return players[currentPlayer];
    }


    /**
     * คืนค่าผู้เล่นฝ่ายตรงข้าม
     * @return ผู้เล่นฝ่ายตรงข้าม
     */
    public Player opponentPlayer() {
        return players[1 - currentPlayer];
    }

    /**
     * คืนค่าผู้เล่นตาม index
     * @param index 0 = ผู้เล่นคนที่ 1 หรือ 1 = ผู้เล่นคนที่ 2 เท่านั้น
     * @return ผู้เล่นตาม index
     * @throws IllegalArgumentException หาก index ไม่ใช่ 0 หรือ 1
     */
    public Player getPlayer(int index) {
        if (index != 0 && index != 1) throw new IllegalArgumentException("must be 0 or 1");
        return players[index];
    }

    /**
     * @return index ของผู้เล่นปัจจุบัน (0 หรือ 1)
     */
    public int getCurrentPlayerIndex() {
        return currentPlayer;
    }


    /**
     * จบเทิร์นของผู้เล่นปัจจุบัน
     * <p>
     * กระบวนการ:
     *  1. เพิ่มจำนวนเทิร์นของผู้เล่นปัจจุบัน
     *  2. สลับ currentPlayer
     *
     * @precondition
     *  - ต้องถูกเรียกหลังจาก GameEngine ประมวลผลเทิร์นเสร็จแล้ว
     *
     * @sideEffect
     *  - เปลี่ยนค่า currentPlayer
     *  - เปลี่ยนค่า playerTurns
     */
    public void endTurn() {
        playerTurns[currentPlayer]++;
        switchPlayer();
    }

    /**
     * คืนค่าจำนวนเทิร์นที่ผู้เล่นเล่นไปแล้ว
     *
     * @param index 0 หรือ 1 เท่านั้น
     * @return จำนวนเทิร์นของผู้เล่นนั้น
     * @throws IllegalArgumentException หาก index ไม่ถูกต้อง
     *
     */
    public int getPlayerTurns(int index) {
        validateIndex(index);
        return playerTurns[index];
    }

    private void switchPlayer() {
        currentPlayer = 1 - currentPlayer;
    }

    /**
     * ตรวจสอบว่าผู้เล่นไม่มีมินเนียนเหลืออยู่หรือไม่
     *
     * @param index 0 หรือ 1
     * @return true หากไม่มีมินเนียนเหลือ
     */
    public boolean hasNoMinions(int index) {
        validateIndex(index);
        return players[index].countMinions() == 0;
    }

    /**
     * คืนค่าจำนวนมินเนียนของผู้เล่น
     * ใช้สำหรับการตัดสินกรณีเสมอ (tie-break)
     *
     * @param index 0 หรือ 1
     * @return จำนวนมินเนียนที่ยังมีชีวิต
     *
     */
    public int getMinionCount(int index) {
        validateIndex(index);
        return players[index].countMinions();
    }

    /**
     * คืนค่าผลรวม HP ของมินเนียนทั้งหมดของผู้เล่น
     * ใช้สำหรับ tie-break
     *
     * @param index 0 หรือ 1
     * @return ผลรวม HP ของมินเนียนทั้งหมด
     */
    public long getTotalHp(int index) {
        validateIndex(index);
        return players[index].sumHp();
    }

    /**
     * คืนค่างบประมาณที่เหลือของผู้เล่น
     * @param index 0 หรือ 1
     * @return budget ปัจจุบันของผู้เล่น
     */
    public double getRemainingBudget(int index) {
        validateIndex(index);
        return players[index].getBudget();
    }

    /**
     * คืนหมายเลขรอบ (Full Round)
     *  รอบ คือ ค่าน้อยที่สุดของจำนวนเทิร์นของทั้งสองฝ่าย
     * @return หมายเลขรอบปัจจุบัน
     */
    public int getRound() {
        return Math.min(playerTurns[0], playerTurns[1]);
    }

    /**
     * ตรวจสอบว่า index ถูกต้องหรือไม่
     * @param index index ของผู้เล่น
     * @throws IllegalArgumentException หาก index ไม่ใช่ 0 หรือ 1
     */
    private void validateIndex(int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException("Invalid player index: " + index);
        }
    }

    /**
     * ลบมินเนียนออกจากเกม
     *
     * @param target มินเนียนที่ต้องการลบ (ต้องไม่เป็น null)
     * @precondition
     *  - target ต้องอยู่ในกระดานและเป็นของผู้เล่นในเกม
     * @postcondition
     *  - target ถูกลบออกจาก Board
     *  - target ถูกลบออกจาก Player
     * @sideEffect
     *  - เปลี่ยนสถานะ Board
     *  - เปลี่ยนรายการมินเนียนของ Player
     */
    public void removeMinion(Minion target) {
        board.removeMinion(target);
        target.getOwner().removeMinion(target);
    }

    /**
     * คืนค่า index ของผู้เล่น
     * @param player ผู้เล่นที่ต้องการค้นหา
     * @return 0 หรือ 1
     *
     * @throws IllegalArgumentException หากไม่พบผู้เล่นใน GameState
     * @sideEffect
     *  - ไม่มี
     */
    public int indexOf(Player player) {
        if (players[0] == player) return 0;
        if (players[1] == player) return 1;
        throw new IllegalArgumentException("Player not found in GameState");
    }

    /**
     * ตรวจสอบว่า Player 1 เป็น Bot หรือไม่
     * @return true หากเป็น Bot ตาม GameMode
     */
    public boolean isPlayer1Bot() {
        return mode == GameMode.AUTO;
    }

    /**
     * ตรวจสอบว่า Player 2 เป็น Bot หรือไม่
     * @return true หากเป็น Bot ตาม GameMode
     */
    public boolean isPlayer2Bot() {
        return mode == GameMode.AUTO || mode == GameMode.SOLITAIRE;
    }
}
