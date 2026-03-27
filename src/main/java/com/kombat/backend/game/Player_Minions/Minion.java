package com.kombat.backend.game.Player_Minions;

import com.kombat.backend.game.Engine.GameEngine;
import com.kombat.backend.game.GameContext;
import com.kombat.backend.game.Interface.LocalStore;
import com.kombat.backend.game.Interface.StrategyContext;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Strategy;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
* Responsibilities
* - เก็บข้อมูลเจ้าของ (Player)
* - เก็บชนิดของมินเนียน (TypeMinion)
* - เก็บตำแหน่งบนกระดาน
* - เก็บค่า HP และสถานะการตาย
* - เก็บ local variables สำหรับ strategy
* - เรียกใช้ strategy ในแต่ละเทิร์น
 */
public class Minion implements LocalStore {
    /**
     * -- GETTER --
     *
     */
    @Getter
    private final TypeMinion type;
    /**
     */
    @Getter
    private final Player owner;

    /**
     */
    @Getter
    private int row;
    /**
     */
    @Getter
    private int col;
    /**
     * -- GETTER --
     *  HP ของ minion ตัวนั้น
     *
     */
    @Getter
    private long hp;
    /**
     * -- GETTER --
     *  HP ของ minion ตัวนั้น
     *
     */
    @Getter
    private final long maxHp;

    private final Map<String, Long> localVars = new HashMap<>();

    /**
     * สร้างมินเนียนใหม่
     * @param owner ผู้เล่นเจ้าของมินเนียน
     * @param row ตำแหน่งแถวเริ่มต้น
     * @param col ตำแหน่งคอลัมน์เริ่มต้น
     * @param type ชนิดของมินเนียน
     */
    public Minion(Player owner, int row, int col, TypeMinion type, long maxHp) {
        if (owner == null) throw new IllegalArgumentException("Owner must not be null");
        if (type == null) throw new IllegalArgumentException("Type must not be null");
        if (maxHp <= 0) throw new IllegalArgumentException("Max HP must be positive");

        this.owner = owner;
        this.row = row;
        this.col = col;
        this.type = type;
        this.hp = maxHp; // เริ่มด้วย HP เต็ม
        this.maxHp = maxHp;
    }

    /**
     * ให้มินเนียนทำงานใน 1 เทิร์น
     * @param engine ส่วนของการควบคุมเกม
     * @implNote ถ้ามินเนียนตาย จะไม่ execute strategy
     */
    public void takeTurn(GameEngine engine) {
        if (isDead()) return;
        StrategyContext ctx = new GameContext(this, engine);
        type.strategy().execute(ctx);
    }

    /** @return กลยุทธ์ของ minion */
    public Strategy getStrategy() {
        return type.getStrategy();
    }

    /**
     * เปลี่ยนตำแหน่งของ minion
     * @implNote  row และ col จะถูกเปลี่ยน
     */
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * อ่านค่า local variable
     * @param id ชื่อตัวแปร
     * @return ค่า ถ้าไม่มีจะคืนค่า 0
     */
    @Override
    public long getLocal(String id) {
        return localVars.getOrDefault(id, 0L);
    }

    /**
     * ตั้งค่า local variable
     * @param id    ชื่อตัวแปร
     * @param value ค่าใหม่
     * @implNote  ค่าใน localVars ถูกเปลี่ยน
     */
    @Override
    public void setLocal(String id, long value) {
        localVars.put(id, value);
    }

    /**
     * @return map ของ local variables ทั้งหมด (อ่านอย่างเดียว)
     */
    public Map<String, Long> getAllLocals() {
        return Collections.unmodifiableMap(localVars);
    }

    public long getDefenseFactor() {
        return type.defenseFactor();
    }

    /**
     * ทำความเสียหายให้มินเนียน
     * @param amount จำนวนความเสียหาย
     * @return true ถ้า minion ตาย
     * @implNote HP จะลดไม่ต่ำกว่า 0
     */
    public boolean damage(long amount) {
        if (amount <= 0) return false;
        hp -= amount;
        clampHp();
        return this.isDead();
    }

    /**
     * จำกัดค่า HP ให้อยู่ในช่วง [0, maxHp]
     */
    private void clampHp() {
        if (hp < 0) hp = 0;
        if (hp > getMaxHp()) hp = getMaxHp();
    }

    /**
     * @return true ถ้า HP <= 0
     */
    public boolean isDead() {
        return hp <= 0;
    }

    @Override
    public String toString() {
        return "Minion{" +
                "owner=" + owner.getName() +
                ", row=" + row +
                ", col=" + col +
                ", hp=" + hp +
                ", type=" + type.getTypeName() +
                '}';
    }
}
