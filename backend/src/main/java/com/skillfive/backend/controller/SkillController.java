package com.skillfive.backend.controller;

import com.skillfive.backend.dto.SkillEffectResponse;
import com.skillfive.backend.dto.SkillUseRequest;
import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.enums.SkillType;
import com.skillfive.backend.service.GameService;
import com.skillfive.backend.service.SkillService;
import com.skillfive.backend.service.skill.SkillManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 技能控制器
 */
@RestController
@RequestMapping("/api/skill")
public class SkillController {

    @Autowired
    private SkillService skillService;
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private SkillManagerService skillManagerService;

    /**
     * 创建新技能（管理员功能）
     */
    @PostMapping("/create")
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        Skill createdSkill = skillService.createSkill(skill);
        return ResponseEntity.ok(createdSkill);
    }

    /**
     * 获取技能详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        Skill skill = skillService.findById(id)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
        return ResponseEntity.ok(skill);
    }

    /**
     * 更新技能（管理员功能）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        Skill updatedSkill = skillService.updateSkill(skill);
        return ResponseEntity.ok(updatedSkill);
    }

    /**
     * 删除技能（管理员功能）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有启用的技能
     */
    @GetMapping("/all")
    public ResponseEntity<List<Skill>> getAllEnabledSkills() {
        List<Skill> skills = skillService.findAllEnabled();
        return ResponseEntity.ok(skills);
    }

    /**
     * 根据类型获取技能
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Skill>> getSkillsByType(@PathVariable SkillType type) {
        List<Skill> skills = skillService.findByType(type);
        return ResponseEntity.ok(skills);
    }

    /**
     * 检查技能是否可用
     */
    @GetMapping("/{skillId}/available")
    public ResponseEntity<Map<String, Object>> checkSkillAvailable(@PathVariable Long skillId,
                                                                 @RequestParam Long userId,
                                                                 @RequestParam Long gameId) {
        boolean available = skillService.isSkillAvailable(skillId, userId, gameId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * 获取技能冷却时间
     */
    @GetMapping("/{skillId}/cooldown")
    public ResponseEntity<Map<String, Integer>> getSkillCooldown(@PathVariable Long skillId,
                                                              @RequestParam Long userId,
                                                              @RequestParam Long gameId) {
        // 这里可以从SkillServiceImpl获取扩展的冷却时间方法
        return ResponseEntity.ok(Map.of("remainingCooldown", 0));
    }
    
    /**
     * 使用技能（新实现）
     * 
     * @param request 技能使用请求
     * @return 技能效果响应
     */
    @PostMapping("/use")
    public ResponseEntity<SkillEffectResponse> useSkill(@RequestBody SkillUseRequest request) {
        try {
            // 验证参数
            if (request.getSkillId() == null || request.getUserId() == null || request.getGameId() == null) {
                return ResponseEntity.badRequest().body(
                    SkillEffectResponse.failure("缺少必要参数")
                );
            }
            
            // 获取技能信息
            Skill skill = skillService.findById(request.getSkillId())
                .orElseThrow(() -> new RuntimeException("技能不存在"));
            
            // 获取游戏信息
            Game game = gameService.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
            
            // 验证用户是否拥有该技能
            // 注意：这里假设所有用户都可以使用所有技能，实际项目中可能需要实现用户技能关联
            // if (!skillService.hasSkill(request.getUserId(), request.getSkillId())) {
            //     return ResponseEntity.badRequest().body(
            //         SkillEffectResponse.failure("用户未拥有该技能")
            //     );
            // }
            
            // 验证技能冷却时间
            if (!skillService.isSkillAvailable(request.getSkillId(), request.getUserId(), request.getGameId())) {
                return ResponseEntity.badRequest().body(
                    SkillEffectResponse.failure("技能冷却中")
                );
            }
            
            // 执行技能效果
            Game updatedGame = skillManagerService.executeSkillEffect(
                game, skill, request.getUserId(), request.getTargetPosition(), request.getParams()
            );
            
            // 更新游戏状态
            gameService.updateGame(updatedGame);
            
            // 记录技能使用
            skillService.startSkillCooldown(request.getSkillId(), request.getUserId(), request.getGameId());
            
            // 构建响应
            SkillEffectResponse.GameStateResponse gameState = new SkillEffectResponse.GameStateResponse();
            gameState.setGameId(updatedGame.getId());
            gameState.setBoardState(updatedGame.getBoardState());
            gameState.setCurrentPlayerId(updatedGame.getCurrentPlayerId());
            gameState.setLastMove(updatedGame.getLastMove());
            gameState.setGameStatus(updatedGame.getStatus().name());
            
            String effectDescription = skillManagerService.getSkillDescription(skill);
            
            return ResponseEntity.ok(
                SkillEffectResponse.success("技能使用成功", gameState, effectDescription)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                SkillEffectResponse.failure(e.getMessage())
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                SkillEffectResponse.failure(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                SkillEffectResponse.failure("技能使用失败: " + e.getMessage())
            );
        }
    }
    
    /**
     * 获取用户的技能列表（新实现）
     * 
     * @param userId 用户ID
     * @return 技能列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSkills(@PathVariable Long userId) {
        try {
            // 注意：这里假设所有用户都可以使用所有技能，实际项目中可能需要实现用户技能关联
            // return ResponseEntity.ok(skillService.getUserSkills(userId));
            return ResponseEntity.ok(skillService.findAllEnabled());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取用户技能失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有可用的技能效果类型（新实现）
     * 
     * @return 效果类型列表
     */
    @GetMapping("/effect-types")
    public ResponseEntity<?> getEffectTypes() {
        try {
            return ResponseEntity.ok(skillManagerService.getAllEffectTypes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取效果类型失败: " + e.getMessage());
        }
    }
}