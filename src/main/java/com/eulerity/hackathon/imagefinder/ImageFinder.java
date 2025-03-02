package com.eulerity.hackathon.imagefinder;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	private boolean isValidURL(String url) {
		try{
			new URL(url);
			log.info("Valid URL: " + url);
			return true;
		}
		catch (Exception e) {
			log.error("Invalid URL: " + url);
			return false;
		}
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");

        log.info("Got request of:{} with query param: {}", path, url);
		if(isValidURL(url)) {

			Document document = Jsoup.connect(url).get();
			Elements images = document.select("img[src]");
			List<String> imageUrls = new ArrayList<>();
			for(Element img : images) {
				String imageUrl = img.attr("src");
				if(imageUrl.startsWith("data")) continue;

				if(imageUrl.startsWith("http")) {
					log.info("URL: {}", imageUrl);
					imageUrls.add(imageUrl);
				}
				else {
					URL baseUrl = new URL(url.endsWith("/") ? url : url + "/");
					URL finalUrl = new URL(baseUrl, imageUrl);
					log.info("URL: {}", finalUrl);
					imageUrls.add(finalUrl.toString());
				}
			}
			resp.getWriter().print(GSON.toJson(imageUrls));

		}
		else{
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
			resp.getWriter().write("Invalid URL provided.");
		}
	}
}
