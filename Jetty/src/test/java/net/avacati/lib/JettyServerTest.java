package net.avacati.lib;

import net.avacati.lib.jetty.JettyServer;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.Socket;

public class JettyServerTest {
    private static final int PORT = 8766;

    @Test
    public void httpRequestShouldGetThroughToServlet() throws IOException {
        // Setup SUT in separate thread.
        new Thread(() -> {
            // Create a servlet
            Servlet servlet = new HttpServlet() {
                @Override
                protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                    response.setContentType("text/html;charset=utf-8");
                    response.getWriter().write("foobar");
                }
            };

            // Create the SUT
            final JettyServer jettyServer = new JettyServer(servlet, PORT);

            // Start the SUT
            jettyServer.startAndJoin();
        }, "JettyServer").start();

        // Arrange client side
        final Socket socket = new Socket("localhost", PORT);
        final OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
        String request =
                "GET /path/file.html HTTP/1.0\n" +
                "From: someuser@jmarshall.com\n" +
                "User-Agent: HTTPTool/1.0\n" +
                "\n";

        // Act - send the request
        writer.write(request);
        writer.flush();

        // Act - receive response
        InputStream in = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String c;
        while ((c = br.readLine()) != null) {
            sb.append(c);
        }

        // Assert
        String next = sb.toString();
        Assert.assertTrue("Does not contain foobar: " + next, next.contains("foobar"));
    }
}
