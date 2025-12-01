package com.skillfive.backend.repository;

import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 技能Repository接口
 */
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * 根据技能类型查找技能
     */
    List<Skill> findByType(SkillType type);

    /**
     * 查找启用的技能
     */
    List<Skill> findByEnabledTrue();

    /**
     * 根据技能类型查找启用的技能
     */
    List<Skill> findByTypeAndEnabledTrue(SkillType type);

    /**
     * 根据技能名称查找技能
     */
    Skill findByName(String name);
}