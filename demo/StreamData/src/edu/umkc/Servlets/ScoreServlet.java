package edu.umkc.Servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Compute.ScoreCalculator;
import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Stream.StreamingConsumer;
import edu.umkc.Util.CommonUtil;
import edu.umkc.Util.PropertyReader;

@WebServlet("/CalcScore")
public class ScoreServlet extends HttpServlet {

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
		logger.debug("ScoreServlet :: doWork :: Start");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		String family = PropertyReader.getInstance().getProperty(DiSCConstants.FAMILY);
		String scoringFunction = PropertyReader.getInstance().getProperty(DiSCConstants.SCORING_FUNCTION);
		String ess = PropertyReader.getInstance().getProperty(DiSCConstants.ESS);

		// Find out the node responsible for the Family and get the score from the node.
		if (PropertyReader.getInstance().getProperty(DiSCConstants.NODE_NUM).equals("1")) {
			try (BufferedReader br = new BufferedReader(new FileReader(new File(DiSCConstants.CONFIG_FILE)))) {
				String line = null;
				boolean isNode1 = false;
				while ((line = br.readLine()) != null) {
					if(line.contains("_gspfams")) {
						String[] inp = line.split("=");
						String[] families = inp[1].split(";");
						for(String fam : families) {
							if(family.equals(fam)) {
								String nodeIp = CommonUtil.getNodeIp(inp[0]);
								logger.debug("ScoreServlet :: doWork :: nodeIp :: " + nodeIp);
								if(DiSCConstants.IP_1.equals(nodeIp)) {
									logger.debug("ScoreServlet :: doWork :: Node 1 is responsible for the family");
									isNode1 = true;
									break;
								}
								URLConnection connection = new URL("http://" + nodeIp + ":8080/StreamData/CalcScore")
										.openConnection();
								connection.setRequestProperty("Accept-Charset", "UTF-8");
								try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
									String inputLine;
									while ((inputLine = in.readLine()) != null) {
										logger.debug(inputLine);
										response.setContentType("application/json");
										out.println(inputLine);
										return;
									}
								} catch(Exception e) {
									logger.error("ScoreServlet :: Exception encountered while reading the input stream :: " + e);
									e.printStackTrace();
								}
								//In order to compute the score from the first responsible node.
								break;
							}
							if(isNode1) {
								break;
							}
						}
					}					
				}
			} catch (Exception e) {
				logger.error("ScoreServlet :: Exception encountered while reading the config file :: " + e);
				e.printStackTrace();
			}
		}
		
		String json = ScoreCalculator.getActAndEstScores(family, scoringFunction, ess);
		logger.debug("ScoreServlet :: doWork :: Return JSON :: " + json);
		out.println(json);
	}
	
}
