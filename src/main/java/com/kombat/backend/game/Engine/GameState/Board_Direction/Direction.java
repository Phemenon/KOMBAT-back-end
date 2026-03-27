package com.kombat.backend.game.Engine.GameState.Board_Direction;

public enum Direction {
    UP {
        public int[] delta(int col) { return new int[]{-1, 0}; }
    },

    UP_RIGHT {
        public int[] delta(int col) {
            return (col % 2 == 1)
                    ? new int[]{-1, +1}
                    : new int[]{0, +1};
        }
    },

    DOWN_RIGHT {
        public int[] delta(int col) {
            return (col % 2 == 1)
                    ? new int[]{0, +1}
                    : new int[]{+1, +1};
        }
    },

    DOWN {
        public int[] delta(int col) { return new int[]{+1, 0}; }
    },

    DOWN_LEFT {
        public int[] delta(int col) {
            return (col % 2 == 1)
                    ? new int[]{0, -1}
                    : new int[]{+1, -1};
        }
    },

    UP_LEFT {
        public int[] delta(int col) {
            return (col % 2 == 1)
                    ? new int[]{-1, -1}
                    : new int[]{0, -1};
        }
    };

    /**
     * คำนวณ delta (direction for row, direction for col) สำหรับการเคลื่อนที่แบบ hex grid
     * แทน hex grid ด้วย (row, col) แบบ array ปกติ
     * @param col คอลัมน์ปัจจุบัน
     * @return array ขนาด 2: [direction for row,direction for col]
     */
    public abstract int[] delta(int col);
}
