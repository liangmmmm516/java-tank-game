package com.BattleTank.game;

import java.awt.*;
import static com.BattleTank.util.Constant.*;

/**
 * 玩家坦克类
 * 核心作用：玩家可操控的游戏主体，具备移动、方向控制、血量管理能力
 * 移动逻辑：支持上下左右移动，撞墙/边界检测，方向与移动绑定
 */
public class Tank {
    private int x, y;
    // 坦克尺寸
    private final int width = 30;
    private final int height = 30;
    // 坦克移动速度：4像素/帧
    private final int speed = 4;
    // 坦克颜色
    private Color color;
    // 坦克血量：初始3点，被敌方碰撞扣1点，血量为0则死亡
    private int hp = 3;

    // 坦克当前朝向
    private int dir = 0;

//    初始化坦克位置,朝向,颜色
    public Tank(int x, int y, int dir, Color color) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.color = color;
    }

//    ===============向上移动==============
    public void moveUp() {
        int nextY = y - speed; // 下一步Y坐标
        // 撞墙检测：未撞墙则移动
        if (!GamePanel.instance.isTankHitWall(x, nextY, width, height)) {
            y = nextY;
        }
        dir = 0; // 更新朝向为上
        checkBorder(); // 边界检测
    }

//    ===============向下移动==============
    public void moveDown() {
        int nextY = y + speed;
        if (!GamePanel.instance.isTankHitWall(x, nextY, width, height)) {
            y = nextY;
        }
        dir = 1; // 更新朝向为下
        checkBorder();
    }

//    ================向左移动=============
    public void moveLeft() {
        int nextX = x - speed;
        if (!GamePanel.instance.isTankHitWall(nextX, y, width, height)) {
            x = nextX;
        }
        dir = 2; // 更新朝向为左
        checkBorder();
    }

//    ================向右移动=============
    public void moveRight() {
        int nextX = x + speed;
        if (!GamePanel.instance.isTankHitWall(nextX, y, width, height)) {
            x = nextX;
        }
        dir = 3; // 更新朝向为右
        checkBorder();
    }

//    ===============获取坦克当前朝向===========
    public int getCurrentDirection() {
        return dir;
    }

//    =================边界检测==============
    private void checkBorder() {
        if (x < 0) x = 0; // 左边界
        if (x > FRAME_WIDTH - width) x = FRAME_WIDTH - width; // 右边界
        if (y < 0) y = 0; // 上边界
        if (y > FRAME_HEIGHT - height) y = FRAME_HEIGHT - height; // 下边界
    }

//    ==============绘制玩家坦克=============
    public void draw(Graphics g) {
        long now = System.currentTimeMillis();
        if (now - GamePanel.instance.getHurtTime() < GamePanel.HURT_INTERVAL / 2){
            if ((now / 100) % 2 == 0){
                g.setColor(color);
                g.fillRect(x, y, width, height);
            }
        } else {
            g.setColor(color);
            g.fillRect(x, y, width, height);
        }
    }

//    状态管理+碰撞检测
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHp() { return hp; } // 获取当前血量
    public void reduceHp() { hp--; } // 扣血（被敌方碰撞时调用）
    public boolean isDead() { return hp <= 0; } // 判断是否死亡
}