package net.avacati.lib.mvc.actionresults;

import net.avacati.lib.mvc.Route;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileResult implements ActionResult {
    private final String filename;

    public FileResult(String filename) {
        this.filename = filename;
    }

    @Override
    public void createResult(Route route, HttpServletResponse response) throws IOException {
//        final FileInputStream inputStream = new FileInputStream(getFile(this.filename));
        final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
        final ServletOutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.close();
    }

    private File getFile(String fileName) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }
}
