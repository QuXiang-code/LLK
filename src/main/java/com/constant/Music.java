package com.constant;

public class Music {
    private static final int start = 101;
    private static final int end = 117;
    public static final int totalNum = end - start + 1;

    public static final String[] list = new String[totalNum];

    static {
        int j = 0;
        for (int i = start; i <= end; i++) {
            list[j++] = SimplePath.MUSIC_PATH + i + ".mid";
        }
    }
}
