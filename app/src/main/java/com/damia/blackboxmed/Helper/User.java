package com.damia.blackboxmed.Helper;

public class User {
    private String username;
    private String token;

    public User (){
    }

    public User(String email, String token) {
        this.username = username;
        this.token = token;
    }


    public void setUsername(String username) { this.username = username; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public String getToken() { return token; }

}
