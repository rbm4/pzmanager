package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting.ConfigType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SandboxSettingRepository extends JpaRepository<SandboxSetting, Long> {

    Optional<SandboxSetting> findBySettingKey(String settingKey);

    Optional<SandboxSetting> findBySettingKeyAndConfigType(String settingKey, ConfigType configType);

    @Query("SELECT s FROM SandboxSetting s WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.settingKey) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR s.category = :category) AND " +
           "(:configType IS NULL OR s.configType = :configType)")
    Page<SandboxSetting> search(@Param("search") String search, @Param("category") String category,
                                @Param("configType") ConfigType configType, Pageable pageable);

    @Query("SELECT s FROM SandboxSetting s WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.settingKey) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR s.category = :category)")
    Page<SandboxSetting> search(@Param("search") String search, @Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT s.category FROM SandboxSetting s WHERE s.category IS NOT NULL ORDER BY s.category")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT s.category FROM SandboxSetting s WHERE s.category IS NOT NULL AND s.configType = :configType ORDER BY s.category")
    List<String> findDistinctCategoriesByConfigType(@Param("configType") ConfigType configType);

    List<SandboxSetting> findByOverwriteAtStartupTrueAndAppliedValueIsNotNull();

    List<SandboxSetting> findByOverwriteAtStartupTrueAndAppliedValueIsNotNullAndConfigType(ConfigType configType);

    List<SandboxSetting> findByConfigType(ConfigType configType);

    long countByAppliedValueIsNotNull();

    long countByOverwriteAtStartupTrue();

    long countByConfigType(ConfigType configType);
}
