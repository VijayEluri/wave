package com.chrisyunker.wave;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.Image;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

@SuppressWarnings("serial")
public class BabelRobot extends AbstractRobotServlet
{
	static private final String FLICKR_API_KEY = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
	
	static public final String FLICKR_API_OK = "ok";
	static public final String FLICKR_API_FAIL = "fail";
	
	private Pattern pattern = Pattern.compile("pic:([\\w\\d\\s\\,]+)", Pattern.CASE_INSENSITIVE);
	

	@Override
	public void processEvents(RobotMessageBundle bundle)
	{	
		Wavelet wavelet = bundle.getWavelet();
				
		for (Event e : bundle.getEvents())
		{
			if (e.getType() == EventType.BLIP_SUBMITTED)
			{
				Blip eventBlip = e.getBlip();
				if (eventBlip != null)
				{
					Matcher matcher = pattern.matcher(eventBlip.getDocument().getText());
					if (matcher.find())
					{
						Blip blip = wavelet.appendBlip();
						TextView tv = blip.getDocument();
						String tags = matcher.group(1).replace("\n", "");
						tv.append("Getting picture for tag(s) ["+ tags + "]\n");
						requestPicture(tags, tv);
					}
				}	
			}
		}		
	}
	
	private void requestPicture(String tags, TextView tv)
	{
		String encodedTags;
		try
		{
			encodedTags = URLEncoder.encode(tags, "UTF-8");
		}
		catch (Exception e)
		{
			tv.append("Failed picture request; Bad tag(s) ["+ e.getMessage() +"]");
			return;
		}
		
		String requestUrl =
			"http://api.flickr.com/services/rest/?"+
			"format=json&"+
			"method=flickr.photos.search&"+
			"api_key="+ FLICKR_API_KEY +"&"+
			"per_page=3&"+
			"extras=url_sq,path_alias,url_s&"+
			"tags="+ encodedTags +"&"+
			"tag_mode=all&"+
			"nojsoncallback=1";
		
		String jsonText;
		
		// Request Flickr picture data
		try
		{
			URL url = new URL(requestUrl);
			URLConnection conn = url.openConnection();
			BufferedReader in =
				new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
	        jsonText = in.readLine();	        
	        in.close();
		}
		catch (Exception e)
		{
			tv.append("Failed picture request ["+ e.getMessage() +"]");
			return;
		}
		
		// Parse returned JSON data
		try
		{
			JSONObject  obj = new JSONObject(jsonText);
			
			String status = obj.get("stat").toString().replace("\"", "");
			if (!status.equalsIgnoreCase(FLICKR_API_OK))
			{
				String message = obj.get("message").toString().replace("\"", "");
				
				tv.append("Failed picture request by Flickr ["+ message +"]");
				return;
			}
					
			// Drill down to photo array
			JSONArray photos = obj.getJSONObject("photos").getJSONArray("photo");
			
			JSONObject photo = photos.getJSONObject(0);
			
			String title = photo.get("title").toString().replace("\"", "");
			String photoUrl = photo.get("url_s").toString().replace("\"", "");		
			int height = Integer.parseInt(photo.get("height_s").toString().replace("\"", ""));
			int width = Integer.parseInt(photo.get("width_s").toString().replace("\"", ""));
			
			Image image = new Image(photoUrl, width, height, title);
			tv.appendElement(image);
			
			
			photo = photos.getJSONObject(1);
			
			title = photo.get("title").toString().replace("\"", "");
			photoUrl = photo.get("url_s").toString().replace("\"", "");		
			height = Integer.parseInt(photo.get("height_s").toString().replace("\"", ""));
			width = Integer.parseInt(photo.get("width_s").toString().replace("\"", ""));
			
			image = new Image(photoUrl, width, height, title);
			tv.appendElement(image);
			
			
			photo = photos.getJSONObject(2);
			
			title = photo.get("title").toString().replace("\"", "");
			photoUrl = photo.get("url_s").toString().replace("\"", "");		
			height = Integer.parseInt(photo.get("height_s").toString().replace("\"", ""));
			width = Integer.parseInt(photo.get("width_s").toString().replace("\"", ""));
			
			image = new Image(photoUrl, width, height, title);
			tv.appendElement(image);
		}
		catch (JSONException e)
		{
			tv.append("Failed picture request ["+ e.getMessage() +"]");
			return;
		}
	}
}
