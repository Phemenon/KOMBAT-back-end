package com.kombat.backend.game.Engine.GameState.Board_Direction;

import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;
import lombok.Getter;

import java.util.Optional;

public class Board {
    /**
     * -- GETTER --
     *
     */
    @Getter
    private final int rows;
    /**
     * -- GETTER --
     *
     */
    @Getter
    private final int cols;

    private final Hex[][] grid;

    /**
     * สร้างกระดานเกมตามขนาดที่กำหนด
     * @param rows จำนวนแถว (> 0)
     * @param cols จำนวนคอลัมน์ (> 0)
     *
     * @postcondition
     *  - grid ถูกสร้างครบทุกช่อง
     *  - ทุก Hex ถูกตั้งค่า default = ไม่สามารถ spawn ได้
     *  - กำหนด base spawn area ให้ Player 1 และ Player 2
     *
     * @sideEffect
     *  - สร้าง Hex object ใหม่ทั้งหมด
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Hex[rows][cols];
        initHexes();
    }

    private void initHexes() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // default: ไม่ spawn ได้ทั้งสองฝั่ง
                grid[row][col] = new Hex(false, false);
            }
        }
        grid[0][0] = new Hex(true, false);                // P1 base
        grid[0][1] = new Hex(true, false);
        grid[0][2] = new Hex(true, false);
        grid[1][0] = new Hex(true, false);
        grid[1][1] = new Hex(true, false);
        grid[rows - 1][cols - 1] = new Hex(false, true);  // P2 base
        grid[rows - 1][cols - 2] = new Hex(false, true);
        grid[rows - 1][cols - 3] = new Hex(false, true);
        grid[rows - 2][cols - 1] = new Hex(false, true);
        grid[rows - 2][cols - 2] = new Hex(false, true);

    }

    /**
     * ตรวจสอบว่าพิกัดอยู่ในขอบเขตของกระดานหรือไม่
     * @param r แถว
     * @param c คอลัมน์
     * @return true ถ้า (r, c) อยู่ในกระดาน, false ถ้าอยู่นอกขอบ
     */
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    /**
     * คืน Hex ที่ตำแหน่งที่กำหนด
     *
     * @param r แถว
     * @param c คอลัมน์
     * @return Hex หรือ null หากอยู่นอกขอบเขต
     * @warning
     *  - คืน reference จริงของ Hex
     */
    public Hex getHex(int r, int c) {
        return inBounds(r, c) ? grid[r][c] : null;
    }

    /**
     * ตรวจสอบว่าพิกัดที่กำหนดมี minion อยู่หรือไม่
     *
     * @param r แถว
     * @param c คอลัมน์
     * @return true ถ้ามี minion อยู่ในช่องนั้น
     */
    public boolean isMinionHere(int r, int c) {
        return inBounds(r, c) && grid[r][c].isMinionHere();
    }

    /**
     * คืน minion ที่อยู่ในพิกัดที่กำหนด
     *
     * @param r แถว
     * @param c คอลัมน์
     * @return minion ที่ตำแหน่งนั้น หรือ null ถ้าอยู่นอกขอบหรือไม่มี minion
     */
    public Minion getMinionAt(int r, int c) {
        return inBounds(r, c) ? grid[r][c].getMinion() : null;
    }

    /**
     * วาง Minion ลงบนกระดาน
     *
     * @param m Minion (ต้องไม่เป็น null)
     * @param r แถวเป้าหมาย อยู่ในขอบเขต ช่องนั้นต้องไม่มี Minion อยู่
     * @param c คอลัมน์เป้าหมาย อยู่ในขอบเขต ช่องนั้นต้องไม่มี Minion อยู่
     *
     * @return true หากวางสำเร็จ
     *
     * @sideEffect
     *  - เปลี่ยน state ของ grid
     *  - เปลี่ยนตำแหน่งของ Minion
     */
    public boolean placeMinion(Minion m, int r, int c) {
        if (!inBounds(r, c) || isMinionHere(r, c)) return false;
        Hex hex = grid[r][c];
        if (hex.isMinionHere()) return false;
        hex.placeMinion(m);
        m.setPosition(r, c);
        return true;
    }

    /**
     * ลบ minion ออกจากกระดาน
     * ใช้เมื่อ minion ตายหรือถูกนำออกจากเกม
     * @param m minion ที่ต้องการลบ
     * Side Effect:
     * - ช่องเดิมของ minion จะถูกตั้งค่าเป็น null
     */
    public void removeMinion(Minion m) {
        int row = m.getRow();
        int col = m.getCol();

        if (inBounds(row, col)) grid[row][col].removeMinion();
    }

    /**
     * เคลื่อนที่ Minion ไป 1 ช่องตาม Direction
     *
     * @param m Minion ที่จะเคลื่อนที่ -> m != null
     * @param dir ทิศทางการเคลื่อนที่ -> dir != null
     * - ช่องเป้าหมายต้องอยู่ในขอบเขต
     * - ช่องเป้าหมายต้องว่าง
     *
     * @return true หากเคลื่อนที่สำเร็จ
     *
     * @sideEffect
     *  - เปลี่ยนตำแหน่ง Minion บนกระดาน
     */
    public boolean move(Minion m, Direction dir) {
        int[] d = dir.delta(m.getCol());
        int nr = m.getRow() + d[0];
        int nc = m.getCol() + d[1];

        if (!inBounds(nr, nc) || grid[nr][nc].isMinionHere()) return false;

        grid[m.getRow()][m.getCol()].removeMinion();
        grid[nr][nc].placeMinion(m);
        m.setPosition(nr, nc);
        return true;
    }

