package net.osmand.exceptionanalyzer.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 28.05.17.
 */
public class ExceptionText {
    private String date;
    private String name;
    private String body;
    private String apkVersion;

    public ExceptionText(String date, String name, String body, String apkVersion) {
        this.date = date;
        this.name = name;
        this.body = body;
        this.apkVersion = apkVersion;
    }
    
    public String getExceptionHash() {
    	String hsh = name;
    	String[] lines = body.split("\n");
    	int top = 3;
    	int i = 0;
    	while(top > 0 && i < lines.length) {
    		if(lines[i].contains("net.osmand")) {
    			hsh += "\n" + lines[i];
    			top --;
    		}
    		i++;
    	}
    	
    	top= 3;
    	i = 0;
    	while(top > 0 && i < lines.length) {
    			hsh += "\n" + lines[i];
    		top --;
    		i++;
    	}
    	return hsh;
    }

    public String getName() {
        return String.valueOf(name);
    }

    public List<String> getAllInfo() {
        List<String> info = new ArrayList<>();
        info.add(name);
        info.add(apkVersion);
        info.add(date);
        return Collections.unmodifiableList(info);
    }

    public String getStackTrace() {
        return String.valueOf(body);
    }

    @Override
    public boolean equals(Object thatObject) {
        if (thatObject instanceof ExceptionText) {
            ExceptionText exceptionText = (ExceptionText) thatObject;
            if (name.equals(exceptionText.getName()) && body.equals(exceptionText.getStackTrace())) {
                return true;
            }
        }
        return false;
    }
}
