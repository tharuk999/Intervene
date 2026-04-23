package com.interactout.controller;

import com.interactout.model.AppProfileEntity;
import com.interactout.model.InterventionType;
import com.interactout.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** GET /api/profiles — all profiles (Android polls on startup) */
    @GetMapping
    public List<AppProfileEntity> getAll() {
        return profileService.getAll();
    }

    /** GET /api/profiles/active — only enabled profiles */
    @GetMapping("/active")
    public List<AppProfileEntity> getActive() {
        return profileService.getActive();
    }

    /** GET /api/profiles/{pkg} */
    @GetMapping("/{packageName}")
    public AppProfileEntity getOne(@PathVariable String packageName) {
        return profileService.getByPackageName(packageName);
    }

    /** POST /api/profiles — create or update (upsert) */
    @PostMapping
    public AppProfileEntity save(@RequestBody AppProfileEntity profile) {
        return profileService.save(profile);
    }

    /** PUT /api/profiles/{pkg}/enabled — toggle master switch */
    @PutMapping("/{packageName}/enabled")
    public AppProfileEntity toggleEnabled(
        @PathVariable String packageName,
        @RequestBody Map<String, Boolean> body
    ) {
        return profileService.toggleEnabled(packageName, body.get("enabled"));
    }

    /**
     * PUT /api/profiles/{pkg}/interventions/{type}/intensity
     * Called on slider release from React Native InterventionSetupScreen.
     */
    @PutMapping("/{packageName}/interventions/{type}/intensity")
    public AppProfileEntity updateIntensity(
        @PathVariable String packageName,
        @PathVariable InterventionType type,
        @RequestBody Map<String, Float> body
    ) {
        return profileService.updateIntensity(packageName, type, body.get("intensity"));
    }

    /** DELETE /api/profiles/{pkg} */
    @DeleteMapping("/{packageName}")
    public ResponseEntity<Void> delete(@PathVariable String packageName) {
        profileService.delete(packageName);
        return ResponseEntity.noContent().build();
    }
}
