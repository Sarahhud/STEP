package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that encapsulates the subtraction game. */
@WebServlet("/comment-choice")
public final class CommentChoiceServlet extends HttpServlet {

  private int choice = -1;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String json = new Gson().toJson(choice);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the input from the form.
    choice = getChoice(request);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /** Returns the choice entered by the user, or -1 if the choice was invalid. */
  private int getChoice(HttpServletRequest request) {
    // Get the input from the form.
    String commentChoiceString = request.getParameter("comment-choice");

    // Convert the input to an int.
    int commentChoice;
    try {
      commentChoice = Integer.parseInt(commentChoiceString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentChoiceString);
      return -1;
    }
    return commentChoice;
  }
}