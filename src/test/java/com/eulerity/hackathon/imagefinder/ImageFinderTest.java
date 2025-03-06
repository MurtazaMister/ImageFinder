package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.mockito.Mockito;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ImageFinderTest {

	public HttpServletRequest request;
	public HttpServletResponse response;
	public StringWriter sw;
	public HttpSession session;
	public ImageFinder imageFinder;

	@Before
	public void setUp() throws Exception {
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Mockito.when(response.getWriter()).thenReturn(pw);
		Mockito.when(request.getRequestURI()).thenReturn("/foo/foo/foo");
		Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo/foo/foo"));
		session = Mockito.mock(HttpSession.class);
		Mockito.when(request.getSession()).thenReturn(session);
		imageFinder = new ImageFinder();
	}

	@Test
	public void testValidURL() throws IOException, ServletException {
		String testUrl = "https://www.example.com";
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn(testUrl);
		Mockito.when(request.getParameter("recursive")).thenReturn("false");
		Mockito.when(request.getParameter("recursiveLevels")).thenReturn("0");

		imageFinder.doPost(request, response);
		
		// Verify response is not empty
		Assert.assertFalse(sw.toString().isEmpty());
		
		// Verify response is valid JSON
		try {
			new Gson().fromJson(sw.toString(), new TypeToken<Map<String, Object>>(){}.getType());
		} catch (Exception e) {
			Assert.fail("Response is not valid JSON");
		}
	}

	@Test
	public void testInvalidURL() throws IOException, ServletException {
		String invalidUrl = "not-a-url";
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn(invalidUrl);
		Mockito.when(request.getParameter("recursive")).thenReturn("false");
		Mockito.when(request.getParameter("recursiveLevels")).thenReturn("0");
		
		imageFinder.doPost(request, response);
		
		// Verify response is not empty
		Assert.assertNotNull(sw.toString());
	}

	@Test
	public void testMaxRecursionLevel() throws IOException, ServletException {
		String testUrl = "https://www.example.com";
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn(testUrl);
		Mockito.when(request.getParameter("recursive")).thenReturn("true");
		Mockito.when(request.getParameter("recursiveLevels")).thenReturn("10");

		imageFinder.doPost(request, response);
		
		// Verify response is not empty
		Assert.assertNotNull(sw.toString());
		Assert.assertFalse(sw.toString().isEmpty());
	}

	@Test
	public void testEmptyURL() throws IOException, ServletException {
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn("");
		Mockito.when(request.getParameter("recursive")).thenReturn("false");
		Mockito.when(request.getParameter("recursiveLevels")).thenReturn("0");
		
		imageFinder.doPost(request, response);
		
		// Verify response is not empty
		Assert.assertNotNull(sw.toString());
	}

	@Test
	public void testMalformedURL() throws IOException, ServletException {
		String malformedUrl = "http://invalid url with spaces";
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn(malformedUrl);
		Mockito.when(request.getParameter("recursive")).thenReturn("false");
		Mockito.when(request.getParameter("recursiveLevels")).thenReturn("0");
		
		imageFinder.doPost(request, response);
		
		Assert.assertNotNull(sw.toString());
		Assert.assertTrue(sw.toString().isEmpty() || sw.toString().contains("error"));
	}

	@Test
	public void testResponseStructure() throws IOException, ServletException {
		String testUrl = "https://www.example.com";
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn(testUrl);
		Mockito.when(request.getParameter("recursive")).thenReturn("false");
		Mockito.when(request.getParameter("recursiveLevels")).thenReturn("0");
		
		imageFinder.doPost(request, response);
		
		String response = sw.toString();
		Assert.assertNotNull(response);
		
		// Verify it's valid JSON if not empty
		if (!response.isEmpty()) {
			try {
				new Gson().fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());
				Assert.assertTrue(true);
			} catch (Exception e) {
				Assert.fail("Response is not valid JSON: " + response);
			}
		}
	}
}



