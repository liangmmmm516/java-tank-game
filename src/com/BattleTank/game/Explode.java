package com.BattleTank.game;

import java.awt.*;

/**
 * 爆炸动画类
 * 核心作用：坦克/子弹/基地被击中时播放的视觉效果，支持对象池复用
 * 动画逻辑：逐帧放大的圆形渐变效果，播放完后标记为死亡，等待回收
 */
public class Explode {
    // 爆炸中心X坐标
    private int x, y;
    // 当前动画帧（0~MAX_FRAME-1）：帧越大，爆炸范围越大
    private int index = 0;
    // 爆炸存活状态
    public boolean isLive = true;

    // 爆炸总帧数：固定10帧
    public static final int MAX_FRAME = 10;
    // 爆炸基础尺寸：30像素
    private int size = 30;

//    ===========初始化爆炸中心坐标============
    public Explode(int x, int y) {
        this.x = x;
        this.y = y;
    }

//    ===============绘制爆炸动画=============
    public void draw(Graphics g) {
        if (!isLive) return; // 已死亡则不绘制

        // 当前帧的爆炸尺寸：索引*3（逐帧扩大）
        int currentSize = index * 3;
        // 设置爆炸颜色：红255，绿从200逐帧减20（200→0），蓝0（橙黄→暗红）
        g.setColor(new Color(255, 200 - index * 20, 0));
        // 绘制圆形爆炸：中心偏移=
        g.fillOval(x + size/2 - currentSize/2, y + size/2 - currentSize/2, currentSize, currentSize);

        // 帧索引+1，播放下一帧
        index++;
        // 播放完成（索引≥总帧数）：标记为死亡
        if (index >= MAX_FRAME) {
            isLive = false;
        }
    }

//    ===============重置爆炸状态==============
    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        index = 0; // 重置帧索引为0
        isLive = true; // 标记为存活
    }
}