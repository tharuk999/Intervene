package com.interactout.repository;

import com.interactout.model.AppProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppProfileRepository extends JpaRepository<AppProfileEntity, String> {

    /** Returns only profiles where enabled = true (used by Android polling) */
    List<AppProfileEntity> findByEnabledTrue();
}
