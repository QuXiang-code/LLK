package com.ui;

import com.constant.Images;
import com.constant.Level;
import com.constant.Music;
import com.constant.Sound;
import com.model.Block;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MainFrame extends JFrame {
    // 初始棋盘、各难度棋盘
    static final int INIT_ROW = 11;
    static final int INIT_COL = 12;
    static final int JUN_ROW = 12;
    static final int JUN_COL = 12;
    static final int MID_ROW = 14;
    static final int MID_COL = 14;
    static final int PRO_ROW = 16;
    static final int PRO_COL = 16;

    static final int offsetW = 15;
    static final int offsetH = 148;

    // 游戏计数重置相关
    static final int MATCHED_COUNT = 0;
    static final int SCORE = 0;
    static final int TOTAL_TIME = 200;
    static final int LIVES = 4;
    static final int TIP = 10;
    static final int HINT = 0;
    static final int LEVEL = Level.NORMAL;

    // 标签默认值
    static final String DEFAULT_VALUE = "--";

    static int row = INIT_ROW;
    static int col = INIT_COL;
    static int r = row + 2;
    static int c = col + 2;
    static int WINDOW_WIDTH = Images.basePixels * c + offsetW;
    static int WINDOW_HEIGHT = Images.basePixels * r + offsetH;
    // 是否正在游戏
    static boolean playing = false;
    // 当前已配对数量
    static int matchedCount = MATCHED_COUNT;
    // 分数
    static int score = SCORE;
    // 总时间
    static int totalTime = TOTAL_TIME;
    // 当前时间
    static AtomicInteger currTime = new AtomicInteger(totalTime);
    // 生命
    static int lives = LIVES;
    // 提示数量
    static int tip = TIP;
    // 连击数量
    static int hint = HINT;
    // 音乐开关
    static boolean musicOn = true;
    // 音效开关
    static boolean soundOn = true;
    // 选中的第一个块
    static Block first;
    // 关卡
    static int level = LEVEL;

    static Block[][] board;

    static GamePanel gamePanel = new GamePanel();
    static JPanel statePanel = new JPanel();
    static JPanel stateNorthPanel = new JPanel();
    static JPanel stateSouthPanel = new JPanel();
    static JPanel buttonPanel = new JPanel();
    static JLabel levelLabel = new JLabel("关卡：");
    static JLabel levelValueLabel = new JLabel(DEFAULT_VALUE);
    static JLabel changeLabel = new JLabel("变化：");
    static JLabel changeValueLabel = new JLabel(DEFAULT_VALUE);
    static JLabel livesLabel = new JLabel("生命：");
    static JLabel livesValueLabel = new JLabel(DEFAULT_VALUE);
    static JLabel tipLabel = new JLabel("提示：");
    static JLabel tipValueLabel = new JLabel(DEFAULT_VALUE);
    static JLabel hintLabel = new JLabel("连击：");
    static JLabel hintValueLabel = new JLabel(DEFAULT_VALUE);
    static JLabel scoreLabel = new JLabel("分数：");
    static JLabel scoreValueLabel = new JLabel(DEFAULT_VALUE);
    //    static JLabel timeLabel = new JLabel("时间：");
    static TimebarPanel timeBarPanel = new TimebarPanel(totalTime);

    private static final Color LIGHT_BLUE = new Color(32, 178, 170);
    private static final Color INDIAN_RED_3 = new Color(205, 85, 85);
    static GameButton junButton = new GameButton("初级", INDIAN_RED_3);
    static GameButton midButton = new GameButton("中级", INDIAN_RED_3);
    static GameButton proButton = new GameButton("高级", INDIAN_RED_3);
    static GameButton tipButton = new GameButton("提示");
    static GameButton shuffleButton = new GameButton("洗牌");
    static GameButton quitButton = new GameButton("弃局", INDIAN_RED_3);
    static GameButton musicButton = new GameButton("音乐：开");
    static GameButton soundButton = new GameButton("音效：开");

    static Font globalFont = new Font("微软雅黑", Font.PLAIN, 18);

    static Thread timeThread;
    static Thread musicThread;
    static Thread soundThread;

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        new MainFrame().init();
    }

    void init() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        initUI();
    }

    // 初始化界面，并未开始游戏
    void initUI() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 不在游戏中，点击没用
                if (!playing) return;
                int x = e.getX(), y = e.getY();
                // 在棋盘内点击
                if (x >= Images.basePixels && x <= Images.basePixels * (col + 1) && y >= Images.basePixels && y <= Images.basePixels * (row + 1)) {
                    // 注意鼠标坐标的 xy 和数组坐标 xy 是反的！
                    Block selectedBlock = board[y / Images.basePixels][x / Images.basePixels];
                    // 不为空块
                    if (selectedBlock.img == null) return;
                    Graphics2D g2d = ((Graphics2D) gamePanel.getGraphics());
                    g2d.setColor(Color.red);
                    g2d.setStroke(new BasicStroke(3f));
                    if (first == null) {
                        first = selectedBlock;
                        g2d.drawRect(first.y * Images.basePixels, first.x * Images.basePixels, Images.basePixels, Images.basePixels);
                        // 播放点击音效
                        if (soundOn) playSound(Sound.CLICK);
                    } else {
                        if (match(first, selectedBlock, true)) {
                            g2d.drawRect(selectedBlock.y * Images.basePixels, selectedBlock.x * Images.basePixels, Images.basePixels, Images.basePixels);
                            // 播放匹配音效
                            if (soundOn) playSound(Sound.MATCH);
                            first.img = selectedBlock.img = null;
                            moveBlock(first, selectedBlock);
                            // 加分
                            scoreValueLabel.setText(String.valueOf(score += 10 + hint));
                            // 加时间
                            currTime.set(Math.min(totalTime, currTime.get() + 2));
                            // 连击
                            hintValueLabel.setText(String.valueOf(++hint));
                            // 关卡完成
                            if (++matchedCount == row * col / 2) {
                                // 停止倒计时线程
                                timeThread.interrupt();
                                timeThread = new Thread(() -> {
                                    try {
                                        while (currTime.get() > 0) {
                                            if (timeThread.isInterrupted()) return;
                                            Thread.sleep(1000);
                                            timeBarPanel.setCurrTime(currTime.decrementAndGet());
                                            statePanel.updateUI();
                                        }
                                        // 时间到，游戏结束
                                        gameOver();
                                    } catch (InterruptedException exception) {

                                    }
                                });
                                scoreValueLabel.setText(String.valueOf(score += currTime.get()));
                                JOptionPane.showMessageDialog(null, "关卡完成，额外获得 " + currTime + " 分");
                                matchedCount = MATCHED_COUNT;
                                // 下一关
                                levelValueLabel.setText(String.valueOf(++level));
                                changeValueLabel.setText(Level.name[level % Level.name.length]);
                                // 重置计时
                                currTime.set(TOTAL_TIME);
                                timeBarPanel.setCurrTime(currTime.get());
                                timeThread.start();
                                initData();
                                // 奖励提示和生命
                                tipValueLabel.setText(String.valueOf(tip += 2));
                                tipButton.setEnabled(true);
                                livesValueLabel.setText(String.valueOf(++lives));
                                shuffleButton.setEnabled(true);
                            }
                            // 判断是否有解，无解就洗牌
                            if (!hasSolution(false)) {
                                // 没有生命值，无法洗牌，游戏结束
                                if (lives == 0) {
                                    gameOver();
                                    return;
                                }
                                shuffle();
                                livesValueLabel.setText(String.valueOf(--lives));
                                if (soundOn) playSound(Sound.SHUFFLE);
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        } else {
                            hint = 0;
                            if (soundOn) playSound(Sound.LOSE_HINT);
                        }
                        hintValueLabel.setText(String.valueOf(hint));
                        first = null;
                        gamePanel.updateUI();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                if (x >= Images.basePixels && x <= Images.basePixels * (col + 1) && y >= Images.basePixels && y <= Images.basePixels * (row + 1))
                    gamePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else gamePanel.setCursor(Cursor.getDefaultCursor());
            }
        });

        // 状态栏
        levelLabel.setFont(globalFont);
        levelValueLabel.setFont(globalFont);
        changeLabel.setFont(globalFont);
        changeValueLabel.setFont(globalFont);
        livesLabel.setFont(globalFont);
        livesValueLabel.setFont(globalFont);
        tipLabel.setFont(globalFont);
        tipValueLabel.setFont(globalFont);
        hintLabel.setFont(globalFont);
        hintValueLabel.setFont(globalFont);
        scoreLabel.setFont(globalFont);
        scoreValueLabel.setFont(globalFont);
