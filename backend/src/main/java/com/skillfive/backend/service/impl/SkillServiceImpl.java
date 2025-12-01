package com.skillfive.backend.service.impl;

import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.enums.SkillType;
import com.skillfive.backend.repository.SkillRepository;
import com.skillfive.backend.service.SkillService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 技能服务实现类
 */
@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    
    // 简单的技能冷却管理，实际项目中可以使用Redis等缓存
    private final Map<String, Long> skillCooldownMap = new ConcurrentHashMap<>();

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public Skill createSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    @Override
    public Optional<Skill> findById(Long id) {
        return skillRepository.findById(id);
    }

    @Override
    public Skill updateSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    @Override
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
        skill.setEnabled(false);
        skillRepository.save(skill);
    }

    @Override
    public List<Skill> findByType(SkillType type) {
        return skillRepository.findByType(type);
    }

    @Override
    public List<Skill> findAllEnabled() {
        return skillRepository.findByEnabledTrue();
    }

    @Override
    public List<Skill> findEnabledByType(SkillType type) {
        return skillRepository.findByTypeAndEnabledTrue(type);
    }

    @Override
    public boolean isSkillAvailable(Long skillId, Long userId, Long gameId) {
        // 检查技能是否存在且启用
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
        
        if (!skill.getEnabled()) {
            return false;
        }
        
        // 检查技能冷却
        String cooldownKey = userId + ":" + gameId + ":" + skillId;
        Long lastUseTime = skillCooldownMap.get(cooldownKey);
        
        if (lastUseTime != null) {
            long currentTime = System.currentTimeMillis();
            long cooldownTime = skill.getCooldown() * 1000L; // 转换为毫秒
            if (currentTime - lastUseTime < cooldownTime) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 记录技能使用时间，开始冷却
     */
    public void startSkillCooldown(Long skillId, Long userId, Long gameId) {
        String cooldownKey = userId + ":" + gameId + ":" + skillId;
        skillCooldownMap.put(cooldownKey, System.currentTimeMillis());
    }
    
    /**
     * 获取技能剩余冷却时间（秒）
     */
    public int getRemainingCooldown(Long skillId, Long userId, Long gameId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
        
        String cooldownKey = userId + ":" + gameId + ":" + skillId;
        Long lastUseTime = skillCooldownMap.get(cooldownKey);
        
        if (lastUseTime == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownTime = skill.getCooldown() * 1000L;
        long remainingTime = cooldownTime - (currentTime - lastUseTime);
        
        return remainingTime > 0 ? (int) (remainingTime / 1000) : 0;
    }
}