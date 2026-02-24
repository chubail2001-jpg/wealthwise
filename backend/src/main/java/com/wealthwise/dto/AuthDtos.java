package com.wealthwise.dto;

public class AuthDtos {

    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String email;

        public RegisterRequest() {}
        public RegisterRequest(String username, String password, String fullName, String email) {
            this.username = username; this.password = password;
            this.fullName = fullName; this.email    = email;
        }
        public String getUsername()       { return username; }
        public String getPassword()       { return password; }
        public String getFullName()       { return fullName; }
        public String getEmail()          { return email; }
        public void setUsername(String v) { username = v; }
        public void setPassword(String v) { password = v; }
        public void setFullName(String v) { fullName = v; }
        public void setEmail(String v)    { email    = v; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}
        public LoginRequest(String username, String password) {
            this.username = username; this.password = password;
        }
        public String getUsername()       { return username; }
        public String getPassword()       { return password; }
        public void setUsername(String v) { username = v; }
        public void setPassword(String v) { password = v; }
    }

    public static class UserSummary {
        private Long   id;
        private String username;
        private String fullName;
        private String email;

        public UserSummary(Long id, String username, String fullName, String email) {
            this.id = id; this.username = username;
            this.fullName = fullName; this.email = email;
        }
        public Long   getId()       { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail()    { return email; }
    }

    public static class AuthResponse {
        private String      token;
        private UserSummary user;

        public AuthResponse(String token, UserSummary user) {
            this.token = token; this.user = user;
        }
        public String      getToken() { return token; }
        public UserSummary getUser()  { return user; }
    }
}