//        timeLabel.setFont(globalFont);
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        changeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        livesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
//        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        stateNorthPanel.setLayout(new GridLayout(2, 6));
        stateNorthPanel.add(levelLabel);
        stateNorthPanel.add(levelValueLabel);
        stateNorthPanel.add(changeLabel);
        stateNorthPanel.add(changeValueLabel);
        stateNorthPanel.add(livesLabel);
        stateNorthPanel.add(livesValueLabel);
        stateNorthPanel.add(tipLabel);
        stateNorthPanel.add(tipValueLabel);
        stateNorthPanel.add(hintLabel);
        stateNorthPanel.add(hintValueLabel);
        stateNorthPanel.add(scoreLabel);
        stateNorthPanel.add(scoreValueLabel);
        stateSouthPanel.setLayout(new BorderLayout());
//        stateSouthPanel.add(timeLabel,BorderLayout.WEST);
        timeBarPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 25));
        stateSouthPanel.add(timeBarPanel, BorderLayout.CENTER);
        statePanel.setLayout(new BorderLayout());
        statePanel.add(stateNorthPanel, BorderLayout.NORTH);
        statePanel.add(stateSouthPanel, BorderLayout.SOUTH);

        // 按钮栏
        junButton.addActionListener(e -> {
            row = JUN_ROW;
            col = JUN_COL;
            r = row + 2;
            c = col + 2;
            WINDOW_WIDTH = Images.basePixels * c + offsetW;
            WINDOW_HEIGHT = Images.basePixels * r + offsetH;
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            setLocationRelativeTo(null);
            board = new Block[r][c];
            gamePanel.init(board, row, col);
            prepareUI(true);
            initData();
        });
        midButton.addActionListener(e -> {
            row = MID_ROW;
            col = MID_COL;
            r = row + 2;
            c = col + 2;
            WINDOW_WIDTH = Images.basePixels * c + offsetW;
            WINDOW_HEIGHT = Images.basePixels * r + offsetH;
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            setLocationRelativeTo(null);
            board = new Block[r][c];
            gamePanel.init(board, row, col);
            prepareUI(true);
            initData();
        });
        proButton.addActionListener(e -> {
            row = PRO_ROW;
            col = PRO_COL;
            r = row + 2;
            c = col + 2;
            WINDOW_WIDTH = Images.basePixels * c + offsetW;
            WINDOW_HEIGHT = Images.basePixels * r + offsetH;
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            setLocationRelativeTo(null);
            board = new Block[r][c];
            gamePanel.init(board, row, col);
            prepareUI(true);
            initData();
        });
        tipButton.addActionListener(e -> {
            hasSolution(true);
            tipValueLabel.setText(String.valueOf(--tip));
            if (soundOn) playSound(Sound.TIP);
            if (tip == 0) tipButton.setEnabled(false);
        });
        shuffleButton.addActionListener(e -> {
            shuffle();
            livesValueLabel.setText(String.valueOf(--lives));
            if (lives == 0) shuffleButton.setEnabled(false);
            gamePanel.updateUI();
            if (soundOn) playSound(Sound.SHUFFLE);
        });
        quitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(null, "确定弃局吗？", "弃局", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                quit();
            }
        });
        musicButton.addActionListener(e -> {
            musicOn = !musicOn;
            musicButton.setText("音乐：" + (musicOn ? "开" : "关"));
            if (musicOn) {
                playMusic();
            } else {
                stopMusic();
            }
        });
        soundButton.addActionListener(e -> {
            soundOn = !soundOn;
            soundButton.setText("音效：" + (soundOn ? "开" : "关"));
        });
        junButton.setFont(globalFont);
        midButton.setFont(globalFont);
        proButton.setFont(globalFont);
        tipButton.setFont(globalFont);
        shuffleButton.setFont(globalFont);
        quitButton.setFont(globalFont);
        musicButton.setFont(globalFont);
        soundButton.setFont(globalFont);
