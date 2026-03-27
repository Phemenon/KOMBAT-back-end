package com.kombat.backend.game.Player_Minions;

import com.kombat.backend.game.Interface.GlobalStore;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * คลาส Player แทนผู้เล่นหนึ่งคนในเกม KOMBAT
 * <p>
 * หน้าที่ของคลาสนี้:
 * - เก็บสถานะระดับผู้เล่นที่คงอยู่ข้ามหลาย ๆ turn
 * - เก็บ global variables ที่ใช้ร่วมกันระหว่าง minion ของผู้เล่นเดียวกัน
 * - จัดการงบประมาณ (budget) ของผู้เล่น (มี method หัก budget)
 * - ติดตามจำนวนการ spawn minion ที่ใช้ไปเทียบกับ max_spawn ที่อนุญาต
 * - เก็บสถานะแพ้ (defeated) สำหรับใช้ตรวจสอบเงื่อนไขจบเกม
 * <p>
 * คลาสนี้ "ไม่รับผิดชอบ":
 * - การควบคุมลำดับ turn ของเกม
 * - การบังคับกฎ spawn ได้แค่ครั้งเดียวต่อ turn
 * - การตัดสินแพ้ชนะของเกม
 * <p>
 * กฎที่เกี่ยวข้องกับเวลา (turn-based rules) และกฎระดับเกมทั้งหมด
 * จะถูกจัดการโดย GameEngine แยกต่างหาก
 * Invariants:
 *  - budget >= 0
 *  - 0 <= spawnsUsed <= maxSpawns
 *  - aliveMinions จะไม่เก็บ minion ที่ตายแล้ว
 *  - allowedTypes ไม่มี duplicate
 */

public class Player implements GlobalStore {
    /**
     * -- GETTER --
     *  คืนรหัสผู้เล่น
     *
     */
    @Getter
    private final int playerId;
    /**
     * -- GETTER --
     *  คืนชื่อผู้เล่น
     *
     */
    @Getter
    private String name = "Guest";

    private final Map<String, Long> globals = new HashMap<>();

    /**
     * -- GETTER --
     *  คืนค่างบประมาณปัจจุบันของผู้เล่น
     */
    @Getter
    private double budget;
    private int spawnsUsed;
    private final int maxSpawns;
    private final List<Minion> aliveMinions = new ArrayList<>();
    /**
     * -- SETTER --
     *  Set hasEverSpawned
     *
     */
    @Setter
    private boolean hasEverSpawned = false;
    private List<TypeMinion> allowedTypes = new ArrayList<>();

    private boolean spawnedThisTurn = false;
    private boolean boughtHexThisTurn = false;

    @Setter
    private boolean boughtHexLastTurn;



    /**
     * สร้างผู้เล่นใหม่
     * @param playerId รหัสผู้เล่น
     * @param name ชื่อผู้เล่น
     * @param initialBudget งบประมาณเริ่มต้น (มาจาก config file)
     * @param maxSpawns จำนวน minion สูงสุดที่สามารถ spawn ได้ต่อเกม (ควรเป็น 1 per turn)
     */
    public Player(int playerId, String name, double initialBudget, int maxSpawns) {
        this.playerId = playerId;
        this.name = name;
        this.budget = initialBudget;
        this.maxSpawns = maxSpawns;
        this.spawnsUsed = 0;
    }

    public Player(int playerId, double initialBudget, int maxSpawns) {
        this.playerId = playerId;
        this.name = name + playerId;
        this.budget = initialBudget;
        this.maxSpawns = maxSpawns;
        this.spawnsUsed = 0;
    }

    /**
     * ใช้งบประมาณ หากงบไม่พอจะไม่ทำอะไร
     *
     * @param amount จำนวนงบที่ต้องการใช้
     * @return true หากใช้งบสำเร็จ, false หากงบไม่พอหรือจำนวนไม่ถูกต้อง
     * @implNote เมธอดนี้จะเปลี่ยนแปลงค่า budget ของผู้เล่นเมื่อใช้งบสำเร็จ
     */
    public boolean spendBudget(double amount) {
        if (amount < 0) return false;
        if (budget < amount) return false;
        budget -= amount;
        return true;
    }

