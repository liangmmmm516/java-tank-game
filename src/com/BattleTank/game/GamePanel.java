package com.BattleTank.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static com.BattleTank.util.Constant.*;

/**
 * 游戏核心面板类（JPanel子类）
 * 核心作用：承载游戏所有逻辑（初始化、绘制、输入监听、碰撞检测、AI逻辑等）
 */
public class GamePanel extends JPanel {
    // 游戏状态：对应菜单/运行/胜利/失败
    public static int gameState;
    // 菜单选中索引
    private int menuIndex;
    // 玩家坦克对象
    private Tank playerTank;
    // 玩家基地对象
    private Home home;


    // 子弹列表：存储所有存活的子弹（玩家+敌方，此处暂只有玩家子弹）
    private List<Bullet> bulletList;
    // 上次发射子弹的时间
    private long lastFireTime;
    // 发射间隔
    private static final int FIRE_INTERVAL = 220;


    // 敌方坦克列表
    private List<EnemyTank> enemyList;
    // 控制生成间隔
    private long lastEnemyTime;
    // 敌方坦克速度
    private int ENEMY_SPEED;


    // 关卡
    private int currentLevel = 1;
    // 即将进入的关卡
    private int transitionLevel;
    // 过场开始
    private long transitionStartTime;
    // 过场持续
    private static final int TRANSITION_DURATION = 2000;
    // 已击杀数
    private int enemyKilledCount;
    // 需要击杀
    private int enemyNeedToKill;

    // 难度参数
    private int MAX_ENEMY_COUNT;
    // 敌人刷新间隔
    private int ENEMY_SPAWN_INTERVAL;


    // 胜利条件：需要击杀的敌方坦克总数
    private static final int TOTAL_ENEMY_TO_WIN = 6;
    // 玩家受伤时间,控制受伤无敌帧
    private long hurtTime;
    // 受伤间隔，避免连续扣血
    public static final int HURT_INTERVAL = 1000;

    // 方向按键标记
    private boolean moveUp, moveDown, moveLeft, moveRight;

    // 爆炸效果列表：存储所有正在播放的爆炸动画
    private List<Explode> explodeList = new ArrayList<>();
    // 墙体列表：存储所有游戏中的墙体砖块
    private List<Wall> wallList;

