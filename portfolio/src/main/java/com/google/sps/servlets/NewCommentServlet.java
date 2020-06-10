package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for creating new tasks. */
@WebServlet("/new-comment")
public class NewCommentServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    PrintWriter out = response.getWriter();
    // Only logged-in users can post messages
   if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
      return;
    }

    String title = "Title: " + request.getParameter("title");
    String text = "Comment: " + request.getParameter("input");
    String email = userService.getCurrentUser().getEmail();
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("title", title);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("email", email);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }
}