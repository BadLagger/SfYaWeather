package com.sf.ya.weather;

import java.io.File;

public class InputPrms {
    
    private enum Errors {
	
	None("None"),
	WrongPrm("Wrong Parameter"),
	BadApiFile("Bad API file"),
	BadLimitVal("Bad limit value"),
	BadLatlonValue("Bad LatLon value");
	
	private String msg;
	
	Errors(String m) { msg = m; }
	String getMsg() { return msg; }
    }
    
    private enum Prms {
	
	ApiPath("-api"),
	Help("-help"),
	LatLon("-latlon"),
	Limit("-limit"),
	None("");
	
	private String val;
	
	Prms(String v) { val = v; }
	String getStr() { return val; }
    }
    
    private Errors error = Errors.None;
    
    private boolean apiFlag    = false;
    private boolean helpFlag   = false;
    private boolean latlonFlag = false;
    private boolean limFlag    = false;
    
    private File    apiFile;
    private float[] latlonVal = {0f, 0f};
    private int     limit     = 0;
    
    
    public InputPrms(String[] prms) {
	
	boolean getValue = false;
	Prms curPrm = Prms.None;
	
	for (String inPrm : prms) {
	    
	    if (error != Errors.None)
		break;
	    
	    if (getValue) {
		error = getPrmVal(curPrm, inPrm);
		getValue = false;
		continue;
	    }
	    
	    for (Prms prm : Prms.values()) {
		
		if (prm == Prms.None) {
		    error = Errors.WrongPrm;
		    break;
		}
		
		if (inPrm.equals(prm.getStr())) {
		    curPrm = prm;
		    if (prm != Prms.Help) {
			getValue = true;
		    } else {
			helpFlag = true;
		    }
		    break;
		}
	    }
	    
	}
    }
    
    public boolean isError() {
	return (error == Errors.None) ? false : true;
    }
    
    public String getError() {
	return error.getMsg();
    }
    
    public boolean isHelp() {
	return helpFlag;
    }
    
    public boolean isApi() {
	return apiFlag;
    }
    
    public boolean isLimit() {
	return limFlag;
    }
    
    public boolean isLatLon() {
	return latlonFlag;
    }
    
    public File getApiFile() {
	return apiFile;
    }
    
    public int getLimit() {
	return limit;
    }
    
    public float getLat() {
	return latlonVal[0];
    }
    
    public float getLon() {
	return latlonVal[1];
    }
    
    private Errors getPrmVal(Prms prm, String val) {
	Errors error = Errors.None;
	
	switch(prm) {
	case ApiPath:
	    apiFile = new File(val);
	    if (apiFile.exists() && apiFile.isFile())
		apiFlag = true;
	    else {
		apiFlag = false;
		error = Errors.BadApiFile;
	    }
	    break;
	case Limit:
	    try {
		limit = Integer.parseInt(val);
		limFlag = true;
	    } catch (NumberFormatException err) {
		limFlag = false;
		error = Errors.BadLimitVal;
	    }
	    break;
	case LatLon:
	    String[] latlon_arr = val.split(":");
	    if (latlon_arr.length == 2) {
		try {
		    latlonVal[0] = Float.parseFloat(latlon_arr[0]);
		    latlonVal[1] = Float.parseFloat(latlon_arr[1]);
		    latlonFlag = true;
		} catch (NumberFormatException err) {
		    latlonFlag = false;
		}
	    } else {
		latlonFlag = false;
	    }
	    error = (!latlonFlag) ? Errors.BadLatlonValue : Errors.None;
	    break;
	default:
	    error = Errors.WrongPrm;
	}
	return error;
    }
}
