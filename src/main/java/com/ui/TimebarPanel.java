package com.ui;

import com.sun.org.apache.xpath.internal.operations.Or;

import javax.swing.*;
import java.awt.*;

public class TimebarPanel extends JPanel {
    private int totalTime;
    private int currTime;
    private final int gapX = 5;
    private final int gapY = 3;
    private final Color start = Color.red;
    private final Color end = new Color(60, 179, 113);
    private final Color border = new Color(32, 178, 170);

    public TimebarPanel(int totalTime) {
        this.totalTime = totalTime;
        this.currTime = totalTime;
    }

    public void setCurrTime(int currTime) {
        this.currTime = currTime;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(border);
        g2d.drawRect(gapX, gapY, getWidth() - 1 - gapX * 2, getHeight() - 1 - gapY * 2);
        if (currTime > 0) {
            GradientPaint gradientPaint = new GradientPaint(
                    gapX + 2, gapY + 2, start,
                    (getWidth() - 4 - gapX * 2) * currTime / totalTime, getHeight() - 4 - gapY * 2, end);
            g2d.setPaint(gradientPaint);
            g2d.fillRect(gapX + 2, gapY + 2, (getWidth() - 4 - gapX * 2) * currTime / totalTime, getHeight() - 4 - gapY * 2);
        }
    }
}