//        shuffleButton.setHorizontalAlignment(SwingConstants.CENTER);
//        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(junButton);
        buttonPanel.add(midButton);
        buttonPanel.add(proButton);
        buttonPanel.add(tipButton);
        buttonPanel.add(shuffleButton);
        buttonPanel.add(quitButton);
        buttonPanel.add(musicButton);
        buttonPanel.add(soundButton);

        prepareUI(false);
        // 倒计时线程
        timeThread = new Thread(() -> {
            try {
                while (currTime.get() > 0) {
                    if (timeThread.isInterrupted()) return;
                    Thread.sleep(1000);
                    timeBarPanel.setCurrTime(currTime.decrementAndGet());
                    statePanel.updateUI();
                }
                // 时间到，游戏结束
                gameOver();
            } catch (InterruptedException e) {

            }
        });

        setIconImage(Images.titleImg);
        setTitle("furry 连连看");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        // 窗口弹出在屏幕中央
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        setUndecorated(true);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(statePanel, BorderLayout.NORTH);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);
        gamePanel.setPlaying(playing);
        add(gamePanel, BorderLayout.CENTER);

        // 更新 LAF
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.updateComponentTreeUI(this);

        // 背景音乐
        playMusic();
        setVisible(true);
    }

    void prepareUI(boolean startGame) {
        // 重置游戏计数
        if (!startGame) {
            matchedCount = MATCHED_COUNT;
            score = SCORE;
            currTime.set(TOTAL_TIME);
            timeBarPanel.setCurrTime(currTime.get());
            lives = LIVES;
            tip = TIP;
            hint = HINT;
            first = null;
            level = LEVEL;
        }

        // 标签显示
        levelValueLabel.setText(startGame ? String.valueOf(level) : DEFAULT_VALUE);
        changeValueLabel.setText(startGame ? Level.name[level] : DEFAULT_VALUE);
        livesValueLabel.setText(startGame ? String.valueOf(lives) : DEFAULT_VALUE);
        tipValueLabel.setText(startGame ? String.valueOf(tip) : DEFAULT_VALUE);
        hintValueLabel.setText(startGame ? String.valueOf(hint) : DEFAULT_VALUE);
        scoreValueLabel.setText(startGame ? String.valueOf(score) : DEFAULT_VALUE);

        // 开始/停止倒计时线程
        if (timeThread != null) {
            if (startGame) timeThread.start();
            else {
                timeThread.interrupt();
                timeThread = new Thread(() -> {
                    try {
                        while (currTime.get() > 0) {
                            if (timeThread.isInterrupted()) return;
                            Thread.sleep(1000);
                            timeBarPanel.setCurrTime(currTime.decrementAndGet());
                            statePanel.updateUI();
                        }
                        gameOver();
                    } catch (InterruptedException e) {

                    }
                });
            }
        }

        // 启用/禁用难度按钮
        junButton.setEnabled(!startGame);
        midButton.setEnabled(!startGame);
        proButton.setEnabled(!startGame);

        // 启用/禁用提示/洗牌按钮
        tipButton.setEnabled(startGame);
        shuffleButton.setEnabled(startGame);

        // 启用/禁用弃局按钮
        quitButton.setEnabled(startGame);
        gamePanel.setPlaying(playing = startGame);
    }

    // 初始化块数据
    void initData() {
        Random random = new Random();
        for (int i = 0; i < r / 2; i++) {
            for (int j = 0; j < c; j++) {
                board[i][j] = new Block(i, j, i >= 1 && i <= row && j >= 1 && j <= col ? Images.images[random.nextInt(Images.images.length)] : null);
            }
        }
        for (int i = r / 2; i < r; i++) {
            for (int j = 0; j < c; j++) {
                board[i][j] = new Block(i, j, i >= 1 && i <= row && j >= 1 && j <= col ? board[i - row / 2][j].img : null);
            }
        }
        shuffle();
    }

    // 游戏结束
    void gameOver() {
        JOptionPane.showMessageDialog(null, "游戏结束，你的分数为 " + score);
        quit();
    }

    // 弃局
    void quit() {
        prepareUI(false);
        row = INIT_ROW;
        col = INIT_COL;
        r = row + 2;
        c = col + 2;
        WINDOW_WIDTH = Images.basePixels * c + offsetW;
        WINDOW_HEIGHT = Images.basePixels * r + offsetH;
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }

    // 整合判断
    public static boolean match(Block a, Block b, boolean dL) {
        return basicCondition(a, b) && (matchLine(a, b, dL) || matchOneTurn(a, b, dL) || matchTwoTurn(a, b, dL));
    }

    // 判断是否具备消除的基本条件：两个方块不能是同一个坐标；两个方块必须是同种类型；两个方块中不能有任何一个已经消除过的
    public static boolean basicCondition(Block a, Block b) {
        return !a.equalsByPosition(b) && a.img == b.img && !isEmpty(a) && !isEmpty(b);
    }

    // 判断同一直线能否相连
    public static boolean matchLine(Block a, Block b, boolean dL) {
        // 水平
        if (a.x == b.x) {
            int minY = Math.min(a.y, b.y), maxY = Math.max(a.y, b.y);
            for (int i = minY + 1; i < maxY; i++) {
                if (!isEmpty(board[a.x][i])) return false;
            }
            if (dL) drawLine(a, b);
            return true;
        }
        // 垂直
        else if (a.y == b.y) {
            int minX = Math.min(a.x, b.x), maxX = Math.max(a.x, b.x);
            for (int i = minX + 1; i < maxX; i++) {
                if (!isEmpty(board[i][a.y])) return false;
            }
            if (dL) drawLine(a, b);
            return true;
        }
        // 不在水平或垂直上
        return false;
    }

    // 判断 1 折能否相连：拐角点 c1 和 c2 与 a b 点能相连并且拐角点为空
    public static boolean matchOneTurn(Block a, Block b, boolean dL) {
        Block c1 = board[a.x][b.y];
        Block c2 = board[b.x][a.y];
        if (isEmpty(c1) && matchLine(a, c1, false) && matchLine(b, c1, false)) {
            if (dL) {
                drawLine(a, c1);
                drawLine(b, c1);
            }
            return true;
        } else if (isEmpty(c2) && matchLine(a, c2, false) && matchLine(b, c2, false)) {
            if (dL) {
                drawLine(a, c2);
                drawLine(b, c2);
            }
            return true;
        }
        return false;
    }

    // 判断 2 折能否相连：扫描 a 所在的行和列，找一点 c 使之与 a 直线匹配，与 b 1 折匹配；扫描 b 所在的行和列，找一点 c 使之与 b 直线匹配，与 a 1 折匹配
    public static boolean matchTwoTurn(Block a, Block b, boolean dL) {
        // 扫描 a b 所在的行
        for (int i = 1; ; i++) {
            Block[] cs = {
                    a.x - i >= 0 ? board[a.x - i][a.y] : null,
                    a.x + i < r ? board[a.x + i][a.y] : null,
                    a.y - i >= 0 ? board[a.x][a.y - i] : null,
                    a.y + i < c ? board[a.x][a.y + i] : null
            };
            if (isAllNull(cs)) break;
            for (Block c : cs) {
                if (c != null && isEmpty(c) && matchLine(c, a, false) && matchOneTurn(c, b, dL)) {
                    if (dL) drawLine(a, c);
                    return true;
                }
            }
        }
        for (int i = 1; ; i++) {
            Block[] cs = {
                    b.x - i >= 0 ? board[b.x - i][b.y] : null,
                    b.x + i < r ? board[b.x + i][b.y] : null,
                    b.y - i >= 0 ? board[b.x][b.y - i] : null,
                    b.y + i < c ? board[b.x][b.y + i] : null
            };
            if (isAllNull(cs)) break;
            for (Block c : cs) {
                if (c != null && isEmpty(c) && matchLine(c, b, false) && matchOneTurn(c, a, dL)) {
                    if (dL) drawLine(b, c);
                    return true;
                }
            }
        }
//        for (int i = 0; i < c; i++) {
//            Block c1 = board[a.x][i];
//            Block c2 = board[b.x][i];
//            if (i != a.y && isEmpty(c1) && matchLine(c1, a, false) && matchOneTurn(c1, b, dL)) {
//                if (dL) drawLine(a, c1);
//                return true;
//            } else if (i != b.y && isEmpty(c2) && matchLine(c2, b, false) && matchOneTurn(c2, a, dL)) {
//                if (dL) drawLine(a, c2);
//                return true;
//            }
//        }
//        // 扫描 a b 所在的列
//        for (int i = 0; i < r; i++) {
//            Block c1 = board[i][a.y];
//            Block c2 = board[i][b.y];
//            if (i != a.x && isEmpty(c1) && matchLine(c1, a, false) && matchOneTurn(c1, b, dL)) {
//                if (dL) drawLine(a, c1);
//                return true;
//            } else if (i != b.x && isEmpty(c2) && matchLine(c2, b, false) && matchOneTurn(c2, a, dL)) {
//                if (dL) drawLine(b, c2);
//                return true;
//            }
//        }
        // 不存在这样的 c 点
        return false;
    }

    // 判断格子是否为空或已经被消除
    public static boolean isEmpty(Block c) {
        return c.img == null;
    }

    // 在两个方块之间连线
    public static void drawLine(Block a, Block b) {
        Graphics2D g2d = ((Graphics2D) gamePanel.getGraphics());
        g2d.setColor(Color.red);
        // 设置画笔粗细
        g2d.setStroke(new BasicStroke(5f));
        g2d.drawLine((int) ((a.y + 0.5) * Images.basePixels),
                (int) ((a.x + 0.5) * Images.basePixels),
                (int) ((b.y + 0.5) * Images.basePixels),
                (int) ((b.x + 0.5) * Images.basePixels));
    }

    // 打乱棋盘
    public static void shuffle() {
        Random random = new Random();
        int x2 = 1, y2 = 1;
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= col; j++) {
                if (!isEmpty(board[i][j])) {
                    x2 = i;
                    y2 = j;
                    break;
                }
            }
        }
        // 洗牌洗到有解为止
        do {
            for (int i = 0; i < row * col; i++) {
                int x1 = random.nextInt(row) + 1;
                int y1 = random.nextInt(col) + 1;
                if (isEmpty(board[x1][y1])) continue;
                swap(board, x1, y1, x2, y2);
            }
            for (int i = 1; i <= row; i++) {
                for (int j = 1; j <= col; j++) {
                    if (!isEmpty(board[i][j])) {
                        board[i][j].x = i;
                        board[i][j].y = j;
                    }
                }
            }
        } while (!hasSolution(false));
    }

    // 交换二维数组元素
    public static void swap(Block[][] board, int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2) return;
        Block temp = board[x1][y1];
        board[x1][y1] = board[x2][y2];
        board[x2][y2] = temp;
    }

    // 根据关卡移动
    public static void moveBlock(Block a, Block b) {
        switch (level % Level.name.length) {
            // 向下
            case Level.DOWN:
                moveDown(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = board[b.x + 1][b.y];
                moveDown(b);
                break;
            // 向左
            case Level.LEFT:
                moveLeft(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = board[b.x][b.y - 1];
                moveLeft(b);
                break;
            // 向上
            case Level.UP:
                moveUp(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = board[b.x - 1][b.y];
                moveUp(b);
                break;
            // 向右
            case Level.RIGHT:
                moveRight(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = board[b.x][b.y + 1];
                moveRight(b);
                break;
            // 上下分离
            case Level.UP_AND_DOWN:
                moveUpAndDown(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.x <= row / 2 ? board[b.x - 1][b.y] : board[b.x + 1][b.y];
                moveUpAndDown(b);
                break;
            // 左右分离
            case Level.LEFT_AND_RIGHT:
                moveLeftAndRight(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.y <= col / 2 ? board[b.x][b.y - 1] : board[b.x][b.y + 1];
                moveLeftAndRight(b);
                break;
            // 上下集中
            case Level.UP_AND_DOWN_GATHER:
                moveUpAndDownGather(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.x <= row / 2 ? board[b.x + 1][b.y] : board[b.x - 1][b.y];
                moveUpAndDownGather(b);
                break;
            // 左右集中
            case Level.LEFT_AND_RIGHT_GATHER:
                moveLeftAndRightGather(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.y <= col / 2 ? board[b.x][b.y + 1] : board[b.x][b.y - 1];
                moveLeftAndRightGather(b);
                break;
            // 上左下右
            case Level.UP_LEFT_DOWN_RIGHT:
                moveUpLeftDownRight(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.x <= row / 2 ? board[b.x][b.y - 1] : board[b.x][b.y + 1];
                moveUpLeftDownRight(b);
                break;
            // 左下右上
            case Level.LEFT_DOWN_RIGHT_UP:
                moveLeftDownRightUp(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.y <= col / 2 ? board[b.x + 1][b.y] : board[b.x - 1][b.y];
                moveLeftDownRightUp(b);
                break;
            // 向外扩散
            case Level.OUTWARD_SPREAD:
                // 先上下分离
                moveUpAndDown(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.x <= row / 2 ? board[b.x - 1][b.y] : board[b.x + 1][b.y];
                moveUpAndDown(b);
                // 再对所有空出来的位置左右分离，遍历顺序是从内到外
                for (int j = col / 2; j >= 1; j--) {
                    for (int i = 1; i <= row; i++) {
                        if (isEmpty(board[i][j])) moveLeftAndRight(board[i][j]);
                    }
                }
                for (int j = col / 2 + 1; j <= col; j++) {
                    for (int i = 1; i <= row; i++) {
                        if (isEmpty(board[i][j])) moveLeftAndRight(board[i][j]);
                    }
                }
                break;
            // 向内集中
            case Level.INWARD_GATHER:
                // 先左右居中
                moveLeftAndRightGather(a);
                // b 在 a 消除后移动了，同时 b 变换位置
                if (!isEmpty(b)) b = a.y <= col / 2 ? board[b.x][b.y + 1] : board[b.x][b.y - 1];
                moveLeftAndRightGather(b);
                // 再对所有空出来的位置上下集中，注意遍历顺序是从外到内
                for (int i = 1; i <= row / 2; i++) {
                    for (int j = 1; j <= col; j++) {
                        if (isEmpty(board[i][j])) moveDown(board[i][j]);
                    }
                }
                for (int i = row; i > row / 2; i--) {
                    for (int j = 1; j <= col; j++) {
                        if (isEmpty(board[i][j])) moveUp(board[i][j]);
                    }
                }
                break;
        }
    }

    // 向下
    static void moveDown(Block b) {
        for (int i = b.x; i >= 1; i--) {
            board[i][b.y].img = board[i - 1][b.y].img;
        }
    }

    // 向左
    static void moveLeft(Block b) {
        for (int i = b.y; i <= col; i++) {
            board[b.x][i].img = board[b.x][i + 1].img;
        }
    }

    // 向上
    static void moveUp(Block b) {
        for (int i = b.x; i <= row; i++) {
            board[i][b.y].img = board[i + 1][b.y].img;
        }
    }

    // 向右
    static void moveRight(Block b) {
        for (int i = b.y; i >= 1; i--) {
            board[b.x][i].img = board[b.x][i - 1].img;
        }
    }

    // 上下分离
    static void moveUpAndDown(Block b) {
        // 向上
        if (b.x <= row / 2) {
            for (int i = b.x; i < row / 2; i++) {
                board[i][b.y].img = board[i + 1][b.y].img;
            }
            board[row / 2][b.y].img = null;
        }
        // 向下
        else {
            for (int i = b.x; i > row / 2 + 1; i--) {
                board[i][b.y].img = board[i - 1][b.y].img;
            }
            board[row / 2 + 1][b.y].img = null;
        }
    }

    // 左右分离
    static void moveLeftAndRight(Block b) {
        // 向左
        if (b.y <= col / 2) {
            for (int i = b.y; i < col / 2; i++) {
                board[b.x][i].img = board[b.x][i + 1].img;
            }
            board[b.x][col / 2].img = null;
        }
        // 向右
        else {
            for (int i = b.y; i > col / 2 + 1; i--) {
                board[b.x][i].img = board[b.x][i - 1].img;
            }
            board[b.x][col / 2 + 1].img = null;
        }
    }

    // 上下集中
    static void moveUpAndDownGather(Block b) {
        // 向下
        if (b.x <= row / 2) moveDown(b);
            // 向上
        else moveUp(b);
    }

    // 左右集中
    static void moveLeftAndRightGather(Block b) {
        // 向右
        if (b.y <= col / 2) moveRight(b);
            // 向左
        else moveLeft(b);
    }

    // 上左下右
    static void moveUpLeftDownRight(Block b) {
        // 向左
        if (b.x <= row / 2) moveLeft(b);
            // 向右
        else moveRight(b);
    }

    // 左下右上
    static void moveLeftDownRightUp(Block b) {
        // 向下
        if (b.y <= col / 2) moveDown(b);
            // 向上
        else moveUp(b);
    }

    // 判断当前棋盘是否有解
    static boolean hasSolution(boolean dL) {
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= col; j++) {
                if (!isEmpty(board[i][j])) {
                    for (int k = i; k <= row; k++) {
                        for (int l = 0; l <= col; l++) {
                            if (match(board[k][l], board[i][j], false)) {
                                // 提示时显示边框
                                if (dL) {
                                    Graphics2D g2d = ((Graphics2D) gamePanel.getGraphics());
                                    g2d.setColor(Color.green);
                                    g2d.setStroke(new BasicStroke(5f));
                                    g2d.drawRect(board[k][l].y * Images.basePixels, board[k][l].x * Images.basePixels, Images.basePixels, Images.basePixels);
                                    g2d.drawRect(board[i][j].y * Images.basePixels, board[i][j].x * Images.basePixels, Images.basePixels, Images.basePixels);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // 判断是否全为 null
    static boolean isAllNull(Block[] bs) {
        for (Block b : bs) {
            if (b != null) return false;
        }
        return true;
    }

    // 播放音乐
    static void playMusic() {
        Random random = new Random();
        musicThread = new Thread(() -> {
            try {
                while (true) {
                    // 获得音频输入流
                    AudioInputStream ais = AudioSystem.getAudioInputStream(new File(Music.list[random.nextInt(Music.totalNum)]));
                    // 获得音频格式
                    AudioFormat format = ais.getFormat();
                    // 创建关于 format 的 SourceDataLine(扬声器的数据流) 的 Info 对象
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    // 根据 Info 获得 SourceDataLine
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                    // 注意：也可以直接用 format 创建 SourceDataLine
                    // SourceDataLine line = AudioSystem.getSourceDataLine(ais.getFormat());

                    // 打开关于 format 的扬声器管道的入口端(format 参数可以不要)
                    line.open(format);
                    // 打开出口端
                    line.start();

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while (true) {
                        bytesRead = ais.read(buffer);
                        if (bytesRead <= 0)
                            break;
                        // 线程被打断
                        if (musicThread.isInterrupted()) break;
                        line.write(buffer, 0, bytesRead);
                    }
                    // 将管道的数据流放空
                    line.drain();
                    // 关闭管道
                    line.close();
                }
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
        musicThread.start();
    }

    // 停止音乐
    static void stopMusic() {
        musicThread.stop();
    }

    // 播放音效
    static void playSound(String filename) {
        soundThread = new Thread(() -> {
            try {
                // 获得音频输入流
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
                // 获得音频格式
                AudioFormat format = ais.getFormat();
                // 创建关于 format 的 SourceDataLine(扬声器的数据流) 的 Info 对象
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                // 根据 Info 获得 SourceDataLine
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                // 注意：也可以直接用 format 创建 SourceDataLine
                // SourceDataLine line = AudioSystem.getSourceDataLine(ais.getFormat());

                // 打开关于 format 的扬声器管道的入口端(format 参数可以不要)
                line.open(format);
                // 打开出口端
                line.start();

                byte[] buffer = new byte[1024];
                int bytesRead;
                while (true) {
                    // 线程被打断
                    if (soundThread.isInterrupted()) return;
                    bytesRead = ais.read(buffer);
                    if (bytesRead <= 0)
                        break;
                    line.write(buffer, 0, bytesRead);
                }
                // 将管道的数据流放空
                line.drain();
                // 关闭管道
                line.close();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        });
        soundThread.start();
    }
}
