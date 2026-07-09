package com.BattleTank.game;

import java.awt.*;

/**
 * 玩家大本营/基地类
 * 核心作用：游戏核心保护目标，被子弹击中则游戏失败
 * 绘制样式：黄色正方形+红色边框+中心"基地"文字，被毁后不再绘制
 */
public class Home {
    private int x;
    private int y;
    // 基地尺寸
    public static final int HOME_SIZE = 50;
    // 基地存活状态
    public boolean isLive = true;

//    ============初始化基地位置==============
    public Home(int x, int y){
        this.x = x;
        this.y = y;
    }

//    =============绘制基地到游戏面板=============
    public void draw(Graphics g){
        if(!isLive) return; // 被毁则不绘制

        // 绘制基地主体
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, HOME_SIZE, HOME_SIZE);
        // 绘制红色边框
        g.setColor(Color.RED);
        g.drawRect(x, y, HOME_SIZE, HOME_SIZE);
        // 绘制中心文字
        g.setColor(Color.BLACK);
        g.drawString("基地", x + 10, y + 30);
    }

//    ====================碰撞检测==================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return HOME_SIZE; }
    public int getHeight() { return HOME_SIZE; }
}