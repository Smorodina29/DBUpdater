package com.company.ui.jfx.login;

/**
 * Created by Александр on 30.03.2017.
 */
public class User {

    private String name;
    private Role role;

    public User(String name) {
        this(name, Role.USER);
    }

    public User(String name, Role role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
