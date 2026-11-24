package edu.gmu.careerlaunch.sharedcore.applications;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.gmu.careerlaunch.sharedcore.common.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/applications")
@Validated
public class ApplicationController {

    private final ApplicationRepository applications;

    public ApplicationController(ApplicationRepository applications) {
        this.applications = applications;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse created = applications.create(userId, request.jobId(), request.resumeUrl());
        return ResponseEntity.created(URI.create("/applications/" + created.applicationId())).body(created);
    }

    @GetMapping
    public ApplicationPage list(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int pageSize) {
        return applications.findByUserId(userId, status, page, pageSize);
    }

    @GetMapping("/{applicationId}")
    public ApplicationResponse get(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable @Min(1) long applicationId) {
        return applications.findByIdAndUserId(applicationId, userId)
                .orElseThrow(ApplicationController::notFound);
    }

    @PutMapping("/{applicationId}/status")
    public ApplicationResponse updateStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable @Min(1) long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applications.updateStatus(applicationId, userId, request.status())
                .orElseThrow(ApplicationController::notFound);
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Application not found", null);
    }
}
