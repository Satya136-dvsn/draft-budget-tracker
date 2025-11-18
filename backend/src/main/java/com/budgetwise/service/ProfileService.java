package com.budgetwise.service;

import com.budgetwise.dto.ProfileDto;
import com.budgetwise.entity.User;
import com.budgetwise.entity.UserProfile;
import com.budgetwise.repository.UserProfileRepository;
import com.budgetwise.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(UserProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get user profile by user ID
     * Auto-creates profile if it doesn't exist
     */
    @Transactional
    public ProfileDto getProfile(Long userId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Auto-create profile if it doesn't exist
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                    
                    UserProfile newProfile = new UserProfile(user);
                    newProfile.setCurrency("INR");
                    newProfile.setLanguage("en");
                    newProfile.setTheme("light");
                    newProfile.setTimezone("Asia/Kolkata");
                    newProfile.setDateFormat("dd/MM/yyyy");
                    newProfile.setNotificationEmail(true);
                    newProfile.setNotificationPush(true);
                    
                    return profileRepository.save(newProfile);
                });
        
        return mapToDto(profile);
    }

    /**
     * Create initial profile for a new user
     */
    @Transactional
    public ProfileDto createProfile(Long userId, ProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (profileRepository.existsByUserId(userId)) {
            throw new RuntimeException("Profile already exists for user ID: " + userId);
        }

        UserProfile profile = new UserProfile(user);
        updateProfileFromDto(profile, profileDto);
        
        UserProfile savedProfile = profileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    /**
     * Update existing user profile
     */
    @Transactional
    public ProfileDto updateProfile(Long userId, ProfileDto profileDto) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        updateProfileFromDto(profile, profileDto);
        
        UserProfile updatedProfile = profileRepository.save(profile);
        return mapToDto(updatedProfile);
    }

    /**
     * Update only preferences (theme, language, notifications, etc.)
     */
    @Transactional
    public ProfileDto updatePreferences(Long userId, ProfileDto profileDto) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user ID: " + userId));

        // Update only preference fields
        if (profileDto.getTheme() != null) {
            profile.setTheme(profileDto.getTheme());
        }
        if (profileDto.getLanguage() != null) {
            profile.setLanguage(profileDto.getLanguage());
        }
        if (profileDto.getTimezone() != null) {
            profile.setTimezone(profileDto.getTimezone());
        }
        if (profileDto.getDateFormat() != null) {
            profile.setDateFormat(profileDto.getDateFormat());
        }
        if (profileDto.getNotificationEmail() != null) {
            profile.setNotificationEmail(profileDto.getNotificationEmail());
        }
        if (profileDto.getNotificationPush() != null) {
            profile.setNotificationPush(profileDto.getNotificationPush());
        }

        UserProfile updatedProfile = profileRepository.save(profile);
        return mapToDto(updatedProfile);
    }

    /**
     * Initialize profile on user registration
     */
    @Transactional
    public void initializeProfile(User user) {
        if (!profileRepository.existsByUserId(user.getId())) {
            UserProfile profile = new UserProfile(user);
            // Set default values for Indian users
            profile.setCurrency("INR");
            profile.setLanguage("en");
            profile.setTheme("light");
            profile.setTimezone("Asia/Kolkata");
            profile.setDateFormat("dd/MM/yyyy");
            profile.setNotificationEmail(true);
            profile.setNotificationPush(true);
            
            profileRepository.save(profile);
        }
    }

    /**
     * Helper method to update profile entity from DTO
     */
    private void updateProfileFromDto(UserProfile profile, ProfileDto dto) {
        if (dto.getMonthlyIncome() != null) {
            profile.setMonthlyIncome(dto.getMonthlyIncome());
        }
        if (dto.getSavingsTarget() != null) {
            profile.setSavingsTarget(dto.getSavingsTarget());
        }
        if (dto.getCurrency() != null) {
            profile.setCurrency(dto.getCurrency());
        }
        if (dto.getTimezone() != null) {
            profile.setTimezone(dto.getTimezone());
        }
        if (dto.getLanguage() != null) {
            profile.setLanguage(dto.getLanguage());
        }
        if (dto.getTheme() != null) {
            profile.setTheme(dto.getTheme());
        }
        if (dto.getDateFormat() != null) {
            profile.setDateFormat(dto.getDateFormat());
        }
        if (dto.getNotificationEmail() != null) {
            profile.setNotificationEmail(dto.getNotificationEmail());
        }
        if (dto.getNotificationPush() != null) {
            profile.setNotificationPush(dto.getNotificationPush());
        }
    }

    /**
     * Helper method to map entity to DTO
     */
    private ProfileDto mapToDto(UserProfile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setMonthlyIncome(profile.getMonthlyIncome());
        dto.setSavingsTarget(profile.getSavingsTarget());
        dto.setCurrency(profile.getCurrency());
        dto.setTimezone(profile.getTimezone());
        dto.setLanguage(profile.getLanguage());
        dto.setTheme(profile.getTheme());
        dto.setDateFormat(profile.getDateFormat());
        dto.setNotificationEmail(profile.getNotificationEmail());
        dto.setNotificationPush(profile.getNotificationPush());
        return dto;
    }
}
