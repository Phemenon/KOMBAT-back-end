package com.kombat.backend.game.Engine.GameState.Fight;

import com.kombat.backend.game.Engine.GameState.Mode_State.GameState;
import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Player_Minions.Minion;

public record Combat(GameState state) {


    /**
     * ยิงโจมตีจากมินเนียนผู้โจมตีไปยังช่องที่อยู่ติดกันตามทิศทางที่กำหนด
     *
     * @param attacker มินเนียนผู้โจมตี
     * @param dir ทิศทางในการโจมตี
     * @param cost งบประมาณที่ใช้เพิ่มพลังโจมตี
     * @return ผลลัพธ์ของการโจมตี KILL ได้ ,ตีพลาด MISS,ตีโดน HIT,NO_BUDGET
     *
     * @implNote
     *   ค่าใช้จ่ายรวมของการโจมตีเท่ากับ cost + 1
     *   หาก budget ไม่เพียงพอ จะไม่เกิดการโจมตีและ budget จะไม่ถูกหัก
     *   หาก budget เพียงพอ budget จะถูกหักทันทีไม่ว่าการโจมตีจะโดนหรือไม่
     *   การโจมตีมีผลเฉพาะช่องถัดไปหนึ่งช่องตามทิศทางที่กำหนด
     *   หากช่องเป้าหมายอยู่นอกกระดานหรือไม่มีมินเนียน จะถือว่า MISS
     *   ค่าความเสียหายคำนวณจาก max(1, cost - defense factor ของเป้าหมาย)
     *   หาก HP ของเป้าหมายหลังรับความเสียหายต่ำกว่า 1 เป้าหมายจะตายและถูกลบออกจาก board
     *   รองรับกรณีโจมตีตัวเอง (self-attack)
     */
    public CombatResult shoot(Minion attacker, Direction dir, long cost) {
        if (cost < 0) return CombatResultFactory.noBudget(attacker);
        long totalCost = cost + 1;
        if (attacker.getOwner().getBudget() < totalCost) return CombatResultFactory.noBudget(attacker);
        attacker.getOwner().spendBudget(totalCost);
        int[] d = dir.delta(attacker.getCol());
        int tr = attacker.getRow() + d[0];
        int tc = attacker.getCol() + d[1];

        if (!state.getBoard().inBounds(tr, tc)) return CombatResultFactory.miss(attacker,totalCost,tr,tc);

        Minion target = state.getBoard().getMinionAt(tr, tc);
        if (target == null) return CombatResultFactory.miss(attacker,cost,tr,tc);

        long dmg = computeDamage(cost, target);

        if (target.damage(dmg)) {
            state.removeMinion(target);
            return CombatResultFactory.kill(attacker,target,dmg,cost,tr,tc);
        }

        return CombatResultFactory.hit(attacker,target,dmg,cost,tr,tc);

    }

    private long computeDamage(long cost, Minion target) {
        return Math.max(1, cost - target.getDefenseFactor());
    }

}
