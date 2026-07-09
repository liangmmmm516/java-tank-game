package com.BattleTank.game;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static com.BattleTank.util.Constant.*;

public class GameFrame extends JFrame implements Runnable{
    private final GamePanel gamePanel;

    public GameFrame(){
        initFrame();
        // 初始化面板，添加到窗口里
        gamePanel = new GamePanel();
        add(gamePanel);
        initEventListener();
        new Thread(this).start();
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow(); // 替代requestFocus，更稳定
        });
    }

    public void initFrame(){
        setTitle(GAME_TITLE);
        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void initEventListener(){
        // 按键监听绑定到GamePanel上
        gamePanel.addKeyListener(new KeyAdapter() {
            // 按键按下
            @Override
            public void keyPressed(KeyEvent e) {
                gamePanel.keyPress(e.getKeyCode());
            }
            // 按键松开
            @Override
            public void keyReleased(KeyEvent e) {
                gamePanel.keyRelease(e.getKeyCode());
            }
        });
    }

    @Override
    public void run() {
        while (true){
            gamePanel.repaint();
            try {
                Thread.sleep(REPAINT_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}