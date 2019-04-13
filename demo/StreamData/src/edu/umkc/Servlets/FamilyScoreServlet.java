package edu.umkc.Servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Compute.ScoreCalculator;
import edu.umkc.Stream.StreamingConsumer;

@WebServlet("/FamilyScore")
public class FamilyScoreServlet extends HttpServlet {

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
		String json = ScoreCalculator.getActAndEstScores(request.getParameter("family"), request.getParameter("function"), request.getParameter("ess"));
		logger.debug("ScoreServlet :: doWork :: Return JSON :: " + json);
		out.println(json);
	}
	
}