    /**
     * เพิ่มงบประมาณให้ผู้เล่น
     * ใช้ในช่วงเริ่ม turn หรือหลังคำนวณดอกเบี้ย
     *
     * @param amount จำนวนงบที่ต้องการเพิ่ม
     * @implNote ค่า budget ของผู้เล่นจะเพิ่มขึ้น
     */
    public void addBudget(double amount) {
        if (amount > 0) budget += amount;
    }

    /**
     * จำกัด budget ไม่ให้เกินค่าที่กำหนด
     * @param maxBudget ค่าสูงสุดที่อนุญาต (maxBudget >= 0)
     *
     * @sideEffect
     *  - อาจปรับลดค่า budget
     */
    public void clampBudget(double maxBudget) {
        if (budget > maxBudget) budget = maxBudget;
    }

    /**
     * ตรวจสอบว่ายังสามารถ spawn minion เพิ่มได้หรือไม่
     *
     * @return true หากยัง spawn ได้, false หากถึงจำนวนสูงสุดแล้ว
     */
    public boolean hasNoSpawnLeft() {
        return spawnsUsed >= maxSpawns;
    }

    /**
     * คืนจำนวนครั้งที่สามารถ spawn ได้อีก
     * @return จำนวน spawn ที่เหลือ
     */
    public int getSpawnsLeft() {
        return maxSpawns - spawnsUsed;
    }

    /**
     * บันทึกว่ามีการ spawn minion สำเร็จแล้ว 1 ครั้ง
     * @implNote หัก spawnsUsed ของผู้เล่น
     * @throws IllegalStateException หาก spawn เกินจำนวนที่อนุญาต
     */
    public void registerSpawn() {
        if (hasNoSpawnLeft()) {
            throw new IllegalStateException("No spawns left");
        }
        spawnsUsed++;
    }

    /**
     * เพิ่ม minion ใหม่เข้า list
     * เรียกเมื่อ spawn สำเร็จ
     *
     * @param minion minion ที่ถูก spawn
     */
    public void addMinion(Minion minion) {
        aliveMinions.add(minion);
    }

    /**
     * ลบ minion ออกจาก list
     * เรียกเมื่อ minion ตาย
     *
     * @param minion minion ที่ตาย
     */
    public void removeMinion(Minion minion) {
        aliveMinions.remove(minion);
    }

    /**
     * คืนจำนวน minion ที่ยังมีชีวิต
     */
    public int countMinions() {
        return aliveMinions.size();
    }

    /**
     * คืนผลรวม HP ของ minion ทั้งหมด
     */
    public long sumHp() {
        long sum = 0;
        for (Minion m : aliveMinions) {
            sum += m.getHp();
        }
        return sum;
    }

    /**
     * ผู้เล่นถือว่าแพ้เมื่อ:
     *  - เคย spawn มาแล้วอย่างน้อย 1 ครั้ง
     *  - และไม่มี minion ที่ยังมีชีวิตเหลืออยู่
     *
     * @return true หากเข้าเงื่อนไขแพ้
     */
    public boolean isDefeated() {
        return hasEverSpawned && aliveMinions.isEmpty();
    }

    /**
     * คืนค่า global variable

     * @param id ชื่อตัวแปร
     * @return ค่า global variable / หากยังไม่เคยถูกกำหนดค่า จะคืนค่า 0
     */
    @Override
    public long getGlobal(String id) {
        return globals.getOrDefault(id, 0L);
    }

    /**
     * กำหนดค่า global variable ของผู้เล่น
     * @param id ชื่อตัวแปร
     * @param value ค่าที่ต้องการกำหนด

     * @implNote ค่า global variable ชื่อนี้จะถูกสร้างใหม่หรือถูกเขียนทับ
     */
    @Override
    public void setGlobal(String id, long value) {
        globals.put(id, value);
    }

