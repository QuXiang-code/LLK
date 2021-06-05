package com.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @Author yzx
 * @Description 对话框中的按钮自定义 UI
 * @Date 2020/12/13
 */
public class GameButton extends JButton implements MouseListener {
    // 绘制时使用的颜色
    private Color backgroundColor;
    private Color disabledColor;

    private Color originBgColor;
    private Color foreColor;
    public static final Color LIGHT_BLUE = new Color(18, 150, 219);

    public GameButton(String text) {
        super(text);
        setContentAreaFilled(false);
        originBgColor = LIGHT_BLUE;
        foreColor = Color.white;
        disabledColor = Color.lightGray;
        addMouseListener(this);
    }

    public GameButton(String text, Color color) {
        super(text);
        setContentAreaFilled(false);
        originBgColor = color;
        foreColor = Color.white;
        disabledColor = Color.lightGray;
        addMouseListener(this);
    }

    @Override
    public void paint(Graphics g) {
        Rectangle rect = getVisibleRect();
        Graphics2D g2d = (Graphics2D) g;
        // 画背景
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (backgroundColor == null) backgroundColor = originBgColor;
        g2d.setColor(isEnabled() ? backgroundColor: disabledColor);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        // 画文字
        FontMetrics fontMetrics = getFontMetrics(getFont());
        String text = getText();
        int stringWidth = fontMetrics.stringWidth(text);
        int stringHeight = fontMetrics.getHeight();
        g2d.setFont(getFont());
        g2d.setColor(foreColor);
        g2d.drawString(text, (rect.width - stringWidth) / 2, (rect.height - stringHeight) / 2 + 20);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        backgroundColor = originBgColor.darker();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        backgroundColor = originBgColor.brighter();
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        backgroundColor = originBgColor;
    }
}
