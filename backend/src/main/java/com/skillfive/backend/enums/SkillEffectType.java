package com.skillfive.backend.enums;

/**
 * 技能效果类型枚举
 * 定义技能的具体效果类型
 */
public enum SkillEffectType {
    /**
     * 重置棋盘 - 清空所有棋子
     */
    BOARD_RESET,
    
    /**
     * 额外回合 - 玩家可以连续行动
     */
    EXTRA_TURN,
    
    /**
     * 移除棋子 - 移除指定位置的棋子
     */
    REMOVE_PIECE,
    
    /**
     * 交换棋子 - 交换两个位置的棋子
     */
    SWAP_PIECES,
    
    /**
     * 强制移动 - 强制对手在指定位置下棋
     */
    FORCE_MOVE,
    
    /**
     * 时间延长 - 增加回合时间限制
     */
    TIME_EXTENSION,
    
    /**
     * 护盾 - 免疫一次技能效果
     */
    SHIELD,
    
    /**
     * 反射 - 将技能效果反射给施法者
     */
    REFLECT,
    
    /**
     * 复制 - 复制对手的上一个技能
     */
    COPY,
    
    /**
     * 混乱 - 随机改变棋盘上的几个棋子
     */
    CHAOS,
    
    /**
     * 透视 - 查看对手的下一步可能行动
     */
    FORESIGHT,
    
    /**
     * 传送 - 将一个棋子传送到另一个位置
     */
    TELEPORT,
    
    /**
     * 冻结 - 冻结对手的某个棋子，使其无法移动
     */
    FREEZE,
    
    /**
     * 燃烧 - 在指定位置放置一个会消失的棋子
     */
    BURN,
    
    /**
     * 治愈 - 恢复被移除的己方棋子
     */
    HEAL
}