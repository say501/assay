package com.lgcns.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RunManager {

	static RunManager rm;
	Map<String, List<String>> queue = new LinkedHashMap<String, List<String>>();
	
	public RunManager() {
		rm = this;
	}
	
	public void push(String key, String value) {
		List<String> tmp = queue.get(key);
		if (tmp != null) {
			tmp.add(value);
		} else {
			List<String> tmp2 = new ArrayList<String>();
			tmp2.add(value);
			queue.put(key, tmp2);
		}
	}
	
	public String pop(String key) {
		List<String> tmp = queue.get(key);
		if (tmp == null || tmp.size() ==0) {
			return null;
		}
		return tmp.remove(0);
	}
	
	public List<String> keys() {
		List<String> result = new ArrayList<String>();
		Set<String> keys = this.queue.keySet();
		for (String key : keys) {
			result.add(key);
		}
		return result;
	}
	
	public String values(String key) {
		String result = "";
		List<String> values = this.queue.get(key);
		if (values == null || values.size() == 0) {
			return null;
		}
		for (String value : values) {
			result += "\"" + value + "\",";
		}
		result = result.substring(0,  result.length() - 1);
		return result;
	}
	
	public static class WebServer {
	
		public WebServer() {
			
		}
		
		public void start() throws Exception {
			Server server = new Server();
			ServerConnector http = new ServerConnector(server);
			http.setHost("127.0.0.1");
			http.setPort(8080);
			server.addConnector(http);
			
			ServletHandler servletHandler = new ServletHandler();
			servletHandler.addServletWithMapping(MyServlet.class, "/helloworld");
			server.setHandler(servletHandler);
			
			server.start();
		}
		
		public static class MyServlet extends HttpServlet {
		
			public MyServlet() {
			}
			
			protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
				res.setStatus(200);
				String result = "{";
				for (String key : rm.keys()) {
					result += "\"" + key + "\":[" + rm.values(key) + "],";
				}
				result = result.substring(0, result.length() - 1) + "}";
				res.getWriter().write(result);
			}
			
			protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
				res.setStatus(200);
				res.getWriter().write("{\"key\":\"Hello World\"}");
			}
		}
	}
	
	public static class MyClient {
		
		public void test() throws Exception {
			HttpClient client = new HttpClient();
			client.start();
			ContentResponse res = client.newRequest("http://localhost:8080/helloworld").method(HttpMethod.GET).send();
			
			JsonElement element = JsonParser.parseString(res.getContentAsString());
			System.out.println(element.toString());
			JsonObject obj = (JsonObject) JsonParser.parseString(res.getContentAsString());
			Set<String> a = obj.keySet();
			for (String b : a) {
				System.out.println(b + " = " + obj.get(b).toString());
			}
		}
	}
	

	public static void main(String[] args) throws IOException {
		RunManager rm = new RunManager();
		
		int key;
		while (true) {
			String inData = "";
			while ((key = System.in.read()) != 13) {
				inData += Character.toString((char) key);
			}
			String data = inData.trim();
			if (data.equals("END")) {
				break;
			}
			
			String[] datas = data.split(" ");
			
			try {
				switch(datas[0]) {
					case "PUSH":
						rm.push(datas[1], datas[2]);
						break;
					case "POP":
						String value = rm.pop(datas[1]);
						System.out.println(value);
						break;
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			new WebServer().start();
			new MyClient().test();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
