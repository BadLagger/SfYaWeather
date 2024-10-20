package com.sf.ya.weather;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Locale;

class Weather {

	private String lastRawData = "";
	private String apiKey = "";
	private float lat = 0f;
	private float lon = 0f;
	private int limit = 1;

	private final String URL = "https://api.weather.yandex.ru/v2/forecast?";
	private final String HEADER_KEY = "X-Yandex-Weather-Key";

	public Weather() {

	}
	
	/* !\brief Set API KEY from String
	 * !\param[in] API key
	 * ***********************************************/
	public void setApiKey(String key) {
		apiKey = key;
	}

	/* !\brief Set API KEY from File
	 * !\param[in] file with API KEY (Should contain only one string with API key)
	 * !\return true on success, false on error (if file contains greater lines than one or 0)
	 * ***********************************************/
	public boolean setApiKey(File fileKey) {

		boolean ret = true;
		String line = "";

		try {
			Scanner scanner = new Scanner(fileKey);
			int lineCount = 0;

			while (scanner.hasNext()) {
				line = scanner.nextLine();

				if (lineCount >= 1) {
					ret = false;
					break;
				}
			}
			scanner.close();
		} catch (Exception err) {
			ret = false;
		}

		if (ret) {
			apiKey = line;
		}

		return ret;
	}

	
	/* !\brief Set Latitude and Longitude
	 * !\param[in] latitude (Should be in range -90 to +90 degrees)
	 * !\param[in] longitude (Should be in range -180 to +180 degrees)
	 * !\return true on success, false on error (if one of values out of range)
	 * ***********************************************/
	public boolean setLatLon(float lat, float lon) {

		if ((lat > 90.) || (lat < -90.) || (lon > 180.) || (lon < -180.)) {
			return false;
		}

		this.lat = lat;
		this.lon = lon;

		return true;
	}

	/* !\brief Set Limit parameter for days
	 * !\param[in] limit (Should greater than 0)
	 * * !\return true on success, false on error (if value out of range)
	 * ***********************************************/
	public boolean setLimit(int limit) {

		if (limit > 0) {
			this.limit = limit;
			return true;
		}
		
		return false;
	}

	/* !\brief Make request to the service through REST API
	 * !\return true on success, false on error
	 * *************************************************/
	public boolean makeRESTRequest() {
		String uri_str = String.format(Locale.ENGLISH, "%slat=%.05f&lon=%.05f&limit=%d&hours=false&extra=false", URL, lat, lon, limit);

		System.out.println(uri_str);

		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(uri_str)).header(HEADER_KEY, apiKey).GET()
					.build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

			if (resp.statusCode() == 200) {
				lastRawData = resp.body();
			} else {
				return false;
			}

		} catch (Exception err) {
			System.out.format("Error: %s\n", err.getMessage());
			return false;
		}

		return true;
	}

	/* !\brief Return last response from service in raw format
	 * !\return string with response
	 * *************************************************/
	public String getRawLastData() {
		return lastRawData;
	}
	
	/* !\brief Return pretty formated last response from service
	 * !\return string with response or string with error message if response not a JSON
	 * *************************************************/
	public String getPrettyJsonLastData() {
		ObjectMapper objectMapper = new ObjectMapper();
		String ret = "";
		
		try {
			Object jsonObject = objectMapper.readValue(lastRawData, Object.class);
			ret = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
		} catch (Exception err) {
			ret = err.getMessage();
		}
		
		return ret;
	}

	/* !\brief Return current temperature value from last response from service
	 * !\return int with temperature value (may throw exception if JSON error occurs)
	 * *************************************************/
	public int getLastTemp() throws Exception {
		JsonNode rootNode = new ObjectMapper().readTree(lastRawData);
		JsonNode factNode = rootNode.get("fact");
		return factNode.get("temp").asInt();
	}

	/* !\brief Return average temperature value from last response from service
	 * !\return int with average temperature value (may throw exception if JSON error occurs)
	 * *************************************************/
	public int getAvgTemp() throws Exception {
		JsonNode rootNode = new ObjectMapper().readTree(lastRawData);
		JsonNode forecastsNode = rootNode.get("forecasts");
		Iterator<JsonNode> fc_iterate = forecastsNode.elements();
		int fullAvgTemp = 0;
		int dayCount = 0;
		
		while(fc_iterate.hasNext()) {
			JsonNode fc_element = fc_iterate.next();
			int dayTemp = fc_element.get("parts").get("day_short").get("temp").asInt();
			int nightTemp = fc_element.get("parts").get("night_short").get("temp").asInt();
			int avgTemp = (dayTemp + nightTemp)/2;

			fullAvgTemp += avgTemp;
			dayCount ++;
		}
		fullAvgTemp /= dayCount;
		return fullAvgTemp;
	}
	
	/* !\brief Save last response from service to file (for debug purposes)
	 * !\param[in] string with path name of file
	 * !\return true on success, false on error
	 * *************************************************/
	public boolean saveFromLastDataToFile(String filename) {
		boolean ret = true;
		
		try {
			File file = new File(filename);
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(lastRawData.getBytes());
			outStream.close();
		} catch (Exception err) {
			ret = false;
		}
		
		return ret;
	}
	
	/* !\brief Load from file data as last response from service (for debug purposes)
	 * !\param[in] string with path name of file
	 * !\return true on success, false on error
	 * *************************************************/
	public boolean loadToLastDataFromFile(String filename) {
		boolean ret = true;
		
		try {
			File file = new File(filename);
			FileInputStream inStream = new FileInputStream(file);
			byte[] readData = inStream.readAllBytes();
			lastRawData = new String(readData);
			inStream.close();
		} catch (Exception err) {
			
			ret = false;
		}	
		
		return ret;
	}
}

public class Main {
	
	public static void main(String[] args) {
		Weather weather = new Weather();

		weather.setApiKey("53d476cb-893a-4192-a896-d45b90729469");
		if (!weather.setLatLon(55.7522f, 37.6156f)) {
			System.out.println("Error: LatLon out of range");
			return;
		}
		
		if (!weather.setLimit(6)) {
			System.out.println("Error: Limit out of range");
			return;
		}

		if (!weather.makeRESTRequest()) {
			System.out.println("Error: bad request");
			return;
		}
		
		System.out.println(weather.getPrettyJsonLastData());
		
		try {
			System.out.format("Temp: %d\n", weather.getLastTemp());
			System.out.format("AvgTemp: %d\n", weather.getAvgTemp());
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

}
