package com.google.sps.data;

/** An item on a todo list. */
public final class Comment {  
  private final long id;
  private final String title;
  private final long timestamp;
  private final String text;
  private final String author;

  public Comment(long id, String title, long timestamp, String author, String text) {
    this.id = id;
    this.title = title;
    this.timestamp = timestamp;
    this.text = text;
    this.author = author;
  }
}