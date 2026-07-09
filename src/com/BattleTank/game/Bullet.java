package com.BattleTank.game;

import static com.BattleTank.util.Constant.*;

/**
 * 子弹类：新增类型区分 玩家子弹/敌方子弹
 */
public class Bullet {
    private int x, y;
    private int width = 8;
    private int height = 8;
    private int speed = 10;
    private int direction;
    public boolean isLive = true;

    // ===================== 新增：子弹类型 =====================
    public static final int TYPE_PLAYER = 0; // 玩家子弹
    public static final int TYPE_ENEMY = 1;  // 敌方子弹
    private int type; // 当前子弹类型

    // 添加子弹类型
    public Bullet(int x, int y, int direction, int type) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.type = type;
    }

    // 子弹移动
    public void move() {
        switch (direction) {
            case 0: y -= speed; break;
            case 1: y += speed; break;
            case 2: x -= speed; break;
            case 3: x += speed; break;
        }
        checkBorder();
    }

    // 出界销毁
    private void checkBorder() {
        if (x < 0 || x > FRAME_WIDTH || y < 0 || y > FRAME_HEIGHT) {
            isLive = false;
        }
    }

    // 绘制
    public void draw(java.awt.Graphics g) {
        if (!isLive) return;
        // 玩家子弹黄色，敌方子弹红色
        g.setColor(type == TYPE_PLAYER ? java.awt.Color.YELLOW : java.awt.Color.RED);
        g.fillRect(x, y, width, height);
    }

    //Getter
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getType() { return type; }
}