    // 游戏面板单例
    public static GamePanel instance;

//    =========初始化游戏面板=============
    public GamePanel() {
        instance = this; // 初始化单例
        initGame(); // 初始化游戏核心数据
        setFocusable(true); // 面板获取焦点，才能监听按键
    }

//    =========初始化游戏所有核心数据==========
    public void initGame() {
        gameState = START_MENU; // 默认进入开始菜单
        menuIndex = 0; // 菜单默认选中第一个选项（开始游戏）
        startGameLoop(); // 游戏线程
        currentLevel = 1;
        enemyKilledCount = 0;
        playerTank = new Tank(FRAME_WIDTH / 2, FRAME_HEIGHT / 2, 0, Color.YELLOW);         // 初始化玩家坦克

        bulletList = new ArrayList<>(); // 初始化子弹列表
        lastFireTime = 0; // 重置发射时间

        enemyList = new ArrayList<>(); // 初始化敌方坦克列表
        lastEnemyTime = System.currentTimeMillis(); // 初始化敌方生成时间
        hurtTime = 0; // 重置受伤时间
        moveUp = moveDown = moveLeft = moveRight = false; // 重置按键状态
        explodeList.clear(); // 清空爆炸效果

        wallList = new ArrayList<>(); // 初始化墙体列表
        initMap(); // 初始化地图
        home = new Home(FRAME_WIDTH / 2 - Home.HOME_SIZE / 2, FRAME_HEIGHT - 80);         // 初始化基地

        loadLevel(currentLevel); // 加载第一关难度
    }

//    ============加载关卡难度=============
    private void loadLevel(int level){
        if (level == 1){
            enemyNeedToKill = 5;
            MAX_ENEMY_COUNT = 2;
            ENEMY_SPAWN_INTERVAL = 3800;
            ENEMY_SPEED = 2;
        }else if (level == 2) {
            enemyNeedToKill = 8;
            MAX_ENEMY_COUNT = 3;
            ENEMY_SPAWN_INTERVAL = 2800;
            ENEMY_SPEED = 3;
        } else if (level == 3) {
            enemyNeedToKill = 12;
            MAX_ENEMY_COUNT = 4;
            ENEMY_SPAWN_INTERVAL = 1800;
            ENEMY_SPEED = 4;
        }
        enemyKilledCount = 0;  // 重置击杀数
        enemyList.clear();      // 清掉上一关敌人
        lastEnemyTime = System.currentTimeMillis();
    }

//    ==============进入下一关=============
    private void nextLevel(){
        if (currentLevel < 3){
            currentLevel ++;
            loadLevel(currentLevel);
        }else {
            gameState = START_WIN; // 游戏胜利
        }
    }

//    ===========初始化游戏地图=============
    private void initMap() {
        // 生成横向墙体：X从4*40到14*40，Y=300（一行砖块）
        for (int i = 4; i <= 14; i++) wallList.add(new Wall(i * Wall.WALL_SIZE, 300));
        // 生成零散墙体（竖线/单点）
        wallList.add(new Wall(200, 100));
        wallList.add(new Wall(200, 140));
        wallList.add(new Wall(200, 180));
        wallList.add(new Wall(600, 400));
        wallList.add(new Wall(600, 440));
        wallList.add(new Wall(600, 480));
        wallList.add(new Wall(400, 150));
        wallList.add(new Wall(150, 500));
        wallList.add(new Wall(700, 220));
    }

//    =================坦克撞墙检测==============
    public boolean isTankHitWall(int nextX, int nextY, int w, int h) {
        // 检测与所有墙体的碰撞
        for (Wall wall : wallList)
            if (rectCollision(nextX, nextY, w, h, wall.getX(), wall.getY(), wall.getWidth(), wall.getHeight()))
                return true;
        // 检测与基地的碰撞
        if (rectCollision(nextX, nextY, w, h, home.getX(), home.getY(), home.getWidth(), home.getHeight()))
            return true;
        return false;
    }

//    ============播放爆炸动画=============
    private void playExplode(int x, int y) {
        Explode explode = ExplodePool.get(); // 从对象池获取爆炸对象
        explode.reset(x, y); // 重置爆炸坐标和状态
        explodeList.add(explode); // 加入爆炸列表等待绘制
    }

//    =============更新并绘制爆炸动画=============
    private void updateExplode(Graphics g) {
        // 倒序遍历：避免删除元素导致的索引异常
        for (int i = explodeList.size() - 1; i >= 0; i--) {
            Explode e = explodeList.get(i);
            if (e.isLive) {
                e.draw(g); // 绘制存活的爆炸
            } else {
                explodeList.remove(i); // 移除死亡的爆炸
                ExplodePool.recycle(e); // 回收爆炸对象到对象池
            }
        }
    }

//    =============绘制开始菜单=================
    private void drawMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight()); // 黑色背景
        int y = FRAME_HEIGHT / 3; // 菜单起始Y坐标（屏幕上1/3处）
        int dis = 60; // 菜单选项间距
        g.setFont(Game_Font); // 设置游戏字体
        FontMetrics metrics = g.getFontMetrics(); // 获取字体度量（用于居中）
        // 遍历所有菜单选项
        for (int i = 0; i < MENUS.length; i++) {
            // 选中项为蓝色，其他为白色
            g.setColor(i == menuIndex ? Color.BLUE : Color.WHITE);
            // 计算文字居中X坐标：(面板宽度 - 文字宽度)/2
            int x = (getWidth() - metrics.stringWidth(MENUS[i])) / 2;
            g.drawString(MENUS[i], x, y + dis * i); // 绘制菜单文字
        }
    }

