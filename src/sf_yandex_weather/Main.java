package sf_yandex_weather;

public class Main {
    
    private static void showHelp() {
	System.out.println("Help \\TODO");
    }

    public static void main(String[] args) {
	InputPrms prms = new InputPrms(args);
	
	if (prms.isError()) {
	    System.out.format("Error: %s\n", prms.getError());
	    return;
	}
	
	if (prms.isHelp()) {
	    showHelp();
	    return;
	}
	
	if (!prms.isApi()) {
	    System.out.println("Error: API file is mandatory");
	    return;
	}
	
	if (!prms.isLatLon()) {
	    System.out.println("Error: LatLon parameter is mandatory");
	    return;
	}
	
	System.out.format("Api file is: %s\n", prms.getApiFile().getName());
	System.out.format("Latitude: %.04f\n", prms.getLat());
	System.out.format("Longitude: %.04f\n", prms.getLon());
	
	if (prms.isLimit()) {
	    System.out.format("Limit: %d\n", prms.getLimit());
	}
    }

}
