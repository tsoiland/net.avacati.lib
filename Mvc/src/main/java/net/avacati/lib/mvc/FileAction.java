package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.FileResult;

import java.util.Map;

public class FileAction extends AbstractAction {
    private String filename;

    public FileAction(String url, String filename) {
        super(url);
        this.filename = filename;
    }

    @Override
    public ActionResult performAction(Map<String, String> postData, ControllerFactory controllerFactory) throws Throwable {
        return new FileResult(this.filename);
    }
}
