package com.BattleTank.util;

//
//维护游戏中的常量，方便后期管理
//

import java.awt.*;

public class Constant {
    public static final String GAME_TITLE = "坦克大战";

//      窗口大小
    public static final int FRAME_WIDTH = 900;
    public static final int FRAME_HEIGHT = 700;

//      游戏状态
    public static final int START_MENU = 0;
    public static final int START_HELP = 1;
    public static final int START_ABOUT = 2;
    public static final int START_RUN = 3;
    public static final int START_OVER = 4;
    public static final int START_WIN = 5;
    //    增加过场状态
    public static final int STATE_LEVEL_TRANSITION = 6;

//      菜单文本
    public static final String[] MENUS = {
            "开始游戏",
            "游戏帮助",
            "游戏关于",
            "退出游戏"
    };
//      字体
    public static final Font Game_Font = new Font("仿宋",Font.BOLD,24);
//      刷新间隔
    public static final int REPAINT_INTERVAL = 30;
}

