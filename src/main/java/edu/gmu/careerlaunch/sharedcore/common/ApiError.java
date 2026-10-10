package edu.gmu.careerlaunch.sharedcore.common;

/**
 * The standard error format every subsystem uses (see docs/conventions.md):
 * {"error": "...", "code": "...", "detail": "..."}
 *
 * A "record" is a short way to write a class that just holds data.
 * Spring Boot turns it into JSON automatically.
 */
public record ApiError(String error, String code, Object detail) {
}