//    绘制游戏运行界面
//    绘制顺序：背景→墙体→玩家坦克→血量/击杀数→子弹→敌方坦克→爆炸→基地

    private void drawRun(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight()); // 黑色背景
        // 绘制所有墙体
        for (Wall wall : wallList) wall.draw(g);
        // 绘制玩家坦克
        playerTank.draw(g);
        // 绘制玩家血量（左上角）
        g.setColor(Color.WHITE);
        g.drawString("血量：" + playerTank.getHp(), 20, 90);
        // 显示关卡
        g.drawString("关卡:" + currentLevel + "/3", 20, 30);
        // 绘制击杀数
        g.drawString("击杀：" + enemyKilledCount + "/" + enemyNeedToKill, 20, 60);
        // 绘制所有存活的子弹
        for (Bullet b : bulletList) if (b.isLive) b.draw(g);
        // 绘制所有存活的敌方坦克
        for (EnemyTank e : enemyList) if (e.isLive) e.draw(g);
        // 更新并绘制爆炸动画
        updateExplode(g);
        // 绘制基地
        home.draw(g);
    }

//    ==============绘制游戏胜利界面================
    private void drawWin(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight()); // 黑色背景
        g.setColor(Color.GREEN); // 胜利文字为绿色
        g.setFont(Game_Font); // 游戏字体
        g.drawString("游戏胜利！",FRAME_WIDTH/2 - 70,FRAME_HEIGHT/2); // 居中显示胜利文字
        g.setColor(Color.WHITE); // 提示文字为白色
        g.drawString("按ESC返回菜单",FRAME_WIDTH/2 - 100,FRAME_HEIGHT/2+60); // 提示文字（胜利文字下方）
    }

//    ===============绘制游戏失败界面==============
    private void drawOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight()); // 黑色背景
        g.setColor(Color.RED); // 失败文字为红色
        g.setFont(Game_Font); // 游戏字体
        g.drawString("游戏结束",FRAME_WIDTH/2 - 70,FRAME_HEIGHT/2); // 居中显示失败文字
        g.setColor(Color.WHITE); // 提示文字为白色
        g.drawString("按ESC返回菜单",FRAME_WIDTH/2 - 100,FRAME_HEIGHT/2+60); // 提示文字（失败文字下方）
    }

//    ==============绘制帮助界面===============
    private void drawHelp(Graphics g) {
        // GamePanel.java
            g.setColor(Color.BLACK);
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(Color.WHITE);
            g.setFont(Game_Font);
            g.drawString("操作说明", FRAME_WIDTH/2 - 60, FRAME_HEIGHT/3);
            g.drawString("W/↑：向上移动", FRAME_WIDTH/2 - 100, FRAME_HEIGHT/3 + 60);
            g.drawString("S/↓：向下移动", FRAME_WIDTH/2 - 100, FRAME_HEIGHT/3 + 120);
            g.drawString("A/←：向左移动", FRAME_WIDTH/2 - 100, FRAME_HEIGHT/3 + 180);
            g.drawString("D/→：向右移动", FRAME_WIDTH/2 - 100, FRAME_HEIGHT/3 + 240);
            g.drawString("空格：发射子弹", FRAME_WIDTH/2 - 100, FRAME_HEIGHT/3 + 300);
            g.drawString("ESC：返回菜单", FRAME_WIDTH/2 - 100, FRAME_HEIGHT/3 + 360);
        }
    /* 绘制关于界面（暂未实现） */
    private void drawAbout(Graphics g) {}

//    ==============更新子弹状态================
    public void addBullet(Bullet bullet){
        bulletList.add(bullet);
    }
    private void updateBullet() {
        // 移动所有存活的子弹
        for (Bullet b : bulletList) if (b.isLive) b.move();
        // 移除死亡的子弹（流式处理，简化代码）
        bulletList.removeIf(b -> !b.isLive);
    }

