# <div align=center> SECTION III: Database Design </div>

> This section summarizes the design and relationships of the database. Please review the design and inform us immediately if certain subsystem features are not feasible under our design.

```mermaid
---
config:
  layout: elk
---
erDiagram
    direction LR

    USER_CORE ||--o{ PROFILE_STUDENT : "may be a..."
    USER_CORE ||--o{ PROFILE_MENTOR : "may be a..."
    USER_CORE ||--o{ PROFILE_CAREER : "may be a..."
    USER_CORE ||--o{ PROFILE_ADMIN : "may be a..."

    USER_CORE ||--o{ APPLICATIONS : "has many..."
    APPLICATIONS ||--o{ OPPORTUNITIES : "associated with..."
    APPLICATIONS ||--o{ APPLICATION_NOTES : "has many..."
    APPLICATIONS ||--o{ INTERVIEWS : "has many..."

    PROFILE_STUDENT ||--o{ CONNECTIONS : "may request a..."
    PROFILE_MENTOR ||--o{ CONNECTIONS : "may accept a..."

    PROFILE_CAREER ||--o{ EVENTS : "can publish..."
    PROFILE_STUDENT ||--o{ EVENTS : "can register for..."
    EVENTS ||--o{ REGISTRATIONS : "have..."

    PROFILE_STUDENT ||--o{ FORUM_POSTS : "makes a..."
    FORUM_POSTS ||--o{ REPLIES : "Students can make..."
    FORUM_POSTS ||--o{ PINNED_POSTS : "Career Services Staff can place it in..."
    FORUM_POSTS ||--o{ FLAGGED_POSTS : "flagging places it in..."
    PROFILE_ADMIN ||--o{ FLAGGED_POSTS : "resolve..."

    PROFILE_CAREER ||--o{ NOTIFICATIONS : "can dispatch..."
    USER_CORE ||--o{ NOTIFICATIONS : "has an inbox of..."
    PROFILE_ADMIN ||--o{ LOGS : "can be viewed by..."
    USER_CORE ||--o{ LOGS : "actions are recorded in..."

    USER_CORE {
        int UUID_usr pk
        string hashword
        string email "INDEX: entry point for log-in."
        string[] display_name
        enum notif_preference
        enum usr_status
        enum access_level
    }

    PROFILE_STUDENT {
        int UUID_usr fk
        int conferral
        string major
        string target_industry
        string target_role
        string[] desired_skills
        string desired_mentor_bg
    }

    PROFILE_MENTOR {
        int UUID_usr fk
        string degrees
        string background
        string industry
        string availability
        string[] guidance_areas
        int mentee_capacity
        int mentee_actual
    }

    PROFILE_ADMIN {
        int UUID_usr fk
    }

    PROFILE_CAREER {
        int UUID_usr fk
    }

    OPPORTUNITIES {
        int UUID_op pk
        string employer_name
        string role_title
        date deadline_date
        time deadline_time
    }

    APPLICATIONS {
        int UUID_app pk
        int UUID_usr fk
        int UUID_op fk
        enum stage
    }

    APPLICATION_NOTES {
        int UUID_app fk
        enum app_stage
        string notes
    }

    INTERVIEWS {
        int UUID_app fk
        int round_num
        string notes
        string format
        timestamptz start_time
    }

    CONNECTIONS {
        int UUID_conn pk
        int UUID_usr fk "Student"
        int UUID_usr fk "Mentor"
        int UUID_note fk 
        enum conn_status
    }

    NOTES** {
        int UUID_note pk
        int UUID_usr fk "Note owner"
        int UUID_x fk "Affiliated object; can be a connection, application, etc."
        enum note_category
        string body
    }

    EVENTS {
        int uuid_evnt pk
        enum event_category
        enum event_format
        string target_audience
        int capacity
        int num_registered
        timestamptz created_at
    }

    REGISTRATIONS {
        int uuid_usr fk
        int uuid_evnt fk
    }

    FORUM_POSTS {
        int UUID_pst pk
        int UUID_usr fk "Posted by"
        enum forum_category
        string tags*
        string title
        string body
        int score
        int num_replies***
        timestamptz posted_at
        timestamptz last_activity_at
    }

    REPLIES {
        int UUID_pst fk
        string body
        int score
        int marked_as_helpful
        timestamptz posted_at
    }

    PINNED_POSTS {
        int UUID_pst fk
    }

    FLAGGED_POSTS {
        int UUID_usr fk "Reported by"
        int UUID_usr fk "Resolved by"
        string flag_reason
        enum admin_decision
        timestamptz flagged_at
    }

    NOTIFICATIONS {
        int UUID_usr fk "Sender"
        int UUID_usr fk "Receiver & INDEX: for fetching an inbox."
        enum type
        string inbox_name
        string body
        boolean read_by_recipient "INDEX: for sorting and for metrics."
        boolean pinned
        timestamptz sent_at
    }

    LOGS {
        enum log_category
        int UUID_usr fk "Associated User"
        string information
        timestamptz occured_at
    }

    SUMMARY_OF_ENUMS {
        enum event_category
        enum event_format
        enum app_stages
        enum conn_status
        enum usr_status
        enum access_level
        enum forum_category
        enum admin_decision
        enum note_category
        enum log_category
        enum notif_preference
    }
```
### Footnotes
*tags ideally are stored in one string, separated by commas for database storage.

**to facilitate the numerous free-form note-taking features of the platform, we are evaluating a better way to store these notes.

***rates are currently designed as numerical (i.e., "15 average replies per post" rather than "8 replies an hour). Let us know if this poses problems.

## Recognized Functionalities
```
This section is being expanded in Sprint 4!
```

- An inbox can be built by querying the notifications table by recipient.
- "Usage analytics: Aggregate, anonymized metrics about platform usage, new registrations by week, active users by subsystem, applications logged and stage-progression rates, events created and registration rates, forum posts and reply rates, mentor connections initiated and completed, notifications sent and opened. Presented as a dashboard with basic visualizations. Not individual user tracking — aggregate statistics only. This is the metric set that would let Career Services staff actually see whether the platform is working," (Canvas).
    - Registrations can be logged and given a specific category.
    - Logs will reflect typical activity, which can ideally be referenced for user activity by subsystem.
    - Application metrics doable.
    - Forum posts and reply rates doable, but see footnote *** above.
    - Events and registration metrics doable; query EVENTS "created_at" and "num_registered" fields.
    - Mentor connections doable.
    - Notifications doable; query NOTIFICATIONS "read_by_recipient" field.
