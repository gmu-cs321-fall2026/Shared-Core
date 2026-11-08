AI Prompt Log: Dockerfile

Date: 10/10/2026

Developer: David Fyne-Okorie

AI Tool: Claude

Asked: Create a user registration program in springboot connected to a postgresql database. Please explain it to me in steps

Produced: "UserCore.java", "UserRepository.java", "UserService.java", "UserRegistrationDTO.java": Backend for user registration

Changed: Kept the core logic. Rejected the .Registration library and some unused libraries as well. Updated the pom.xml so that the libraries and imports could work. Removed the username field since it won't be used in our program. Added a roles array field for the subsystem users.