//    ===================受伤时间===================
    public long getHurtTime(){
        return hurtTime;
    }

//    =================更新敌方坦克状态================
    private void updateEnemy() {
        // 执行所有存活敌方坦克的AI移动
        // 移除死亡的敌方坦克
        // 发射子弹
        enemyList.removeIf(e -> !e.isLive);
        for (EnemyTank e : enemyList) {
            if (e.isLive) {
                e.aiMove();
            }
        }
    }

//    生成敌方坦克
//    生成条件：
//            1. 当前敌方数量 < 最大数量（MAX_ENEMY_COUNT）
//            2. 未达到胜利所需击杀数（TOTAL_ENEMY_TO_WIN）
//            3. 距离上次生成时间 ≥ 生成间隔（ENEMY_SPAWN_INTERVAL）
//            4. 生成位置：随机位置，且与玩家坦克距离≥250像素
    private void spawnEnemy() {
        // 检查生成条件：数量限制 + 胜利条件
        if (enemyList.size() >= MAX_ENEMY_COUNT) return;
        if (enemyKilledCount >= enemyNeedToKill) return;

        // 检查时间间隔
        long now = System.currentTimeMillis();
        if (now - lastEnemyTime >= ENEMY_SPAWN_INTERVAL) {
            Random r = new Random();
            int x, y;
            int maxTry = 10;
            int tryCount = 0;
            // 循环生成随机位置，直到与玩家距离≥250像素
            do {
                x = r.nextInt(FRAME_WIDTH - 60); // X范围：0 ~ 窗口宽度-60（避免贴边）
                y = r.nextInt(FRAME_HEIGHT - 60); // Y范围：0 ~ 窗口高度-60（避免贴边）
                tryCount ++;
            } while (getDistance(x, y) < 250 && tryCount < maxTry); // 距离检测：≥250像素才生效
            EnemyTank enemyTank = new EnemyTank(x,y);
            enemyTank.setSpeed(ENEMY_SPEED);
            enemyList.add(enemyTank);
            lastEnemyTime = now; // 更新上次生成时间
        }
    }

//    计算两点之间的距离,用于敌方生成时，避免与玩家坦克距离过近
    private double getDistance(int x, int y) {
        int px = playerTank.getX(); // 玩家坦克X坐标
        int py = playerTank.getY(); // 玩家坦克Y坐标
        // 勾股定理：√[(x1-x2)² + (y1-y2)²]
        return Math.sqrt(Math.pow(x-px,2) + Math.pow(y-py,2));
    }

//    碰撞检测核心方法
//    检测顺序：
//             1. 子弹 vs 敌方坦克 → 子弹消失,敌方死亡,播放爆炸
//             2. 玩家坦克 vs 敌方坦克 → 玩家扣血（无敌帧保护）,血量为0则游戏失败
//             3. 子弹 vs 墙体 → 子弹消失,播放爆炸
//             4. 子弹 vs 基地 → 子弹消失,基地被毁,游戏失败
// 碰撞检测（修复：区分敌我子弹，杜绝自伤）
private void checkCollision() {
    // 1. 玩家子弹打敌方坦克
    for (Bullet b : bulletList) {
        if (!b.isLive || b.getType() != Bullet.TYPE_PLAYER) continue;
        for (EnemyTank e : enemyList) {
            if (!e.isLive) continue;

            if (rectCollision(
                    b.getX(), b.getY(), b.getWidth(), b.getHeight(),
                    e.getX(), e.getY(), e.getWidth(), e.getHeight()
            )) {
                b.isLive = false;
                e.isLive = false;
                enemyKilledCount++;
                playExplode(e.getX(), e.getY());
            }
        }
    }

    // 2. 敌人撞玩家
    long now = System.currentTimeMillis();
    if (now - hurtTime > HURT_INTERVAL) {
        for (EnemyTank e : enemyList) {
            if (!e.isLive) continue;
            if (rectCollision(
                    playerTank.getX(), playerTank.getY(), playerTank.getWidth(), playerTank.getHeight(),
                    e.getX(), e.getY(), e.getWidth(), e.getHeight()
            )) {
                playerTank.reduceHp();
                hurtTime = now;
                if (playerTank.isDead()) {
                    gameState = START_OVER;
                }
                break;
            }
        }
    }

    // 3. 子弹撞墙
    for (Bullet b : bulletList) {
        if (!b.isLive) continue;
        for (Wall wall : wallList) {
            if (rectCollision(
                    b.getX(), b.getY(), b.getWidth(), b.getHeight(),
                    wall.getX(), wall.getY(), wall.getWidth(), wall.getHeight()
            )) {
                b.isLive = false;
                playExplode(b.getX(), b.getY());
            }
        }
    }

    // 4. 子弹打基地
    for (Bullet b : bulletList) {
        if(!b.isLive || !home.isLive) continue;
        if(rectCollision(
                b.getX(),b.getY(),b.getWidth(),b.getHeight(),
                home.getX(),home.getY(),home.getWidth(),home.getHeight()
        )){
            b.isLive = false;
            home.isLive = false;
            playExplode(home.getX(), home.getY());
            gameState = START_OVER;
        }
    }
}

