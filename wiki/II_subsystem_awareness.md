# <div align=center> SECTION II: Subsystem Awareness </div>

> This section demonstrates our current understanding of what we, as the shared core, need to bear and mind and deliver in order for your subsystem to operate as it pleases.

> If you have any questions or concerns regarding what's listed in this section, please contact our current sprint's Product Owner.

<!-- SS2 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem II: Application & Readiness Tracker
## Our Current Understanding
- We need to formulate an application object:
    - "Employer name, role title, application date, source (Handshake, referral, career fair, direct), and a target application deadline," (Canvas). All of these fields will be static (so we assume an employer will not be changing their name, the title of the role, or the application deadline).
    - A status for the application, which is dynamic. Each status has its own free-text notes section.
        - Particularly, the interview status can have multiple rounds and the following varied fields: "scheduled date/time, format (phone, video, on-site, technical, behavioral), and outcome notes," (Canvas).
- We need to ensure opportunity postings (a separate object under our design) have unique IDs that applications can hold as foreign keys.
- We need to deliver a database that allows you to:
    - Comfortably query a user's applications to present them as a sorted list, and in a calendar view.
    - Store application objects (as above).
## Standing Questions
- Readiness checklist; how are we going to accommodate the templates that can be set by Career Services? A possible thought is using PostgreSQL's JSON type.

<!------------------------------------------------------------------->
<!-- SS3 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem III: Events & Resources
> Please reach out to our product owner for this sprint with any questions or concerns.
## Our Current Understanding
- We need to formulate an event object:
    - "Category...target audience...format. ...registration count and capacity limits," (Canvas).
- We need to formulate an opportunity posting object that contains opportunity information, and an opportunity ID, the former of which is intended to be consumed for the lightweight postings board.
- We need to deliver a database that allows you to store these objects.
- We need to format log reporting in such a way that registration can be properly logged.

<!------------------------------------------------------------------->
<!-- SS4 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem IV: Mentor Matching
## Our Current Understanding
- We need to formulate a mentor profile extension on the core user object:
    - "background...areas they can offer guidance in...availability...current mentee capacity," (Canvas).
- We need to formulate a student profile extension on the core user object:
    - "target industry or role, specific skills or guidance areas, and preferred mentor background," (Canvas).
- We need to formulate a connection record object:
    - A status for accepted or declined, where an accepted status can then become pending, active, or completed/withdrawn.
    - A way to store session logs and shared notes.

## Standing Questions
The connection record seems very interactive based on the statement of work. It may be worth meeting during a class to establish how we really want to go about this.

<!------------------------------------------------------------------->
<!-- SS5 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem V: Community Forum & Discussion
## Our Current Understanding
- We need to formulate a community post object:
    - "...title, body, category, optional tags such as an employer or role name," (Canvas).
    - A post ID to relate replies to.
    - Reply count.
- We need to ensure logs can properly report moderation decisions and flagged community posts.
- We need to deliver a database that can be queried such that post history can be tracked.

<!------------------------------------------------------------------->
<!-- SS6 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem VI: Notifications & Communications
## Our Current Understanding
- We need to deliver some way to manifest an "inbox" for the user; a place where they can view all notifications sent to them.
- We need to deliver a way to store notification preferences such that they can be flexibly designed by you all, but still stored in a consistent manner.
- We need to ensure you all can query the database efficiently to create weekly digest emails--our goal is to make integration as small of a headache for you as possible.

<!------------------------------------------------------------------->
<!-- SS7 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem VII: Platform Admin & Analytics
## Our Current Understanding
- We need to provide fields in a standard user profile that allow control over user access beyond just user roles and what interfaces we give them. Banning or suspending a user, for example.
- We need to assess which of your aggregate analytics can be comfortably handling through your own querying, and which need additional support from us. Database tables will be designed with useful grouping mechanisms in mind for data analytics.
- Generally, this segment does not cover all we need to be considering for you all. Please keep us updated on ways you plan to use data so we can make sure the database itself keeps up with those needs. 

<!------------------------------------------------------------------->
<!-- SS8 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem VIII: Mock Interview & Peer Practice
## Our Current Understanding

<!------------------------------------------------------------------->