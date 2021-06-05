package com.constant;

public class Level {
    public static final int NORMAL = 0;
    // 向下
    public static final int DOWN = 1;
    // 向左
    public static final int LEFT = 2;
    // 向上
    public static final int UP = 3;
    // 向右
    public static final int RIGHT = 4;
    // 上下分离
    public static final int UP_AND_DOWN = 5;
    // 左右分离
    public static final int LEFT_AND_RIGHT = 6;
    // 上下集中
    public static final int UP_AND_DOWN_GATHER = 7;
    // 左右集中
    public static final int LEFT_AND_RIGHT_GATHER = 8;
    // 上左下右
    public static final int UP_LEFT_DOWN_RIGHT = 9;
    // 左下右上
    public static final int LEFT_DOWN_RIGHT_UP = 10;
    // 向外扩散
    public static final int OUTWARD_SPREAD = 11;
    // 向内集中
    public static final int INWARD_GATHER = 12;

    public static String[] name = {"无", "向下", "向左", "向上", "向右", "上下分离", "左右分离", "上下集中", "左右集中", "上左下右", "左下右上", "向外扩散", "向内集中"};
}
