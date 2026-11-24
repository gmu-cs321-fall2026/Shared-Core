package edu.gmu.careerlaunch.sharedcore.applications;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotNull Integer jobId,
        @Size(max = 2048) String resumeUrl) {
}
