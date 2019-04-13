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
import edu.umkc.Util.PropertyReader;

@WebServlet("/AvgSummary")
public class AvgSummaryServlet extends HttpServlet {

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
		Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
		logger.debug("SummaryServlet :: getSummary :: Start");
		int noOfNodes = Integer.parseInt(PropertyReader.getInstance().getProperty(DiSCConstants.NUM_OF_SLAVES)) + 1;
		String dirPath = DiSCConstants.INPUT_DATA_DIR + DiSCConstants.FILE_SEPERATOR + PropertyReader.getInstance().getProperty(DiSCConstants.EXP_NAME) + "." + noOfNodes + ".r" + PropertyReader.getInstance().getProperty(DiSCConstants.R_VAL) + ".k" + PropertyReader.getInstance().getProperty(DiSCConstants.K_VAL);  
		for(int i=1; i<=noOfNodes; i++) {
			try(BufferedReader br = new BufferedReader(new FileReader(new File(dirPath + DiSCConstants.FILE_SEPERATOR + i + ".txt")))) {
				logger.debug("AvgSummaryServlet :: getSummary :: inputFile :: " + dirPath + DiSCConstants.FILE_SEPERATOR + i + ".txt");
				Map<String, String> dataMap = new LinkedHashMap<String, String>();
				String line = null;
				while((line = br.readLine()) != null) {
					if(!line.contains("Node")) {
						String[] points = line.split("	");
						dataMap.put(points[0], points[1]);
					}
				}
				map.put("Node " + i, dataMap);
			} catch(Exception e) {
				logger.error("Exception encountered while reading the " + i  + " th file");
				e.printStackTrace();
			}
		}

		Gson gson = new Gson(); 
	    String json = gson.toJson(map);
	    return json;		
	}
	
}
