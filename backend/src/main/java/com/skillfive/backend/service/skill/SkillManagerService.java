package com.skillfive.backend.service.skill;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.enums.SkillEffectType;
import com.skillfive.backend.service.skill.handlers.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 技能管理器服务
 * 管理所有技能效果处理器
 */
@Service
public class SkillManagerService {
    
    private final Map<String, SkillEffectHandler> handlers = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // 注册所有技能效果处理器
        registerHandler(new BoardResetHandler());
        registerHandler(new ExtraTurnHandler());
        registerHandler(new RemovePieceHandler());
        registerHandler(new SwapPiecesHandler());
        registerHandler(new ForceMoveHandler());
        registerHandler(new ShieldHandler());
        registerHandler(new FreezeHandler());
        registerHandler(new TeleportHandler());
        registerHandler(new HealHandler());
        // 可以继续添加更多处理器...
    }
    
    /**
     * 注册技能效果处理器
     * 
     * @param handler 处理器实例
     */
    private void registerHandler(SkillEffectHandler handler) {
        handlers.put(handler.getSupportedEffectType(), handler);
    }
    
    /**
     * 获取技能效果处理器
     * 
     * @param effectType 效果类型
     * @return 对应的处理器
     */
    public SkillEffectHandler getHandler(String effectType) {
        SkillEffectHandler handler = handlers.get(effectType);
        if (handler == null) {
            throw new IllegalArgumentException("不支持的效果类型: " + effectType);
        }
        return handler;
    }
    
    /**
     * 执行技能效果
     * 
     * @param game 当前游戏
     * @param skill 使用的技能
     * @param userId 使用技能的用户ID
     * @param targetPosition 目标位置（可选）
     * @param params 额外参数（可选）
     * @return 更新后的游戏
     */
    public Game executeSkillEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        SkillEffectHandler handler = getHandler(skill.getEffectType().name());
        
        // 验证技能是否可以使用
        if (!handler.canUseSkill(game, skill, userId, targetPosition)) {
            throw new IllegalStateException("技能使用条件不满足");
        }
        
        // 执行技能效果
        return handler.executeEffect(game, skill, userId, targetPosition, params);
    }
    
    /**
     * 验证技能是否可以使用
     * 
     * @param game 当前游戏
     * @param skill 要使用的技能
     * @param userId 使用技能的用户ID
     * @param targetPosition 目标位置（可选）
     * @return 是否可以使用
     */
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        try {
            SkillEffectHandler handler = getHandler(skill.getEffectType().name());
            return handler.canUseSkill(game, skill, userId, targetPosition);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取技能描述
     * 
     * @param skill 技能
     * @return 技能描述
     */
    public String getSkillDescription(Skill skill) {
        try {
            SkillEffectHandler handler = getHandler(skill.getEffectType().name());
            return handler.getSkillDescription(skill);
        } catch (IllegalArgumentException e) {
            return skill.getName() + " - 暂无描述";
        }
    }
    
    /**
     * 获取所有支持的效果类型
     * 
     * @return 效果类型列表
     */
    public Map<String, String> getAllEffectTypes() {
        Map<String, String> effectTypes = new HashMap<>();
        for (SkillEffectType type : SkillEffectType.values()) {
            effectTypes.put(type.name(), getEffectTypeDescription(type));
        }
        return effectTypes;
    }
    
    /**
     * 获取效果类型描述
     * 
     * @param type 效果类型
     * @return 描述信息
     */
    private String getEffectTypeDescription(SkillEffectType type) {
        switch (type) {
            case BOARD_RESET:
                return "重置棋盘";
            case EXTRA_TURN:
                return "额外回合";
            case REMOVE_PIECE:
                return "移除棋子";
            case SWAP_PIECES:
                return "交换棋子";
            case FORCE_MOVE:
                return "强制移动";
            case TIME_EXTENSION:
                return "时间延长";
            case SHIELD:
                return "护盾";
            case REFLECT:
                return "反射";
            case COPY:
                return "复制";
            case CHAOS:
                return "混乱";
            case FORESIGHT:
                return "透视";
            case TELEPORT:
                return "传送";
            case FREEZE:
                return "冻结";
            case BURN:
                return "燃烧";
            case HEAL:
                return "治愈";
            default:
                return "未知效果";
        }
    }
}