package sf_yandex_weather;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.Locale;

class Weather {
    
    private String lastRawData = "";
    private String apiKey = "";
    private float lat = 0f;
    private float lon = 0f;
    private int limit = 0;
    
    private final String URL = "https://api.weather.yandex.ru/v2/forecast?";
    private final String HEADER_KEY = "X-Yandex-Weather-Key";
    
    public Weather() {
	
    }
    
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
    
    public boolean setLatLon(float lat, float lon) {
	
	if ((lat > 90.) || (lat < -90.) || (lon > 180.) || (lon < -180.) ) {
	    return false;
	}
	
	this.lat = lat;
	this.lon = lon;
	
	return true;
    }
    
    public boolean setLimit(int limit) {
	
	//! TODO: checking limit ???
	this.limit = limit;
	
	return true;
    }
    
    public boolean makeRESTRequest() {
	String uri_str = String.format(
		Locale.ENGLISH, "%slat=%.05f&lon=%.05f", URL, lat, lon);
	
	if (limit > 0) {
	    uri_str += String.format(Locale.ENGLISH, "&limit=%d", limit);
	}
	
	//System.out.println(uri_str);
	
	try {
	    HttpClient httpClient = HttpClient.newHttpClient();
	    HttpRequest req = HttpRequest
		.newBuilder()
		.uri(URI.create(uri_str))
		.header(HEADER_KEY, apiKey)
		.GET()
		.build();
	    
	    
	    HttpResponse<String> resp = httpClient.send(req, 
		    HttpResponse.BodyHandlers.ofString());
	    
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
    
    public String getRawLastData() {
	return lastRawData;
    }
    
    public String getLastTemp() {
	return "";
    }
    
    public String getAvgTemp() {
	return "";
    }
}

public class Main {
    
    private static void prettyPrintJson(String str) {
    }
    
    private static void showHelp() {
	System.out.println(
		"Help: Oops! This feature in development state!  Sorry!");
    }

    public static void main(String[] args) {
	InputPrms prms = new InputPrms(args);
	Weather weather = new Weather();
	
	if (prms.isError()) {
	    System.out.format("Error: %s\n", prms.getError());
	    return;
	}
	
	//! TODO: Help message
	if (prms.isHelp()) {
	    showHelp();
	    return;
	}
	
	if (!prms.isApi()) {
	    System.out.println("Error: API file is mandatory");
	    return;
	}
	
	if (!weather.setApiKey(prms.getApiFile())) {
	    System.out.println("Error: bad API file format");
	    return;
	}
	
	if (!prms.isLatLon()) {
	    System.out.println("Error: LatLon parameter is mandatory");
	    return;
	}
	
	if (!weather.setLatLon(prms.getLat(), prms.getLon())) {
	    System.out.println("Error: LatLon out of range");
	    return;
	}
	
	System.out.format(Locale.ENGLISH, "Api file is: %s\n", 
		prms.getApiFile().getName());
	System.out.format(Locale.ENGLISH, "Latitude: %.04f\n", 
		prms.getLat());
	System.out.format(Locale.ENGLISH, "Longitude: %.04f\n", 
		prms.getLon());
	
	if (prms.isLimit()) {
	    System.out.format("Limit: %d\n", prms.getLimit());
	}
	
	weather.makeRESTRequest();
    }

}