//    ================矩形碰撞检测==============
    public boolean rectCollision(int x1,int y1,int w1,int h1,int x2,int y2,int w2,int h2) {
        // 矩形1右边界 > 矩形2左边界 && 矩形1左边界 < 矩形2右边界
        // 矩形1下边界 > 矩形2上边界 && 矩形1上边界 < 矩形2下边界
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

//    =============玩家坦克移动控制=============
    private void updatePlayerMove() {
        if (moveUp)    {
            playerTank.moveUp();
        }
        if (moveDown)  {
            playerTank.moveDown();
        }
        if (moveLeft)  {
            playerTank.moveLeft();
        }
        if (moveRight) {
            playerTank.moveRight();
        }
    }

//    重写JPanel的paintComponent方法（游戏核心绘制入口）
//    执行流程：
//            1. 游戏运行状态：更新游戏逻辑（移动,子弹,敌方,碰撞,生成）
//            2. 检查胜利条件：击杀数达标且基地存活 → 游戏胜利
//            3. 根据游戏状态绘制对应界面（菜单/运行/胜利/失败）

    public void startGameLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(16); // 添加休眠，降低CPU占用
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (gameState == START_RUN) {
                    updatePlayerMove();
                    updateBullet();
                    updateEnemy();
                    spawnEnemy();
                    checkCollision();

                    // 修改过关条件检测
                    if (enemyKilledCount >= enemyNeedToKill && home.isLive) {
                        if (currentLevel < 3) {
                            transitionLevel = currentLevel + 1;
                            transitionStartTime = System.currentTimeMillis();
                            gameState = STATE_LEVEL_TRANSITION;
                        } else {
                            gameState = START_WIN;
                        }
                    }
                }  // 添加过场计时与结束处理
                else if (gameState == STATE_LEVEL_TRANSITION){
                    if (System.currentTimeMillis() - transitionStartTime >= TRANSITION_DURATION){
                        currentLevel = transitionLevel;
                        loadLevel(currentLevel);
                        playerTank = new Tank(FRAME_WIDTH / 2 ,FRAME_HEIGHT / 2,0,Color.YELLOW);
                        bulletList.clear();
                        moveUp = moveDown = moveLeft = moveRight = false;
                        hurtTime = 0;
                        gameState = START_RUN;
                    }
                }
            }
        }).start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 调用父类方法，清空面板

        // 根据游戏状态绘制对应界面
        switch (gameState) {
            case START_MENU:
                drawMenu(g);
                break;
            case START_RUN:
                drawRun(g);
                break;
            case START_HELP:
                drawHelp(g);
                break;
            case START_ABOUT:
                drawAbout(g);
                break;
            case START_OVER:
                drawOver(g);
                break;
            case START_WIN:
                drawWin(g);
                break;
            case STATE_LEVEL_TRANSITION:
                drawLevelTransition(g);
                break;    
        }
    }
    
