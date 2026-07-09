package com.BattleTank.game;

import java.awt.*;

/**
 * 墙体砖块类
 * 核心作用：作为游戏中的障碍物，阻挡坦克移动、阻挡子弹穿透，是不可破坏的静态障碍物
 * 每个墙体为固定大小的正方形砖块，具备绘制和碰撞检测所需的坐标/尺寸属性
 */
public class Wall {
    // 墙体左上角的X坐标（像素）
    private int x;
    // 墙体左上角的Y坐标（像素）
    private int y;
    // 墙体统一尺寸：所有砖块都是40x40像素（常量，全局可访问）
    public static final int WALL_SIZE = 40;

//    ==========初始化墙体的位置=========

    public Wall(int x, int y) {
        this.x = x;
        this.y = y;
    }

//    =========绘制墙体到游戏面板=========

    public void draw(Graphics g) {
        // 设置砖块主体颜色为土黄色（RGB：139,69,19）
        g.setColor(new Color(139, 69, 19));
        // 填充矩形：绘制砖块主体
        g.fillRect(x, y, WALL_SIZE, WALL_SIZE);
        // 设置边框颜色为白色
        g.setColor(Color.WHITE);
        // 绘制矩形边框：区分相邻砖块
        g.drawRect(x, y, WALL_SIZE, WALL_SIZE);
    }

    // ==========碰撞检测==========
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return WALL_SIZE;
    }

    public int getHeight() {
        return WALL_SIZE;
    }
}