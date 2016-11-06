package com.paladin.ink;

/**
 * Created by jason on 11/6/16.
 */
public class User {
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getName() + " " + getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User(String id, String name) {

        this.id = id;
        this.name = name;
    }

    String name;
}
