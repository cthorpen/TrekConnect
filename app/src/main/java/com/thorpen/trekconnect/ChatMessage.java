package com.thorpen.trekconnect;

public class ChatMessage {
    private String author;
    private String content;

    public ChatMessage() {
        author = "BLANK AUTHOR";
        content = "BLANK CONTENT";
    }

    public ChatMessage(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "From: " + author + "\n" + content;
    }
}