package com.diewebsiten.core.webservices;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.diewebsiten.core.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.model.InsightsResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Postal;

/**
 * Servlet implementation class G
 */
public class G extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public G() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			
			Log.getInstance().info("====================================================================");
			Log.getInstance().info("========================== NUEVA ENTRADA ===========================");
			Log.getInstance().info("====================================================================");
			Log.getInstance().info("");
			Log.getInstance().info("");
			Log.getInstance().info("Remote Host: " + request.getRemoteHost());
			Log.getInstance().info("Remote Locale: " + request.getLocale());
			Log.getInstance().info("Remote User: " + request.getRemoteUser());
			Log.getInstance().info("Remote User Principal: " + request.getUserPrincipal());
			String userAgent = request.getHeader("User-Agent");
			Log.getInstance().info("Remote Host Info: " + userAgent);
			Log.getInstance().info("====================================================================");
			Log.getInstance().info("========================= POR GEOLOCATION ==========================");
			Log.getInstance().info("====================================================================");
			if (null == request.getParameter("error")) {
				Log.getInstance().info("Latitud: " + request.getParameter("latitud"));
				Log.getInstance().info("Longitud: " + request.getParameter("longitud"));
			} else {
				Log.getInstance().info("No se pudo obtener la ubicación: " + request.getParameter("error"));
			}
			Log.getInstance().info("");			
			Log.getInstance().info("====================================================================");
			Log.getInstance().info("========================= POR DIRECCIÓN IP =========================");
			Log.getInstance().info("====================================================================");
			Log.getInstance().info("===================== request.getRemoteAddr() ======================");
			Log.getInstance().info(getIpLocationData(request.getRemoteAddr()));
			Log.getInstance().info("========================== X-FORWARDED-FOR =========================");
			Log.getInstance().info(getIpLocationData(request.getHeader("X-FORWARDED-FOR")));
			Log.getInstance().info("");
			if (userAgent.toLowerCase().indexOf("mobile") > -1 || userAgent.toLowerCase().indexOf("android")  > -1
					|| userAgent.toLowerCase().indexOf("iphone") > -1) {
				response.getWriter().append("Por favor intenta ver nuestras promociones desde tu computador.");
			} else {
				response.getWriter().append("Por favor intenta más tarde.");
			}
			
		} catch (Exception e) {
			Log.getInstance().imprimirErrorEnLog(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private String getIpLocationData(String ip) throws Exception {
		
		JsonObject data = new JsonObject();
		Log.getInstance().info("Ip Address --> " + ip);
		
		try {
			
			WebServiceClient client = new WebServiceClient.Builder(42, "license_key").build();
			
			InetAddress ipAddress = InetAddress.getByName(ip);
	
			// Do the lookup
			InsightsResponse response = client.insights(ipAddress);
	
			Country country = response.getCountry();
			data.add("ISO Code", new JsonPrimitive(country.getIsoCode()));            // 'US'
			data.add("Country name", new JsonPrimitive(country.getName()));               // 'United States'
			//data.add("ISO Code", new JsonPrimitive(country.getNames().get("zh-CN"))); // '美国'
			//data.add("ISO Code", new JsonPrimitive(country.getConfidence()));         // 99
	
			//Subdivision subdivision = response.getMostSpecificSubdivision();
			//System.out.println(subdivision.getName());       // 'Minnesota'
			//System.out.println(subdivision.getIsoCode());    // 'MN'
			//System.out.println(subdivision.getConfidence()); // 90
	
			City city = response.getCity();
			data.add("City name", new JsonPrimitive(city.getName()));       // 'Minneapolis'
			//System.out.println(city.getConfidence()); // 50
	
			Postal postal = response.getPostal();
			data.add("Postal Code", new JsonPrimitive(postal.getCode()));       // '55455'
			data.add("Postal confidence", new JsonPrimitive(postal.getConfidence())); // 40
	
			Location location = response.getLocation();
			data.add("Latitude", new JsonPrimitive(location.getLatitude()));        // 44.9733
			data.add("Longitude", new JsonPrimitive(location.getLongitude()));       // -93.2323
			data.add("Accuracy radius", new JsonPrimitive(location.getAccuracyRadius()));  // 3
			data.add("Time zone", new JsonPrimitive(location.getTimeZone()));        // 'America/Chicago'
	
			data.add("User type", new JsonPrimitive(response.getTraits().getUserType())); // 'college'
			
		} catch (Exception e) {
			data.add("ERROR", new JsonPrimitive(e.toString()));
		}
		
		return data.toString();
		
	}

}
