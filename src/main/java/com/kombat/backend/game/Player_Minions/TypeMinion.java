package com.kombat.backend.game.Player_Minions;

import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Strategy;

/**
 * แทน "ชนิด" ของมินเนียน (Kind of Minion)
 * เก็บข้อมูลคงที่ที่ไม่เปลี่ยนตลอดทั้งเกม ได้แก่
 * - ชื่อชนิด
 * - ค่า defense factor
 * - Strategy ของชนิดนั้น
 * หมายเหตุ:
 * Strategy ต้องเป็น stateless และสามารถแชร์ instance ได้
 */
public record TypeMinion(
        String name,
        long defenseFactor,
        Strategy strategy
) {

    /**
     * สร้างชนิดของมินเนียน
     *
     * @param name           ชื่อชนิดของมินเนียน
     * @param defenseFactor  ค่า defense factor (ต้องไม่ติดลบ)
     * @param strategy       กลยุทธ์ของมินเนียน (ต้องไม่เป็น null)
     *
     * @throws IllegalArgumentException หากข้อมูลไม่ถูกต้อง
     */
    public TypeMinion {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Type name must not be blank");

        if (defenseFactor < 0)
            throw new IllegalArgumentException("Defense factor must be non-negative");

        if (strategy == null)
            throw new IllegalArgumentException("Strategy must not be null");
    }

    /**
     * @return ชื่อชนิดของมินเนียน
     */
    public String getTypeName() {
        return name;
    }

    public long getDefenseFactor() {
        return defenseFactor;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}
