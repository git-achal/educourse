package com.edu.educourse.dto;

import java.util.List;

/**
 * UserDto — Safe user response object (never exposes password).
 */
public class UserDto {

    private Long id;
    private String fullName;
    private String email;
    private String username;
    private List<String> roles;

    public UserDto() {}

    public UserDto(Long id, String fullName, String email, String username, List<String> roles) {
        this.id       = id;
        this.fullName = fullName;
        this.email    = email;
        this.username = username;
        this.roles    = roles;
    }

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getFullName()             { return fullName; }
    public void setFullName(String fn)      { this.fullName = fn; }

    public String getEmail()                { return email; }
    public void setEmail(String e)          { this.email = e; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public List<String> getRoles()          { return roles; }
    public void setRoles(List<String> r)    { this.roles = r; }
}
