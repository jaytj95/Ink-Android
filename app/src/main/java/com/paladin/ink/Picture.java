package com.paladin.ink;

/**
 * Created by jason on 1/7/17.
 */

public class Picture {
    private String id, url;

    public Picture(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
