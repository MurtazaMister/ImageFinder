package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eulerity.hackathon.imagefinder.service.ImageCrawlerService;
import com.eulerity.hackathon.imagefinder.util.UrlUtilities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
@Slf4j
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	//This is just a test array
	public static final String[] testImages = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
  	};

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/event-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Cache-Control", "no-cache");
		resp.setHeader("Connection", "keep-alive");

		String path = req.getServletPath();

		String url = req.getParameter("url");
		String recursive = req.getParameter("recursive");
		String recursiveLevels = req.getParameter("recursiveLevels");

		PrintWriter writer = resp.getWriter();

		boolean isRecursive = recursive != null && recursive.equals("true");
		int recursiveLevelsInt;
		try {
			recursiveLevelsInt = Integer.parseInt(recursiveLevels);
		} catch (Exception e) {
			recursiveLevelsInt = 0;
		}

        log.info("Got request of:{} with query params\n- url: {}\n- recursive: {}\n recursiveLevels: {}", path, url, recursive, recursiveLevels);

		if(UrlUtilities.isValidURL(url)) {
			ImageCrawlerService imageCrawlerService;
			if(isRecursive) {
				imageCrawlerService = new ImageCrawlerService(true, recursiveLevelsInt);
			}
			else {
				imageCrawlerService = new ImageCrawlerService(false);
			}
			url = UrlUtilities.normalizeUrl(url);
			imageCrawlerService.init(url, writer);
		}
		else{
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
			writer.write("error: Invalid URL");
			writer.flush();
			writer.close();
		}
	}
}