    /**
     * ตรวจสอบว่ามี global variable ชื่อนี้อยู่หรือไม่
     * @param id ชื่อตัวแปร
     * @return true หากมีตัวแปรนี้อยู่
     */
    public boolean hasGlobal(String id) {
        return globals.containsKey(id);
    }

    /**
     * ล้าง global variables ทั้งหมด
     * @implNote global variables ของผู้เล่นจะถูกลบทั้งหมด
     * *** ไม่ควรถูกเรียกระหว่างเกม
     */
    public void resetGlobals() {
        globals.clear();
    }


    /**
     * คืน list ของ minion ที่ยังมีชีวิต
     * @return reference จริงของ internal list
     * @warning
     *  - Caller ไม่สามารถแก้ไข list นี้ได้
     */
    public List<Minion> getMinions() {
        return Collections.unmodifiableList(aliveMinions);
    }

    /**
     * คืน global variables ทั้งหมดในรูปแบบ read-only
     * @return Map ของ global variables ที่ไม่สามารถแก้ไขได้
     */
    public Map<String, Long> getAllGlobals() {
        return Collections.unmodifiableMap(globals);
    }


    @Override
    public String toString() {
        return "Player{" +
                "id=" + playerId +
                ", name='" + name + '\'' +
                ", globals=" + globals +
                '}';
    }

    public void setAllowedTypes(List<TypeMinion> typeMinions) {
        if (typeMinions == null || typeMinions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Player must select at least 1 type"
            );
        }

        if (typeMinions.size() > 5) {
            throw new IllegalArgumentException(
                    "Maximum allowed types is 5"
            );
        }

        // กัน duplicate
        Set<TypeMinion> unique = new HashSet<>(typeMinions);
        if (unique.size() != typeMinions.size()) {
            throw new IllegalArgumentException(
                    "Duplicate types are not allowed"
            );
        }

        this.allowedTypes = new ArrayList<>(typeMinions);
    }

    /**
     * ตรวจสอบว่า type นี้ไม่ได้อยู่ใน allowedTypes
     * @return true หากผู้เล่น "ไม่สามารถ" ใช้ type นี้ได้
     */
    public boolean cannotUseType(TypeMinion type) {
        return !allowedTypes.contains(type);
    }

    public List<TypeMinion> getAllowedTypes() {
        return Collections.unmodifiableList(allowedTypes);
    }


    /**
     * รีเซ็ต flag ที่ใช้ควบคุมข้อจำกัดต่อ turn
     * @implNote
     *  ควรถูกเรียกโดย GameEngine เมื่อเริ่ม turn ใหม่
     *
     * @sideEffect
     *  - spawnedThisTurn = false
     *  - boughtHexThisTurn = false
     */
    public void resetTurnFlags() {
        spawnedThisTurn = false;
        boughtHexThisTurn = false;
    }

    public boolean hasSpawnedThisTurn() {
        return spawnedThisTurn;
    }

    public boolean hasBoughtHexThisTurn() {
        return boughtHexThisTurn;
    }

    /**
     * บันทึกว่าผู้เล่น spawn แล้วใน turn นี้
     * @throws IllegalStateException
     *  หากเรียกซ้ำใน turn เดียวกัน
     *
     * @sideEffect
     *  - เปลี่ยน spawnedThisTurn = true
     */
    public void registerSpawnThisTurn() {
        if (spawnedThisTurn)
            throw new IllegalStateException("Already spawned this turn");
        spawnedThisTurn = true;
    }

    /**
     * บันทึกว่าผู้เล่น ซื้อ Hex แล้วใน turn นี้
     * @throws IllegalStateException
     *  หากเรียกซ้ำใน turn เดียวกัน
     *
     * @sideEffect
     *  - เปลี่ยน spawnedThisTurn = true
     */
    public void registerBuyHexThisTurn() {
        if (boughtHexThisTurn)
            throw new IllegalStateException("Already bought hex this turn");
        boughtHexThisTurn = true;
    }

    public boolean boughtHexLastTurn() {
        return boughtHexLastTurn;
    }

}
