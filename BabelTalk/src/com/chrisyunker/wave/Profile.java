package com.chrisyunker.wave;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class Profile extends ProfileServlet
{
	public static final String BASEURL = "http://babel-talk.appspot.com/";
	public static final String BASEURL_IMG = BASEURL + "img/";
	
	@Override
	public String getRobotAvatarUrl()
	{
		return BASEURL_IMG + "babel.jpg";
	}
	
	@Override
	public String getRobotName()
	{
		return "Babel Talk";
	}
}
