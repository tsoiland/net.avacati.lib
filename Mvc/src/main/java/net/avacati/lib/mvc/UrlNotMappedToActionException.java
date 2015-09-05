package net.avacati.lib.mvc;

public class UrlNotMappedToActionException extends RuntimeException {
    private final String url;

    public UrlNotMappedToActionException(String url) {
        super("Could not find an action mapping from url: " + url + " in route's action list.");
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
