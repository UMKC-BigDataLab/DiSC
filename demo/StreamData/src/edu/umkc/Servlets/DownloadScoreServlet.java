package edu.umkc.Servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umkc.Util.PropertyReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Util.CommonUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@WebServlet("/DownloadScores")
public class DownloadScoreServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DownloadScoreServlet.class.getName());
	private static Map<String, String> familyMap = new HashMap<String, String>();
	
	public void init() {
		try (BufferedReader br = new BufferedReader(new FileReader(new File(DiSCConstants.CONFIG_FILE)))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line.contains("_gspfams")) {
					String[] inp = line.split("=");
					String[] families = inp[1].split(";");
					for(String family : families) {
						familyMap.put(family, inp[0]);
					}
				}
			}
			logger.debug("DownloadScoreServlet :: init :: familyMap :: " + familyMap);
		} catch(Exception e) {
			logger.error("ScoreServlet :: Exception encountered while reading the config file :: " + e);
			e.printStackTrace();
		}
 
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doWork(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doWork(request, response);
	}

	protected void doWork(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("ScoreServlet :: doWork :: Start");
		response.setHeader("Access-Control-Allow-Origin", "*");
		init();

		String scoringFunction = PropertyReader.getInstance().getProperty(DiSCConstants.SCORING_FUNCTION);
		String ess = PropertyReader.getInstance().getProperty(DiSCConstants.ESS);
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/users/arung/jetty/webapps/families/FamilyScores.txt")))) {
			//Iterate over each and every family.
			for(String family : familyMap.keySet()) {
				String nodeIp = CommonUtil.getNodeIp(familyMap.get(family));
				URLConnection connection = new URL("http://" + nodeIp + ":8080/StreamData/FamilyScore?family=" + family + "&function=" + scoringFunction + "&ess=" + ess)
						.openConnection();
				connection.setRequestProperty("Accept-Charset", "UTF-8");
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						HashMap<String,String> map = new Gson().fromJson(inputLine, new TypeToken<HashMap<String, String>>(){}.getType());
						bw.write(family + " :: " + map.get("EstScore") + "\n");
						bw.flush();
					}
				} catch(Exception e) {
					logger.error("ScoreServlet :: Exception encountered while reading the input stream :: " + e);
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			logger.error("DownloadScoreServlet :: doWork :: Exception encountered while writing to the file");
			e.printStackTrace();
		}
	}
	
}
