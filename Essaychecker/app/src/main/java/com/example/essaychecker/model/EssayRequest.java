// 这是发送给后端的请求体
package com.example.essaychecker.model;

public class EssayRequest {
    private String essay;

    public EssayRequest(String essay) {
        this.essay = essay;
    }

    public String getEssay() {
        return essay;
    }

    public void setEssay(String essay) {
        this.essay = essay;
    }
}
