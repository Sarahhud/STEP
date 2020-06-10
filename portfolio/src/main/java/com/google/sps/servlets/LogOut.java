package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for creating new tasks. */
@WebServlet("/log-out")
public class LogOut extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String link;
    UserService userService = UserServiceFactory.getUserService();
    Gson gson = new Gson();
    response.setContentType("application/json;");
    // Only logged-in users can post messages
    if (!userService.isUserLoggedIn()) {
      link = "";
    }
    else {
      link = userService.createLogoutURL("/index.html");
    }
    response.getWriter().println(gson.toJson(link));
  }
}
