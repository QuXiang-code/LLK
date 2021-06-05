package com.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class Block {
    public int x;
    public int y;
    public BufferedImage img;

    public Block(int x, int y, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.img = img;
    }

    // 判断坐标是否相同
    public boolean equalsByPosition(Block b) {
        return x == b.x && y == b.y;
    }

    @Override
    public String toString() {
        return "Block{" +
                "x=" + x +
                ", y=" + y +
                ", img=" + img +
                '}';
    }
}
