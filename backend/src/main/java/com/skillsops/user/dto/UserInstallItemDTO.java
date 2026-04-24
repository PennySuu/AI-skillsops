package com.skillsops.user.dto;

public record UserInstallItemDTO(
        Long skillId,
        String skillName,
        String installedAt,
        String installedVersion,
        String latestVersion,
        boolean updateAvailable,
        boolean offline) {
}