//    过场动画绘制
    private void drawLevelTransition(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(Game_Font);
        String text = "第 " + transitionLevel + " 关";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        g.drawString(text, x, y);
    }

//    按键按下监听
//    1. 运行状态：移动/发射/返回菜单
//    2. 菜单状态：上下选择/确认/退出
//    3. 胜利/失败状态：返回菜单
    public void  keyPress(int keyCode) {
        if (gameState == START_RUN) { // 游戏运行状态
            switch (keyCode) {
                // 向上移动
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    moveUp = true;
                    break;

                // 向下移动
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    moveDown = true;
                    break;

                // 向左移动
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    moveLeft = true;
                    break;

                // 向右移动
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    moveRight = true;
                    break;

                // 返回菜单
                case KeyEvent.VK_ESCAPE:
                    gameState = START_MENU;
                    break;

                case KeyEvent.VK_SPACE: // 空格键发射子弹
                    long now = System.currentTimeMillis();
                    if (now - lastFireTime >= FIRE_INTERVAL) {
                        // 生成玩家子弹
                        Bullet bullet = new Bullet(
                                playerTank.getX() + playerTank.getWidth()/2 - 4,
                                playerTank.getY() + playerTank.getHeight()/2 - 4,
                                playerTank.getCurrentDirection(),
                                Bullet.TYPE_PLAYER
                        );
                        bulletList.add(bullet);
                        lastFireTime = now; // 更新发射时间
                    }
                    break;
            }
        } else if (gameState == START_MENU) { // 菜单状态
            switch (keyCode) {
                // 上选菜单
                case KeyEvent.VK_UP:
                    menuIndex--;
                    if(menuIndex<0) menuIndex=MENUS.length-1; // 循环选择（到顶则切到底）
                    break;
                // 下选菜单
                case KeyEvent.VK_DOWN:
                    menuIndex++;
                    if(menuIndex>=MENUS.length) menuIndex=0; // 循环选择（到底则切到顶）
                    break;
                // 确认选择（回车键）
                case KeyEvent.VK_ENTER:
                    if(menuIndex==0) gameState=START_RUN; // 开始游戏
                    else if(menuIndex==1) gameState=START_HELP; // 帮助界面
                    else if(menuIndex==2) gameState=START_ABOUT; // 关于界面
                    else if(menuIndex==3) System.exit(0); // 退出游戏
                    break;
            }
        } else if (gameState == START_OVER || gameState == START_WIN) { // 胜利/失败状态
            if (keyCode == KeyEvent.VK_ESCAPE) { // ESC键返回菜单
                initGame(); // 重置游戏数据
                gameState = START_MENU; // 切换到菜单状态
            }
        } else if (gameState == STATE_LEVEL_TRANSITION){
            if (keyCode ==KeyEvent.VK_ENTER){
                currentLevel = transitionLevel;
                loadLevel(currentLevel);
                playerTank = new Tank(FRAME_WIDTH / 2, FRAME_HEIGHT / 2, 0, Color.YELLOW);
                bulletList.clear();
                moveUp = moveDown = moveLeft = moveRight = false;
                hurtTime = 0;
                gameState = START_RUN;
            }
        }
        else if (gameState == START_HELP || gameState == START_ABOUT){
            if (keyCode == KeyEvent.VK_ESCAPE){
                gameState = START_MENU;
            }
        }
        repaint(); // 触发面板重绘（更新界面）
    }

//    按键松开监听
//    重置对应方向的按键标记，停止坦克移动
    public void keyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                moveUp = false;
                break;    // 松开上键
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                moveDown = false;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                moveLeft = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                moveRight = false;
                break;
        }
    }
}