package edu.umkc.Servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Stream.StreamingConsumer;

@WebServlet("/Summary")
public class SummaryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StreamingConsumer.class.getName());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doWork(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doWork(request, response);
	}

	protected void doWork(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("SummaryServlet :: doWork :: Start");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		
		String json = getSummary();
		logger.debug("SummaryServlet :: doWork :: Return JSON :: " + json);
		out.println(json);
	}
	
	public static String getSummary() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		logger.debug("SummaryServlet :: getSummary :: Start");
		try (BufferedReader br = new BufferedReader(new FileReader(new File(DiSCConstants.EST_C_SUM_FILE)))) {
			String in = null;
			while((in = br.readLine()) != null) {
				String[] arr = in.split(",");
				map.put(arr[0], arr[1]);
			}
		} catch (Exception e) {
			logger.error("SummaryServlet :: getSummary :: Exception encountered while calculating Estimated Score:: " + e);
			e.printStackTrace();
		}

		Gson gson = new Gson(); 
	    String json = gson.toJson(map);
	    return json;		
	}
	
}
