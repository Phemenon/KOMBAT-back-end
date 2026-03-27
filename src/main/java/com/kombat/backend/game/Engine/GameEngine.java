package com.kombat.backend.game.Engine;

import com.kombat.backend.config.GameConfig;
import com.kombat.backend.game.Engine.GameState.Board_Direction.Board;
import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Engine.GameState.Fight.Combat;
import com.kombat.backend.game.Engine.GameState.Fight.CombatResult;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameMode;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameState;
import com.kombat.backend.game.Engine.Info_Engine.GameEvent.GameEvent;
import com.kombat.backend.game.Engine.Info_Engine.GameEvent.GameEventFactory;
import com.kombat.backend.game.Engine.Info_Engine.GameRandom;
import com.kombat.backend.game.Engine.Info_Engine.GameResult;
import com.kombat.backend.game.Exception.InvalidExpressionException;
import com.kombat.backend.game.Interface.GameEnginePort;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Strategy;
import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;
import com.kombat.backend.game.Player_Minions.TypeMinion;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


/**
 * Core Game Engine ของระบบเกม
 * ทำหน้าที่:
 *  - ควบคุมลำดับการทำงานของแต่ละเทิร์น
 *  - ประสานงานระหว่าง GameState, Board, Player และ Combat
 *  - จัดการ economy (budget, interest)
 *  - จัดการ Bot decision
 *  - ตรวจสอบเงื่อนไขจบเกม
 *  - บันทึก GameEvent เพื่อส่งออกไปยัง layer ภายนอก
 */
public class GameEngine implements GameEnginePort {
    /**
     * -- GETTER --
     *  คืนค่า GameState ปัจจุบัน
     *
     *
     *
     */
    @Getter
    private final GameState state;
    @Getter
    private final GameConfig config;
    private final Combat combat;
    private final GameRandom random;
    /**
     * -- GETTER --
     *  คืนผลลัพธ์ปัจจุบันของเกม
     *
     */
    @Setter
    @Getter
    private GameResult result = GameResult.ONGOING;
    private final Random rand = new Random();
    private static final GameEventFactory Event = GameEventFactory.instance();
    private final Queue<GameEvent> ActionLog  = new LinkedList<>();


    /**
     * สร้าง GameEngine พร้อม GameState และ GameConfig
     *
     * @param state  สถานะปัจจุบันของเกม (ต้องไม่เป็น null)
     * @param config ค่ากำหนดของเกม เช่น งบประมาณ ค่าใช้จ่าย เทิร์นสูงสุด (ต้องไม่เป็น null)
     *
     * @precondition
     *  - state != null
     *  - config != null
     *
     * @postcondition
     *  - สร้าง GameEngine ที่พร้อมทำงาน
     *  - result เริ่มต้นเป็น GameResult.ONGOING
     *
     * @sideEffect
     *  - ไม่มีการเปลี่ยนแปลง state ภายนอก
     * <p>
     * Invariant:
     *  - state และ config จะไม่เป็น null ตลอดอายุของ Engine
     *  - result จะเปลี่ยนจาก ONGOING ได้เพียงครั้งเดียวต่อเกม
     *  - ActionLog ใช้เป็น temporary event buffer และจะถูก flush ทุกครั้งที่ public API คืนค่า
     */
    public GameEngine(GameState state,
                      GameConfig config) {
        this.state = state;
        this.config = config;
        this.combat = new Combat(state);
        this.random = new GameRandom(1000);
    }

