package com.st6.weeklycommit.controller;

import com.st6.weeklycommit.entity.GoalNode;
import com.st6.weeklycommit.entity.enums.GoalLevel;
import com.st6.weeklycommit.repository.GoalNodeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalNodeRepository goalNodeRepository;

    public GoalController(GoalNodeRepository goalNodeRepository) {
        this.goalNodeRepository = goalNodeRepository;
    }

    @GetMapping
    public List<GoalNode> getAll() {
        return goalNodeRepository.findAll();
    }

    @GetMapping("/outcomes")
    public List<GoalNode> getOutcomes() {
        return goalNodeRepository.findByLevel(GoalLevel.OUTCOME);
    }

    @GetMapping("/hierarchy")
    public List<GoalNode> getHierarchy() {
        return goalNodeRepository.findAll();
    }
}
