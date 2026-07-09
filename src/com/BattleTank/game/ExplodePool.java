package com.BattleTank.game;

import java.util.ArrayList;
import java.util.List;

/**
 * 爆炸对象池类
 * 核心作用：预先创建爆炸对象，重复复用，避免频繁new/GC导致游戏卡顿
 * 设计模式：对象池模式（享元模式的一种），减少内存开销和GC频率
 */
public class ExplodePool {
    // 对象池默认大小：预先创建10个爆炸对象
    private static final int DEFAULT_POOL_SIZE = 10;
    // 池子容器：存储可用的爆炸对象（初始为空，静态初始化时填充）
    private static List<Explode> pool = new ArrayList<>();

    // 静态初始化块：程序启动时预先创建10个爆炸对象，放入池子
    static {
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            pool.add(new Explode(0, 0)); // 初始坐标
        }
    }

//    对象池获取一个爆炸对象,获取逻辑：
//             1. 池子有可用对象 → 移除并返回第一个对象
//             2. 池子无可用对象 → 新建一个返回
    public static Explode get() {
        // 池子非空：取出第一个对象
        if (pool.size() > 0) {
            return pool.remove(0);
        } else {
            // 池子为空：新建对象
            return new Explode(0, 0);
        }
    }

//    ============回收爆炸对象到池子==============
    public static void recycle(Explode explode) {
        // 池子容量限制：不超过20个
        if (pool.size() < DEFAULT_POOL_SIZE * 2) {
            pool.add(explode); // 放回池子，等待复用
        }
    }
}