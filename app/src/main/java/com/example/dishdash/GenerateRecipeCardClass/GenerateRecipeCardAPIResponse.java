package com.example.dishdash.GenerateRecipeCardClass;

public class GenerateRecipeCardAPIResponse {
    public String url;
    public String status;
    public String time;

    public GenerateRecipeCardAPIResponse(String url, String status, String time) {
        this.url = url;
        this.status = status;
        this.time = time;
    }

    public GenerateRecipeCardAPIResponse() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