    /**
     * ค้นหา minion ตัวแรกในทิศทางที่กำหนดจากตำแหน่งของ minion ที่ระบุ
     * ใช้สำหรับ implement ally / opponent / nearby
     * @param from minion ต้นทาง
     * @param dir ทิศทางที่ต้องการสแกน
     * @return Optional ของ minion ตัวแรกที่พบในทิศทางนั้น
     *         หรือ Optional.empty() ถ้าไม่พบ minion เลย
     * Side Effect:
     * - ไม่มี
     */
    public Optional<Minion> firstInDirection(Minion from, Direction dir) {
        int row = from.getRow();
        int col = from.getCol();

        while (true) {
            int[] d = dir.delta(col);
            row += d[0];
            col += d[1];

            if (!inBounds(row, col)) return Optional.empty();
            if (grid[row][col].isMinionHere()) return Optional.of(grid[row][col].getMinion());
        }
    }

    /**
     * ตรวจสอบว่าผู้เล่นสามารถ Spawn ที่ตำแหน่งนี้ได้หรือไม่
     * @param player เจ้าของ
     * @param row แถว
     * @param col คอลัมน์
     *
     * @return true หาก:
     *  - อยู่ในขอบเขต
     *  - Hex อนุญาตให้ player spawn
     *  - ไม่มี Minion อยู่
     */
    public boolean canSpawn(Player player, int row, int col) {
        if (!inBounds(row, col)) return false;

        Hex hex = grid[row][col];
        return hex.canSpawn(player) && !hex.isMinionHere();
    }

    /**
     * สแกนหา Minion ตัวแรกในทิศทางที่กำหนด
     * @param from Minion ต้นทาง
     * @param dir ทิศทาง
     *
     * @return Optional<ScanResult>
     *  - empty หากไม่พบ
     *  - หากพบ จะคืน:
     *      - Minion ที่พบ
     *      - ระยะห่างจากต้นทาง
     */
    public Optional<ScanResult> scanDirection(Minion from, Direction dir) {
        int row = from.getRow();
        int col = from.getCol();
        int distance = 0;

        while (true) {
            int[] d = dir.delta(col);
            row += d[0];
            col += d[1];
            distance++;

            if (!inBounds(row, col)) return Optional.empty();

            if (grid[row][col].isMinionHere()) {
                return Optional.of(new ScanResult(grid[row][col].getMinion(), distance));
            }
        }
    }

    /**
     * ตรวจสอบว่าตำแหน่งนี้ติดกับพื้นที่ที่ Spawn ได้ของ player หรือไม่
     * ใช้สำหรับการซื้อ Hex เพิ่ม
     *
     * @param player ผู้เล่น
     * @param row แถว
     * @param col คอลัมน์
     * - ตำแหน่งต้องอยู่ในกระดาน
     *
     * @return true หากมี Hex ข้างเคียงอย่างน้อย 1 ช่องที่ spawn ได้
     *
     */
    public boolean isAdjacentToSpawnable(Player player, int row, int col) {
        if (!inBounds(row, col)) return false;

        Hex target = grid[row][col];

        // ห้ามซื้อถ้ามัน spawnable อยู่แล้ว
        if (target.canSpawn(player)) return false;

        for (Direction dir : Direction.values()) {

            int[] d = dir.delta(col);
            int nr = row + d[0];
            int nc = col + d[1];

            if (!inBounds(nr, nc)) continue;

            if (grid[nr][nc].canSpawn(player)) {
                return true;
            }
        }

        return false;
    }

    public record ScanResult(Minion minion, int distance) {}

    /**
     * ค้นหา opponent ที่ใกล้ที่สุดในทุกทิศทาง
     * @param self Minion ต้นทาง
     * @return ค่า encode แบบ:
     *  distance * 10 + directionIndex (1–6)
     *  หรือ 0 หากไม่พบ
     *
     * @note
     *  ใช้สำหรับ Strategy Script Engine
     */
    public long findClosestOpponent(Minion self) {
        long bestDistance = Long.MAX_VALUE;
        int bestDirIndex = -1;

        Direction[] dirs = Direction.values();

        for (int i = 0; i < dirs.length; i++) {
            Direction dir = dirs[i];

            Optional<ScanResult> result = scanDirection(self, dir);
            if (result.isEmpty()) continue;

            Minion found = result.get().minion();
            int dist = result.get().distance();

            if (found.getOwner() == self.getOwner()) continue; // ต้องเป็น opponent

            if (dist < bestDistance) {
                bestDistance = dist;
                bestDirIndex = i + 1; // 1–6
            }
        }

        if (bestDirIndex == -1) return 0;

        return bestDistance * 10 + bestDirIndex;
    }

    /**
     * ค้นหา ally ที่ใกล้ที่สุดในทุกทิศทาง
     * @param self Minion ต้นทาง
     * @return encode distance + direction หรือ 0 หากไม่พบ
     */
    public long findClosestAlly(Minion self) {
        long bestDistance = Long.MAX_VALUE;
        int bestDirIndex = -1;

        Direction[] dirs = Direction.values();

        for (int i = 0; i < dirs.length; i++) {
            Direction dir = dirs[i];

            Optional<ScanResult> result = scanDirection(self, dir);
            if (result.isEmpty()) continue;

            Minion found = result.get().minion();
            int dist = result.get().distance();

            if (found.getOwner() != self.getOwner()) continue; // ต้องเป็น ally

            if (dist < bestDistance) {
                bestDistance = dist;
                bestDirIndex = i + 1;
            }
        }

        if (bestDirIndex == -1) return 0;

        return bestDistance * 10 + bestDirIndex;
    }

    /**
     * ตรวจสอบว่า Minion นี้ยังอยู่บนกระดานหรือไม่
     *
     * @param m Minion
     * @return true หากพบใน grid
     */
    public boolean containsMinion(Minion m) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].getMinion() == m) return true;
            }
        }
        return false;
    }

}
