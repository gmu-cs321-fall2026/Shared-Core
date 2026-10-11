> This wiki is under construction! It is being updated with information from Sprints 1 & 2.

# <div align=center> SECTION I: Technology Decisions </div>
| | Choice | Justification |
|-|-|-|
|**Language**| Java | Used frequently in our degree, so developers have familiarity with it. The object-oriented approach will be helpful for the shared core delivering fundamental classes that other subsystems can securely extend as per their needs.
|**Database**| PostgreSQL | Popular, robust, and well-documented. Chosen over SQLite due to a specific request from one or two subsystems that we use PostgreSQL. 
|**ORM**| None chosen at this time. | ORMs put a layer of abstraction between the Java programming and the SQL queries (which are two distinct languages), but they have specific use-cases. If we choose to use an ORM, it will be for a select few functionalities, and will not fully replace the raw query programming.


# <div align=center> SECTION II: Subsystem Awareness </div>

> This section demonstrates our current understanding of what we, as the shared core, need to bear and mind and deliver in order for your subsystem to operate as it pleases.
>
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

<!------------------------------------------------------------------->
<!-- SS6 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem VI: 
## Our Current Understanding

<!------------------------------------------------------------------->
<!-- SS7 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem VII: 
## Our Current Understanding

<!------------------------------------------------------------------->
<!-- SS8 ------------------------------------------------------------>

# <span style="color:lightgreen">Subsystem VIII: 
## Our Current Understanding

<!------------------------------------------------------------------->

# <div align=center> SECTION III: Class Definitions </div>

---
# <span style="color:lightgreen">PARENT CLASS/Log
### Member Fields
| Type | Name | Purpose |
| - | - | - |
| <span style="color:cyan">Integer | <span style="color:cyan">userID | The userID will be the foreign key that relates a log entry to the responsible party. Tables can be joined so that administrators can view all logs pertaining to a userID.

# <span style="color:lightgreen">PARENT CLASS/UserCore
### Member Fields
| Type | Name | Purpose |
| -------- | -------- | -------- |
| <span style="color:cyan">Integer | <span style="color:cyan">userID | A unique ID for each user, to be used for logging and in any other place where we may prefer a more standardized identifier rather than a username or email.|
| String[4] | displayName | An array storing the [0] first name, [1] last name, [3] prefix, [4] suffix. |
| String | email* | The user's email. Can be used for display purposes and for sending a notification through email. |
| Integer | phone* | The user's phone number. Can be used for display purposes and for sending a notification through email. |
| Some Enum | notifPreference | The preferred mode of contact for receiving notifications.
| Some Enum | status | Marks whether a user has any restrictions on their usual access level (suspended, as an example).
| <span style="color:coral">String | <span style="color:coral">password | Very, very temporary. For development purposes, password storage may not start out as very secure, but expect it to become more secure as the semester progresses. |

\* We may store the phone and email with some additional "flag" in the data to determine whether or not the user wants each piece of information publicly displayed, or specifically reserved for the system. Should we do this, the API will handle it for you, and you will not need to worry about indexing or similar concerns as long as you use the provided API calls.
### Member Methods
| Type | Name | Args | Return Type | Purpose |
| -------- | -------- | -------- | -------- | -------- |
| abstract | getRole | - | Some Enum | Assures that extensions of the class have a way of validating user role, and thus access level.

## <span style="color:lightseagreen">UserRole_Student
### Member Fields
| Type | Name | Purpose |
| -------- | -------- | -------- |
| ArrayList\<Application\> | apps | A dynamically-sized array storing all of the user's applications. |
| LinkedList | calendarEvents | A linked list of pointers to relevant events (currently includes all scheduled items). |


### Member Methods
## <span style="color:lightseagreen">UserRole_Mentor
## <span style="color:lightseagreen">UserRole_Career
## <span style="color:lightseagreen">UserRole_Admin

# <span style="color:lightgreen">PARENT CLASS/OpportunityPosting
PRIMARY KEY holder of "opportunityID."

# <span style="color:lightgreen">PARENT CLASS/Application
FOREIGN KEY for "opportunityID."
