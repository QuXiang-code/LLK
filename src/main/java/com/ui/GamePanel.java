package com.ui;

import com.constant.Images;
import com.model.Block;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private boolean playing = false;
    private Block[][] board;
    private int row;
    private int col;

    public GamePanel() {

    }

    public GamePanel(Block[][] board, int row, int col) {
        this.board = board;
        this.row = row;
        this.col = col;
    }

    public void init(Block[][] board, int row, int col) {
        this.board = board;
        this.row = row;
        this.col = col;
    }

    public void setBoard(Block[][] board) {
        this.board = board;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public void paint(Graphics g) {
        if(playing) {
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, Images.basePixels * (row + 2), Images.basePixels * (col + 2));
            for (int i = 1; i <= row; i++) {
                for (int j = 1; j <= col; j++) {
                    if (board[i][j].img != null)
                        g.drawImage(board[i][j].img, Images.basePixels * j, Images.basePixels * i, null);
                }
            }
        } else {
            g.drawImage(
                    Images.backgroundImg,
                    0, 0,
                    getWidth(),
                    getHeight(),
                    this
            );
        }
    }
}
