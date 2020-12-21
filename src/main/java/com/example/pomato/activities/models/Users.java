package com.example.pomato.activities.models;

public class Users {
    private String id;
    String email;
    String imageUrl;

    //Constructors
    public Users() {
    }

    public  Users(String id, String email, String imageUrl) {
        this.id = id;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }

    public void setName(String name) { this.id = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
