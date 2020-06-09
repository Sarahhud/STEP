package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.appengine.api.users.*;
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
      link = null;
    }
    else{
      link = userService.createLogoutURL("/index.html");
    }
    response.getWriter().println(gson.toJson(link));
  }
}
