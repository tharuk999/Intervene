package com.interactout.service;

import com.interactout.model.AppProfileEntity;
import com.interactout.model.InterventionEntity;
import com.interactout.model.InterventionType;
import com.interactout.repository.AppProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProfileService {

    private final AppProfileRepository repo;

    public ProfileService(AppProfileRepository repo) {
        this.repo = repo;
    }

    public List<AppProfileEntity> getAll() {
        return repo.findAll();
    }

    public List<AppProfileEntity> getActive() {
        return repo.findByEnabledTrue();
    }

    public AppProfileEntity getByPackageName(String packageName) {
        return repo.findById(packageName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Profile not found: " + packageName));
    }

    /**
     * Create or update profile (upsert).
     * Ensures all 8 intervention types are present after save.
     */
    public AppProfileEntity save(AppProfileEntity incoming) {
        AppProfileEntity existing = repo.findById(incoming.getPackageName()).orElse(null);

        if (existing != null) {
            // Merge fields
            existing.setAppName(incoming.getAppName());
            existing.setEnabled(incoming.isEnabled());
            existing.setDailyLimitMs(incoming.getDailyLimitMs());
            if (incoming.getInterventions() != null && !incoming.getInterventions().isEmpty()) {
                existing.syncInterventions(incoming.getInterventions());
            }
            return repo.save(existing);
        } else {
            if (incoming.getInterventions() != null) {
                incoming.syncInterventions(incoming.getInterventions());
            } else {
                incoming.syncInterventions(List.of()); // fill all 8 defaults
            }
            return repo.save(incoming);
        }
    }

    public AppProfileEntity toggleEnabled(String packageName, boolean enabled) {
        AppProfileEntity profile = getByPackageName(packageName);
        profile.setEnabled(enabled);
        return repo.save(profile);
    }

    /**
     * Update a single intervention's intensity (called on slider release).
     * Implements paper Section 3.2 — intensity 0.0–1.0.
     */
    public AppProfileEntity updateIntensity(String packageName, InterventionType type, float intensity) {
        AppProfileEntity profile = getByPackageName(packageName);
        profile.getInterventions().stream()
            .filter(i -> i.getType() == type)
            .findFirst()
            .ifPresentOrElse(
                i -> i.setIntensity(intensity),
                () -> {
                    InterventionEntity newI = new InterventionEntity();
                    newI.setType(type);
                    newI.setEnabled(true);
                    newI.setIntensity(intensity);
                    newI.setProfile(profile);
                    profile.getInterventions().add(newI);
                }
            );
        return repo.save(profile);
    }

    public void delete(String packageName) {
        if (!repo.existsById(packageName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found: " + packageName);
        }
        repo.deleteById(packageName);
    }
}
