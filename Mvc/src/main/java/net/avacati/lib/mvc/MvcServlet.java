package net.avacati.lib.mvc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class MvcServlet extends HttpServlet {
    private Route route;

    MvcServlet(Route route) {
        this.route = route;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get request data form servlet
        Map<String, String[]> postdata = request.getParameterMap();

        // Join values for similar keys to avoid array.
        Map<String, String> map = new HashMap<>();
        for(String key: postdata.keySet()){
            map.put(key, Arrays.stream(postdata.get(key)).collect(Collectors.joining(",")));
        }

        // Pass on to route
        this.route.route(request.getRequestURI(), map, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }
}