    /**
     * โหลดและ compile Strategy ของ Minion จากไฟล์
     *
     * @param file ชื่อไฟล์หรือ path ของ strategy (ต้องถูกต้องและ parse ได้)
     * @return Strategy ที่พร้อมใช้งาน
     *
     * @sideEffect
     *  - หาก syntax ผิด จะ throw RuntimeException
     *
     * @throws RuntimeException หากไฟล์มี syntax ผิดพลาด
     */
    public Strategy loadMinionStrategy(String file) {
        try {
            return StrategyEvaluator.compile(file);
        } catch (InvalidExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * เริ่มต้นเกมอย่างเป็นทางการ
     * @return รายการ GameEvent ที่มีเพียง GAME_START
     *
     * @postcondition
     *  - ไม่เปลี่ยน GameState
     *  - ไม่เปลี่ยน GameResult
     *  - คืน event เพื่อให้ layer ภายนอกเริ่ม rendering เกม
     *
     * @note
     *  - Caller ต้อง consume event ทันที
     */
    public List<GameEvent> startGame() {
        ActionLog.add(Event.gameStart());
        return flushActionLog();
    }



    /**
     * ประมวลผล 1 เทิร์นของผู้เล่นปัจจุบัน และคืนรายการ GameEvent
     * <p>
     * ลำดับขั้นตอน:
     *  1. ล้าง ActionLog
     *  2. ตรวจสอบว่าเกมจบหรือยัง หากเกมจบแล้ว จะไม่ประมวลผลและคืน log ว่าง
     *  3. รีเซ็ตสถานะผู้เล่น
     *  4. เพิ่มงบประมาณประจำเทิร์น
     *  5. คำนวณดอกเบี้ย
     *  6. จำกัด budget ไม่ให้เกิน maxBudget
     *  7. ให้ Bot ตัดสินใจ (ถ้าเป็นโหมดที่มี Bot)
     *  8. ให้ Minion ทุกตัวทำงาน
     *  9. สลับผู้เล่น
     * 10. ตรวจสอบเงื่อนไขจบเกม
     *
     * @return รายการ GameEvent ที่เกิดขึ้นในเทิร์นนี้
     *
     * @sideEffect
     *  - เปลี่ยน GameState
     *  - เปลี่ยน Budget
     *  - เพิ่ม/ลบ/เคลื่อนย้าย Minion
     *  - อัปเดต GameResult
     */
    public List<GameEvent> runTurn() {

        ActionLog.clear();

        if (result != GameResult.ONGOING) return flushActionLog();
        Player current = state.getCurrentPlayer();
        ActionLog.add(Event.turnStart(current));
        current.resetTurnFlags();
        current.addBudget(config.getTurnBudget());

        applyInterest(current);

        current.clampBudget(config.getMaxBudget());

        if (isBotTurn()) handleBotDecision(current);

        executeMinions(current);
        ActionLog.add(Event.turnEnd(current));

        state.endTurn();

        if (isGameOver()) {
            result = determineWinner();
            ActionLog.add(Event.gameEnd());
        }

        return flushActionLog();

    }

    /**
     * ให้ Bot ตัดสินใจซื้อ Hex และ Spawn แบบสุ่ม
     * กลไก:
     *  - ซื้อ Hex ตาม probability (มี memory จากเทิร์นก่อน)
     *  - Spawn แบบสุ่ม (80% chance)
     *  - Spawn สูงสุด 1 ครั้งต่อเทิร์น
     *
     * @sideEffect
     *  - เปลี่ยน Budget
     *  - อาจเปลี่ยน Board และ Player state
     */
    private void handleBotDecision(Player player) {

        Board board = state.getBoard();

        // ==========================
        // 1) ตรวจว่าทำอะไรได้บ้าง
        // ==========================

        boolean canBuy = player.getBudget() >= config.getHexPurchaseCost();

        boolean canSpawn = !player.hasNoSpawnLeft()
                && player.getBudget() >= config.getSpawnCost()
                && !player.getAllowedTypes().isEmpty();

        if (!canBuy && !canSpawn) return;


        // ==========================
        // 🎲 2) ตัดสินใจซื้อ Hex (มี Memory)
        // ==========================

        double buyChance = player.boughtHexLastTurn() ? 0.3 : 0.7;
        boolean boughtHexThisTurn = false;

        if (canBuy && rand.nextDouble() < buyChance) {

            List<int[]> candidates = findHexCandidates(player, board);

            if (!candidates.isEmpty()) {
                int[] choice = candidates.get(rand.nextInt(candidates.size()));

                if (buyHex(player, choice[0], choice[1])) {
                    boughtHexThisTurn = true;
                }
            }
        }

        // อัปเดต memory หลัง phase ซื้อ
        player.setBoughtHexLastTurn(boughtHexThisTurn);


        // ==========================
        // 🎲 3) ตัดสินใจ Spawn (สุ่ม 80%)
        // ==========================

        // คำนวณใหม่ เพราะ budget อาจลดหลังซื้อ Hex
        canSpawn = !player.hasNoSpawnLeft()
                && player.getBudget() >= config.getSpawnCost()
                && !player.getAllowedTypes().isEmpty();

        if (!canSpawn || rand.nextDouble() >= 0.8) return;

        // หา candidate ช่อง spawn ทั้งหมดก่อน
        List<int[]> spawnCandidates = new ArrayList<>();

        for (int r = 0; r < state.getBoard().getRows(); r++) {
            for (int c = 0; c < state.getBoard().getRows(); c++) {

                if (!board.canSpawn(player, r, c)) continue;

                spawnCandidates.add(new int[]{r, c});
            }
        }

        if (spawnCandidates.isEmpty()) return;

        // เลือกตำแหน่งแบบสุ่ม
        int[] spawnPos = spawnCandidates.get(rand.nextInt(spawnCandidates.size()));

        // เลือก Type แบบสุ่ม
        List<TypeMinion> deck = player.getAllowedTypes();
        TypeMinion randomType = deck.get(rand.nextInt(deck.size()));

        // Spawn สูงสุด 1 ครั้ง
        spawnMinion(player, randomType, spawnPos[0], spawnPos[1]);
    }

    private List<int[]> findHexCandidates(Player player, Board board) {

        List<int[]> candidates = new ArrayList<>();

        for (int r = 0; r < state.getBoard().getRows(); r++) {
            for (int c = 0; c < state.getBoard().getCols(); c++) {

                if (!board.isAdjacentToSpawnable(player, r, c)) continue;

                if (board.getHex(r, c).canSpawn(player)) continue;

                candidates.add(new int[]{r, c});
            }
        }

        return candidates;
    }



    private boolean isGameOver() {
        Player p1 = state.getPlayer(0);
        Player p2 = state.getPlayer(1);

        if (p1.isDefeated()) return true;
        if (p2.isDefeated()) return true;

        return state.getPlayerTurns(0) >= config.getMaxTurns()
                && state.getPlayerTurns(1) >= config.getMaxTurns();
    }

    /**
     * ตัดสินผลแพ้ชนะตามลำดับความสำคัญ:
     * <p>
     * 1. หากผู้เล่นคนใดถูกกำจัด -> อีกฝ่ายชนะทันที
     * 2. หากครบ maxTurns:
     *    - จำนวน Minion มากกว่า ชนะ
     *    - หากเท่ากัน เปรียบเทียบ HP รวม
     *    - หากยังเท่ากัน เปรียบเทียบ Budget
     *    - หากยังเท่ากัน -> DRAW
     *
     * @return GameResult ที่ไม่เป็น ONGOING
     */
    private GameResult determineWinner() {
        Player p1 = state.getPlayer(0);
        Player p2 = state.getPlayer(1);

        if (p1.isDefeated()) return GameResult.PLAYER2_WIN;
        if (p2.isDefeated()) return GameResult.PLAYER1_WIN;

        boolean maxTurnsReached =
                state.getPlayerTurns(0) >= config.getMaxTurns() &&
                        state.getPlayerTurns(1) >= config.getMaxTurns();

        if (!maxTurnsReached) return GameResult.ONGOING;

        if (p1.countMinions() > p2.countMinions())
            return GameResult.PLAYER1_WIN;

        if (p1.countMinions() < p2.countMinions())
            return GameResult.PLAYER2_WIN;

        long hp1 = p1.sumHp();
        long hp2 = p2.sumHp();

        if (hp1 > hp2) return GameResult.PLAYER1_WIN;
        if (hp1 < hp2) return GameResult.PLAYER2_WIN;

        if (p1.getBudget() > p2.getBudget())
            return GameResult.PLAYER1_WIN;

        if (p1.getBudget() < p2.getBudget())
            return GameResult.PLAYER2_WIN;

        return GameResult.DRAW;
    }

    /**
     * ให้ Minion ทุกตัวของผู้เล่นทำงาน
     * ใช้ snapshot ของ list เพื่อป้องกัน กรณีมี Minion ตายระหว่าง loop
     */
    private void executeMinions(Player player) {
        List<Minion> snapshot = new ArrayList<>(player.getMinions());

        for (Minion m : snapshot) {
            if (isGameOver()) return;
            if (m.isDead()) continue;
            m.takeTurn(this);
        }
    }

    /**
     * Spawn Minion ใหม่บนกระดาน
     *
     * @param player ผู้เล่นเจ้าของ Minion
     * @param kind   ประเภทของ Minion
     * @param row    แถวที่ต้องการวาง
     * @param col    คอลัมน์ที่ต้องการวาง
     *
     * @return true หาก Spawn สำเร็จ, false หากไม่ผ่านเงื่อนไข
     *
     * @precondition
     *  - player ต้องเป็นผู้เล่นที่อยู่ในเกม
     *  - row,col ต้องอยู่ในขอบเขตกระดาน
     *
     * @postcondition (if true)
     *  - หัก budget ตาม spawnCost
     *  - เพิ่ม Minion ลงใน Board และ Player
     *  - ลดจำนวน spawn ที่เหลือ
     *
     * @sideEffect
     *  - เปลี่ยน budget ผู้เล่น
     *  - เปลี่ยน GameState
     *  - เพิ่ม Minion ใหม่เข้าสู่ระบบ
     *
     * @note
     *  - เทิร์นแรกของผู้เล่นจะ spawn ฟรี (ไม่เสีย budget)
     */
    public boolean spawnMinion(Player player, TypeMinion kind,
                               int row, int col) {
        if (player.hasSpawnedThisTurn()) return false;
        if (player.cannotUseType(kind)) return false;
        if (player.hasNoSpawnLeft()){
            ActionLog.add(Event.spawning_limit_failed(player,row,col));
            return false;
        }

        if (!state.getBoard().canSpawn(player,row,col)){
            ActionLog.add(Event.spawning_occupied_failed(player,row,col));
            return false;
        }

        // หักเงิน
        int index = state.indexOf(player);
        boolean isFirstTurn = state.getPlayerTurns(index) == 1;

        // ถ้าไม่ใช่เทิร์นแรก ต้องมีเงินพอ
        if (!isFirstTurn && player.getBudget() < config.getSpawnCost()){
            ActionLog.add(Event.spawnFailedNoMoney(player,row,col));
            return false;
        }

        // หักเงินเฉพาะถ้าไม่ใช่เทิร์นแรก
        if (!isFirstTurn) player.spendBudget(config.getSpawnCost());

        // สร้าง minion
        Minion m = new Minion(player, row, col,kind,config.getInitHp());

        // เพิ่มลง state
        state.getBoard().placeMinion(m,row,col);
        player.addMinion(m);
        player.registerSpawn();
        player.registerSpawnThisTurn();
        player.setHasEverSpawned(true);
        ActionLog.add(Event.spawningSuccess(m,row,col));

        return true;
    }

    private void applyInterest(Player player) {
        double r = calculateInterestRate(player);
        long interest = Math.round(player.getBudget() * r / 100.0);

        if (interest <= 0) return;

        player.addBudget(interest);

        ActionLog.add(Event.interestGained(player, interest));
    }

    public long getOwnerBudget(Player owner) {
        return (long) owner.getBudget();
    }

    public long getOwnerInterestRate(Player owner) {
        return (long) calculateInterestRate(owner);
    }

    private double calculateInterestRate(Player player) {
        double m = player.getBudget();
        if (m < 1) return 0;

        int index = state.indexOf(player);
        int t = state.getPlayerTurns(index) + 1;

        double b = config.getBaseInterestPct();

        return b * Math.log10(m) * Math.log(t);
    }



    public long getMaxBudget() {
        return config.getMaxBudget();
    }

    public long getSpawnsLeft(Player owner) {
        return owner.getSpawnsLeft();
    }

    public long random() {
        return random.nextRandom();
    }

    private static final Set<String> READ_ONLY = Set.of(
            "row","col","Budget","Int",
            "MaxBudget","SpawnsLeft","random"
    );

    public boolean isReadOnly(String id) {
        return READ_ONLY.contains(id);
    }

    public long opponentInfo(Minion self) {
        return state.getBoard().findClosestOpponent(self);
    }

    public long allyInfo(Minion self) {
        return state.getBoard().findClosestAlly(self);
    }

    public long nearbyInfo(Minion self, Direction dir) {
        Optional<Board.ScanResult> result = state.getBoard().scanDirection(self, dir);

        if (result.isEmpty()) return 0;

        Minion target = result.get().minion();
        int z = result.get().distance();

        int x = digitCount(target.getHp());
        int y = digitCount(target.getDefenseFactor());

        long value = 100L * x + 10L * y + z;

        if (target.getOwner() == self.getOwner()) {
            return -value;
        }

        return value;
    }

    private int digitCount(long n) {
        if (n == 0) return 1;
        return (int) Math.log10(Math.abs(n)) + 1;
    }

    /**
     * เคลื่อนที่ Minion ไปในทิศทางที่กำหนด
     *
     * @param self Minion ที่ต้องการเคลื่อนที่
     * @param dir  ทิศทาง
     *
     * @return true หากเคลื่อนที่สำเร็จ, false หากไม่สำเร็จ
     *
     * @precondition
     *  - self ต้องยังไม่ตาย
     *  - owner ต้องมี budget >= 1
     *
     * @postcondition (if true)
     *  - เปลี่ยนตำแหน่ง Minion บนกระดาน
     *
     * @sideEffect
     *  - หัก budget 1 หน่วย (ไม่คืนแม้เคลื่อนไม่สำเร็จ)
     *  - เปลี่ยนตำแหน่งบน Board (ถ้าสำเร็จ)
     *  - เพิ่ม GameEvent ลง ActionLog
     *
     * @invariant
     *  - Budget จะลดลงเสมอหากมีการเรียก method นี้และ owner มี budget >= 1
     *    แม้การเคลื่อนที่ล้มเหลว
     */
    public boolean move(Minion self, Direction dir) {
        Player owner = self.getOwner();

        if (owner.getBudget() < 1) {
            return false;
        }
        int beforeRow = self.getRow();
        int beforeCol = self.getCol();

        owner.spendBudget(1);
        boolean isMoved = state.getBoard().move(self, dir);
        if (isMoved) {
            int afterRow = self.getRow();
            int afterCol = self.getCol();
            ActionLog.add(Event.moving(self, beforeRow, beforeCol, afterRow, afterCol));
        }
        else ActionLog.add(Event.stuck(self, beforeRow, beforeCol));

        return isMoved;
    }

    /**
     * ยิงโจมตีไปในทิศทางที่กำหนด
     * <p>
     * ค่าใช้จ่ายรวม = cost + 1
     * หาก budget ไม่พอ จะไม่เกิดผลใด ๆ
     * <p>
     * ความเสียหายคำนวณตามสูตร:
     * damage = max(1, cost - defenseFactor)
     * <p>
     * หาก HP ของเป้าหมาย <= 0 จะถูกลบออกจากเกมทันที
     *
     * @param self Minion ผู้ยิง
     * @param dir  ทิศทางที่ยิง
     * @param cost ค่า expenditure ของการยิง (ต้อง >= 0)
     * @return CombatResult ผลลัพธ์ของการต่อสู้
     * @sideEffect - เปลี่ยน Budget ของผู้ยิง
     * - เปลี่ยน HP ของเป้าหมาย
     * - อาจลบ Minion ออกจาก GameState
     * - เพิ่ม GameEvent ลง ActionLog
     *
     * @note
     *  - Logic การคำนวณความเสียหายและการลบ Minion ถูกจัดการใน Combat class
     *  - Engine มีหน้าที่เพียงบันทึก GameEvent
     */
    public CombatResult shoot(Minion self, Direction dir, long cost) {

        CombatResult result = combat.shoot(self, dir, cost);

        switch (result.outcome()) {
            case NO_BUDGET -> ActionLog.add(Event.shootNoBudget(self));

            case MISS -> ActionLog.add(Event.shootMiss(self, dir));

            case HIT -> ActionLog.add(Event.shootHit(self, dir, result.damage()));

            case KILL -> {
                ActionLog.add(Event.shootKill(self, dir, result.damage()));
                Minion dead = result.target();
                ActionLog.add(Event.die(dead));
            }

        }

        return result;
    }

    /**
     * ซื้อ Hex เพื่อเปิดพื้นที่ Spawn
     *
     * @param player ผู้เล่นที่ทำการซื้อ
     * @param row    แถว
     * @param col    คอลัมน์
     *
     * @return true หากซื้อสำเร็จ
     *
     * @precondition
     *  - player ต้องเป็น CurrentPlayer
     *  - budget ต้องเพียงพอ
     *  - ตำแหน่งต้องอยู่ติดกับพื้นที่ที่ spawn ได้
     *
     * @postcondition (if true)
     *  - Hex ที่ตำแหน่ง (row,col) จะเปิดสิทธิ์ให้ player spawn ได้ในอนาคต
     *  - Budget ลดลงตาม hexPurchaseCost
     *
     * @sideEffect
     *  - อาจเปิดสิทธิ์ Spawn ที่ตำแหน่งนั้นบน Board
     *  - เปลี่ยน Budget
     *  - บันทึกสถานะว่าผู้เล่นซื้อ Hex ในเทิร์นนี้แล้ว
     */
    public boolean buyHex(Player player, int row, int col) {

        if (player != state.getCurrentPlayer()) return false;
        if (player.hasBoughtHexThisTurn()) return false;


        long cost = config.getHexPurchaseCost();
        if (player.getBudget() < cost){
            ActionLog.add(Event.buyingHex(player,row,col,false));
            return false;
        }

        if (!state.getBoard().isAdjacentToSpawnable(player, row, col)) return false;

        state.getBoard().getHex(row, col).MakeEnable(player);

        player.spendBudget(cost);

        player.registerBuyHexThisTurn();
        ActionLog.add(Event.buyingHex(player,row,col,true));

        return true;
    }

    private boolean isBotTurn() {
        GameMode mode = state.getMode();
        Player current = state.getCurrentPlayer();

        if (mode == GameMode.DUEL) {
            return false;
        }

        if (mode == GameMode.SOLITAIRE) {
            // สมมุติ player 2 เป็น bot
            return state.indexOf(current) == 1;
        }

        return mode == GameMode.AUTO;
    }

    /**
     * คืนรายการ GameEvent ทั้งหมดที่เกิดขึ้น
     * และล้าง ActionLog ภายใน Engine
     *
     * @return list ของ GameEvent ในลำดับที่เกิดขึ้น
     *
     * @sideEffect
     *  - ล้าง ActionLog ภายใน
     */
    public List<GameEvent> flushActionLog() {
        List<GameEvent> events = new ArrayList<>(ActionLog);
        ActionLog.clear();
        return events;
    }

}
