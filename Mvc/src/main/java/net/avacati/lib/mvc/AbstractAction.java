package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import java.util.Map;

public abstract class AbstractAction {
    public String url;
    public String menu;

    public AbstractAction(String url) {
        this.url = url;
    }

    public AbstractAction(String url, String menu) {
        this.url = url;
        this.menu = menu;
    }

    public boolean isMenuItem() {
        return this.menu != null;
    }

    public abstract ActionResult performAction(Map<String, String> postData, ControllerFactory controllerFactory) throws Throwable;
}

