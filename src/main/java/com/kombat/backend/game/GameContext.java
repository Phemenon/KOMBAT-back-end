package com.kombat.backend.game;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Interface.GameEnginePort;
import com.kombat.backend.game.Interface.StrategyContext;
import com.kombat.backend.game.Player_Minions.Minion;

/**
 * GameContext เป็นตัวกลางระหว่าง
 * Strategy ของ minion ที่ถูก Eval มากับ GameEngine
 * หน้าที่หลักของ GameContext คือ:
 *   - ให้ strategy ใช้งานได้ตามที่ spec อนุญาต
 *   - ควบคุมการเข้าถึงตัวแปร (local / global / special)
 *   - ป้องกันไม่ให้ strategy ละเมิดกติกาเกม
 *   - ส่งคำสั่งไปให้ GameEngine ดำเนินการ
 * Strategy จะไม่สามารถเข้าถึง GameEngine โดยตรงได้ ต้องผ่าน GameContext เท่านั้น
 */
public class GameContext implements StrategyContext {
    private final Minion self;
    private final GameEnginePort engine;
    private boolean done = false;

    /**
     * สร้าง context สำหรับ minion หนึ่งตัวในหนึ่งเทิร์น
     * @param self   minion ที่กำลังทำงาน
     * @param engine ระบบเกมหลัก
     */
    public GameContext(Minion self, GameEnginePort engine) {
        this.self = self;
        this.engine = engine;
    }



    /**
     * อ่านค่าตัวแปรตามกติกาของภาษา strategy
     * - ตัวแปรพิเศษ (row, col, Budget, ...)
     * - ตัวแปรขึ้นต้นตัวใหญ่ → global (ระดับผู้เล่น)
     * - ตัวแปรขึ้นต้นตัวเล็ก → local (ระดับ minion)
     * @param id ชื่อตัวแปร
     * @return ค่า long ของตัวแปร (ถ้าไม่มีจะ return ค่า 0)
     */
    @Override
    public long getVar(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id cannot be null or empty");
        switch (id) {
            case "row": return self.getRow();
            case "col": return self.getCol();
            case "Budget": return engine.getOwnerBudget(self.getOwner());
            case "Int": return engine.getOwnerInterestRate(self.getOwner());
            case "MaxBudget": return engine.getMaxBudget();
            case "SpawnsLeft": return engine.getSpawnsLeft(self.getOwner());
            case "random": return engine.random();
        }

        if (Character.isUpperCase(id.charAt(0))) return self.getOwner().getGlobal(id);
        else return self.getLocal(id);
    }

    /**
     * ตั้งค่าตัวแปรตามกติกาของภาษา strategy
     * - ตัวแปรพิเศษเป็น read-only (no-operator)
     * - ตัวแปรขึ้นต้นตัวใหญ่ → global
     * - ตัวแปรขึ้นต้นตัวเล็ก → local
     * @param id    ชื่อตัวแปร
     * @param value ค่าใหม่
     */
    @Override
    public void setVar(String id, long value) {
        if (id == null || id.isEmpty()) return;
        if (engine.isReadOnly(id)) return;

        if (Character.isUpperCase(id.charAt(0))) self.getOwner().setGlobal(id, value);
        else self.setLocal(id, value);
    }

    /**
     * @return คืนค่าข้อมูลศัตรูตามที่ GameEngine กำหนดใน spec(เช่น ระยะ / threat / จำนวน)
     */
    @Override
    public long opponent() {
        return engine.opponentInfo(self);
    }

    /**
     * @return คืนค่าข้อมูลเพื่อนร่วมทีมตามที่ GameEngine กำหนดใน spec(เช่น ระยะ / threat / จำนวน)
     */
    @Override
    public long ally() {
        return engine.allyInfo(self);
    }

    /**
     * @return คคืนค่าข้อมูลของช่องติดกับ minion ในทิศที่กำหนด
     * ค่าที่คืนขึ้นกับ spec ของเกม เช่น:
     * 0 = ไม่มีตัวในทิศนั้น
     * >0 = enemy
     * <0 = ally
     */
    @Override
    public long nearby(Direction dir) {
        return engine.nearbyInfo(self, dir);
    }

    /**
     * คำสั่ง move ในภาษา strategy
     * @param dir ทิศทางที่ต้องการเคลื่อนที่
     * @implNote
     * - ถ้า done แล้ว จะไม่ทำอะไร
     * - การตรวจ Budget และขอบเขต ใน GameEngine อีกที
     * - ถ้า move ไม่สำเร็จจะ setDone เป็น true
     */
    @Override
    public boolean move(Direction dir) {
        if (done) return false;

        if (engine.getOwnerBudget(self.getOwner()) < 1) {
            done = true;
            return false;
        }

        return engine.move(self, dir);
    }

    /**
     * คำสั่ง shoot ในภาษา strategy
     * @param dir  ทิศทางที่ยิง
     * @param cost งบประมาณที่ใช้โจมตี
     * @implNote
     * - ค่าใช้จ่ายจริง = cost + 1 เสมอตาม Spec
     * - การคำนวณ damage ทำใน GameEngine
     */
    @Override
    public void shoot(Direction dir, long cost) {
        if (done) return;
        if (cost < 0) return;
        engine.shoot(self, dir, cost);
    }

    /**
     * คำสั่ง done ในภาษา strategy
     * ใช้จบ process ของ minion ในเทิร์นนี้ทันที
     */
    @Override
    public void done() {
        done = true;
    }

    /**
     * ตรวจสอบว่า strategy จบการทำงานแล้วรึยัง
     * @return true ถ้าจบแล้ว
     */
    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public long random() {
        return engine.random();
    }
}
