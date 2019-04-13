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

@WebServlet("/OverallStat")
public class OverallStatServlet extends HttpServlet {

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
		logger.debug("OverallStatServlet :: doWork :: Start");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		
		String json = getOverallStat();
		logger.debug("OverallStatServlet :: doWork :: Return JSON :: " + json);
		out.println(json);
	}
	
	public static String getOverallStat() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		int noOfNodes = Integer.parseInt(PropertyReader.getInstance().getProperty(DiSCConstants.NUM_OF_SLAVES)) + 1;
		String inFile = DiSCConstants.OUTPUT_DATA_DIR + DiSCConstants.FILE_SEPERATOR + PropertyReader.getInstance().getProperty(DiSCConstants.EXP_NAME) + DiSCConstants.FILE_SEPERATOR + PropertyReader.getInstance().getProperty(DiSCConstants.EXP_NAME) + "." + noOfNodes + ".r" + PropertyReader.getInstance().getProperty(DiSCConstants.R_VAL) + ".k" + PropertyReader.getInstance().getProperty(DiSCConstants.K_VAL);  
		try (BufferedReader br = new BufferedReader(new FileReader(new File(inFile + DiSCConstants.FILE_SEPERATOR  + DiSCConstants.OUTPUT_FILE)))) {
			String in = null;
			while((in = br.readLine()) != null) {
				logger.error("OverallStatServlet :: in :: " + in);
				if(in.contains("Ave Size Before Compression")) {
					map.put("Avg. message size (before compression): ", in.split(":")[1].trim());
				} else if(in.contains("Ave Size After Compression")) {
					map.put("Avg. message size (after compression): ", in.split(":")[1].trim());
				} else if(in.contains("Ave Size Reduction")) {
					map.put("% reduction in size: ", in.split(":")[1].trim());
				} else if(in.contains("Amount of Data Sent")) {
					in = br.readLine();
					Long size = Long.parseLong(in.split(":")[1].trim())/1000000000;
					map.put("Network bandwidth consumption: ", size + "GB");
				} 
			}
		} catch (Exception e) {
			logger.error("OverallStatServlet :: getSummary :: Exception encountered while calculating Estimated Score :: " + e);
			e.printStackTrace();
		}
		
		Gson gson = new Gson(); 
	    String json = gson.toJson(map);
	    return json;		
	}
	
}
