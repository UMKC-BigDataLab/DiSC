package edu.umkc.Servlets;

import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Stream.StreamingConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/TriggerUpload")
public class TriggerUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(TriggerUploadServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doWork(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doWork(request, response);
    }

    protected void doWork(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("TriggerUploadServlet :: doWork :: Start");
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"bash","-c", DiSCConstants.UPLOAD_CMD});
        } catch (Exception e) {
            logger.debug("TriggerUploadServlet :: doWork :: Exception encountered while executing the script");
            e.printStackTrace();
        }

    }
}
