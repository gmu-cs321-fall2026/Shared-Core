package edu.gmu.careerlaunch.sharedcore.applications;

import java.util.List;

public record ApplicationPage(
        List<ApplicationResponse> data,
        int page,
        int pageSize,
        long total) {
}
