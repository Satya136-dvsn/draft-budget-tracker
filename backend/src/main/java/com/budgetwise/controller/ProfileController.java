package com.budgetwise.controller;

import com.budgetwise.dto.ProfileDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Get current user's profile
     */
    @GetMapping
    public ResponseEntity<ProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        ProfileDto profile = profileService.getProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     */
    @PutMapping
    public ResponseEntity<ProfileDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ProfileDto profileDto) {
        ProfileDto updatedProfile = profileService.updateProfile(currentUser.getId(), profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update only user preferences (theme, language, notifications, etc.)
     */
    @PutMapping("/preferences")
    public ResponseEntity<ProfileDto> updatePreferences(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ProfileDto profileDto) {
        ProfileDto updatedProfile = profileService.updatePreferences(currentUser.getId(), profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Test endpoint to verify profile controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Profile endpoint is working!");
    }
}
