package com.google.sps.data;

public final class Comment {  
  private final long id;
  private final String title;
  private final long timestamp;
  private final String text;
  private final String url;
  private final String email;

  public Comment(long id, String title, long timestamp, String text, String email, String url) {
    this.id = id;
    this.title = title;
    this.timestamp = timestamp;
    this.text = text;
    this.url = url;
    this.email = email;
  }
}