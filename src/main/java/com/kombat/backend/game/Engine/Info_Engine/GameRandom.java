package com.kombat.backend.game.Engine.Info_Engine;

import com.kombat.backend.game.Interface.Randomize;

import java.util.Random;

/**
 * ตัวสร้างตัวเลขสุ่มสำหรับใช้ใน Game Engine
 * ใช้สำหรับรองรับ special variable {@code random} ใน minion strategy
 * โดยจะให้ค่าจำนวนเต็มแบบสุ่มในช่วง 0–999
 */
public class GameRandom implements Randomize {

    private final Random random;

    /**
     * สร้างตัวสร้างเลขสุ่มด้วย seed ที่กำหนด
     *
     * @param seed ค่า seed สำหรับ random number generator
     *             ใช้เพื่อให้ผลลัพธ์สามารถทำซ้ำได้ กำหนดขอบเขต
     */
    public GameRandom(long seed) {
        this.random = new Random(seed);
    }

    /**
     * สุ่มค่าจำนวนเต็มสำหรับ special variable {@code random}
     *
     * @return ค่าจำนวนเต็มแบบสุ่มในช่วง 0 ถึง 999
     *
     * @implNote
     * ในการเรียกใช้แต่ละครั้งจะได้ค่าใหม่เสมอ
     * การใช้ {@link java.util.Random} ที่ถูกกำหนด seed ไว้
     */
    @Override
    public long nextRandom() {
        return random.nextInt(1000);
    }


}
