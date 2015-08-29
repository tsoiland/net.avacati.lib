package net.avacati.lib.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;


public class ServerAb {
    private final Servlet servlet;
    private final int port;

    public ServerAb(Servlet servlet, int port) {
        this.servlet = servlet;
        this.port = port;
    }

    public void StartServer() {
        try {
            // Create server
            Server server = new Server(port);

            // Register servlet
            ServletContextHandler handler = new ServletContextHandler();
            handler.addServlet(new ServletHolder(servlet), "/");
            server.setHandler(handler);

            // Start
            server.start();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
