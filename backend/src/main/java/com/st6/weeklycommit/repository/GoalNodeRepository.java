package com.st6.weeklycommit.repository;

import com.st6.weeklycommit.entity.GoalNode;
import com.st6.weeklycommit.entity.enums.GoalLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalNodeRepository extends JpaRepository<GoalNode, UUID> {
    List<GoalNode> findByLevel(GoalLevel level);
    List<GoalNode> findByParentId(UUID parentId);
}
