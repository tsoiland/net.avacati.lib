package net.avacati.lib.mvc.helpers;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;
import java.util.Locale;

public class TestResponse implements HttpServletResponse {

    private StringWriter stringWriter;
    private String redirectLocation;
    private ByteArrayOutputStream outputStream;
    private int serverCode;
    private String errorMsg;

    public String spyRedirectLocation() {
        return this.redirectLocation;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        outputStream = new ByteArrayOutputStream();
        return new ServletOutputStream() {
            public boolean isReady() {
                return true;
            }

            public void setWriteListener(WriteListener writeListener) {

            }

            public void write(int b) throws IOException {
                outputStream.write(b);
            }
        };
    }

    public PrintWriter getWriter() throws IOException {
        stringWriter = new StringWriter();
        return new PrintWriter(stringWriter);
    }

    public String spyResponseWriterContent() {
        return stringWriter.toString();
    }

    public void setCharacterEncoding(String charset) {

    }

    public void setContentLength(int len) {

    }

    public void setContentLengthLong(long len) {

    }

    public void setContentType(String type) {

    }

    public void setBufferSize(int size) {

    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale loc) {

    }

    public Locale getLocale() {
        return null;
    }

    public void addCookie(Cookie cookie) {

    }

    public boolean containsHeader(String name) {
        return false;
    }

    public String encodeURL(String url) {
        return null;
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeUrl(String url) {
        return null;
    }

    public String encodeRedirectUrl(String url) {
        return null;
    }

    public void sendError(int sc, String msg) throws IOException {
        this.serverCode = sc;
        this.errorMsg = msg;
    }

    public void sendError(int sc) throws IOException {
        this.serverCode = sc;
    }

    public void sendRedirect(String location) throws IOException {
        this.redirectLocation = location;
    }

    public void setDateHeader(String name, long date) {

    }

    public void addDateHeader(String name, long date) {

    }

    public void setHeader(String name, String value) {

    }

    public void addHeader(String name, String value) {

    }

    public void setIntHeader(String name, int value) {

    }

    public void addIntHeader(String name, int value) {

    }

    public void setStatus(int sc) {

    }

    public void setStatus(int sc, String sm) {

    }

    public int getStatus() {
        return 0;
    }

    public String getHeader(String name) {
        return null;
    }

    public Collection<String> getHeaders(String name) {
        return null;
    }

    public Collection<String> getHeaderNames() {
        return null;
    }

    public byte[] spyResponseStreamContent() {
        return outputStream.toByteArray();
    }

    public int spyServerCode() {
        return serverCode;
    }

    public String spyErrorMsg() {
        return errorMsg;
    }
}
