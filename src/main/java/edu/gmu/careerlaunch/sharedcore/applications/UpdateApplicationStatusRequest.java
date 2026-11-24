package edu.gmu.careerlaunch.sharedcore.applications;

import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(@NotNull ApplicationStatus status) {
}
