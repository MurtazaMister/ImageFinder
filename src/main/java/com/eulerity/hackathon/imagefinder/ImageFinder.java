package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eulerity.hackathon.imagefinder.object.Image;
import com.eulerity.hackathon.imagefinder.service.ImageCrawlerService;
import com.eulerity.hackathon.imagefinder.util.UrlUtilities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
		resp.setContentType("text/json");
		String path = req.getServletPath();

		String url = req.getParameter("url");
		String recursive = req.getParameter("recursive");
		String permissibleDepth = req.getParameter("permissibleDepth");

		boolean isRecursive = recursive != null && recursive.equals("true");
		int permissibleDepthInt;
		try {
			permissibleDepthInt = Integer.parseInt(permissibleDepth);
		} catch (Exception e) {
			permissibleDepthInt = 0;
		}

        log.info("Got request of:{} with query params\n- url: {}\n- recursive: {}\n permissibleDepth: {}", path, url, recursive, permissibleDepth);

		if(UrlUtilities.isValidURL(url)) {
			ImageCrawlerService imageCrawlerService;
			if(isRecursive) {
				imageCrawlerService = new ImageCrawlerService(true, permissibleDepthInt);
			}
			else {
				imageCrawlerService = new ImageCrawlerService(false);
			}
			ConcurrentMap<String, CopyOnWriteArrayList<Image>> map = imageCrawlerService.init(url);
			resp.getWriter().print(GSON.toJson(map));
		}
		else{
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
			resp.getWriter().write("Invalid URL provided.");
		}
	}
}
