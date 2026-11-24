package edu.gmu.careerlaunch.sharedcore.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationRepository {

    private static final String COLUMNS = "application_id, job_id, user_id, status, resume_url, updated_at, submitted_at";

    private final JdbcTemplate jdbc;

    private final RowMapper<ApplicationResponse> applicationMapper = (rs, rowNum) ->
            new ApplicationResponse(
                    rs.getLong("application_id"),
                    rs.getInt("job_id"),
                    rs.getObject("user_id", UUID.class),
                    ApplicationStatus.valueOf(rs.getString("status")),
                    rs.getString("resume_url"),
                    rs.getObject("updated_at", java.time.OffsetDateTime.class),
                    rs.getObject("submitted_at", java.time.OffsetDateTime.class));

    public ApplicationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ApplicationResponse create(UUID userId, int jobId, String resumeUrl) {
        String sql = "INSERT INTO applications (user_id, job_id, resume_url) VALUES (?, ?, ?) "
                + "RETURNING " + COLUMNS;
        return jdbc.queryForObject(sql, applicationMapper, userId, jobId, resumeUrl);
    }

    public Optional<ApplicationResponse> findByIdAndUserId(long applicationId, UUID userId) {
        String sql = "SELECT " + COLUMNS + " FROM applications WHERE application_id = ? AND user_id = ?";
        return jdbc.query(sql, applicationMapper, applicationId, userId).stream().findFirst();
    }

    public ApplicationPage findByUserId(UUID userId, ApplicationStatus status, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE user_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(userId);
        if (status != null) {
            where.append(" AND status = CAST(? AS application_status)");
            args.add(status.name());
        }

        Long total = jdbc.queryForObject("SELECT count(*) FROM applications" + where, Long.class, args.toArray());
        String sql = "SELECT " + COLUMNS + " FROM applications" + where
                + " ORDER BY submitted_at DESC, application_id DESC LIMIT ? OFFSET ?";
        args.add(pageSize);
        args.add((long) (page - 1) * pageSize);
        List<ApplicationResponse> data = jdbc.query(sql, applicationMapper, args.toArray());
        return new ApplicationPage(data, page, pageSize, total == null ? 0 : total);
    }

    public Optional<ApplicationResponse> updateStatus(long applicationId, UUID userId, ApplicationStatus status) {
        String sql = "UPDATE applications SET status = CAST(? AS application_status) "
                + "WHERE application_id = ? AND user_id = ? RETURNING " + COLUMNS;
        return jdbc.query(sql, applicationMapper, status.name(), applicationId, userId).stream().findFirst();
    }
}
