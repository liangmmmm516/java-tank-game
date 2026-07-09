package com.BattleTank.game;

import java.awt.*;
import java.util.Random;
import static com.BattleTank.util.Constant.*;

/**
 * 敌方坦克类（AI控制）
 * 核心作用：游戏中的敌对单位，具备自动移动、避墙、边界限制能力
 * AI逻辑：随机方向移动，撞墙/边界后换方向，小概率随机换向（模拟智能）
 */
public class EnemyTank {
    private int x;
    private int y;
    // 敌方坦克尺寸：固定30x30像素
    private final int width = 30;
    private final int height = 30;
    // 敌方坦克移动速度
    private int speed;
    // 移动方向
    private int direction;
    // 存活状态
    public boolean isLive = true;
    // 随机数对象：用于方向随机切换
    private final Random random = new Random();

    // 敌方射击冷却
    private long lastFireTime;
    private static final int FIRE_INTERVAL = 1500;

//    ==============初始化敌方坦克位置和随机初始方向===============
    public EnemyTank(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = random.nextInt(4); // 随机初始方向（0-3）
    }

//    移动逻辑,执行流程：
//             1. 计算下一步移动坐标
//             2. 检测是否撞墙：撞墙则换方向，不移动；未撞墙则更新坐标
//             3. 边界限制：超出窗口则回弹并换方向
//             4. 小概率随机换方向：模拟智能移动，避免直线走到底
    public void aiMove() {
//        计算下一步移动坐标
        int nextX = x; // 下一步X坐标
        int nextY = y; // 下一步Y坐标

//        根据当前方向更新下一步坐标
        switch (direction) {
            case 0: nextY -= speed; break;
            case 1: nextY += speed; break;
            case 2: nextX -= speed; break;
            case 3: nextX += speed; break;
        }

//        撞墙检测
        // 调用GamePanel的撞墙检测方法：未撞墙则移动，撞墙则换方向
        if (!GamePanel.instance.isTankHitWall(nextX, nextY, width, height)) {
            x = nextX;
            y = nextY;
        } else {
            direction = random.nextInt(4); // 撞墙：随机换方向
        }

//        边界限制
        checkBorder();

//        随机换方向
        if (random.nextInt(100) < 2) {
            direction = random.nextInt(4);
        }

//        射击
        autoFire();
    }

//    ================发射子弹===============
    private void autoFire() {
        long now = System.currentTimeMillis();
        if (now - lastFireTime > FIRE_INTERVAL) {
            Bullet bullet = new Bullet(
                x + width/2 - 4,
                y + height/2 - 4,
                direction,
                Bullet.TYPE_ENEMY // 敌方子弹
            );
            GamePanel.instance.addBullet(bullet);
            lastFireTime = now;
        }
    }

//    ================边界控制===============
    private void checkBorder() {
        if (x < 0) { // 左边界
            x = 0; // 回弹到0
            direction = 3; // 换向右
        }
        if (x > FRAME_WIDTH - width) {
            x = FRAME_WIDTH - width;
            direction = 2;
        }
        if (y < 0) {
            y = 0;
            direction = 1;
        }
        if (y > FRAME_HEIGHT - height) {
            y = FRAME_HEIGHT - height;
            direction = 0;
        }
    }

//    ===============绘制敌方坦克===============
    public void draw(Graphics g) {
        g.setColor(Color.RED); // 颜色为红色
        g.fillRect(x, y, width, height);
    }

    public void setSpeed(int speed){
        this.speed = speed;
    }

//    =====================碰撞检测====================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}