package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.TourTagRule;

public interface TourTagRuleRepository extends JpaRepository<TourTagRule, Long> {

    List<TourTagRule> findByActiveTrueOrderByPriorityAsc();

    List<TourTagRule> findAllByOrderByPriorityAscRuleIdAsc();
}
