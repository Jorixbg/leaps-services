package com.leaps.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leaps.model.exceptions.InvalidCredentialsException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.token.Token;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

@RestController
@RequestMapping(value = "/login")
public class LoginController {
	
	/**
	 * User login method
	 * 
		{
		  "name" : "mityo_krika@mail.bg".
		  "password" : "yakatarabota"
		}
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Object login(HttpServletRequest req, HttpServletResponse resp) {
		String userData = null;
		String pass = null;
		String hashedPass = null;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			String requestData = sb.toString();

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("name") == null || obj.get("password") == null) {
				throw new InvalidCredentialsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			userData = obj.get("name").getAsString();
			pass = obj.get("password").getAsString();
			hashedPass = LeapsUtils.convertToMd5(pass);
			Map<Token, User> data = UserDao.getInstance().getUserFromDbOrCache(userData, hashedPass, null, null);
			
			if (data != null && data.isEmpty()) {
				throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
			}

			JsonObject userId = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    userId.addProperty("user_id", pair.getValue().getUserId());
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}
			
			return userId.toString();
		} catch (InvalidCredentialsException ice) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ice.getMessage());
				return null;
			} catch (IOException ioe1) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe1) {
				return null;
			}
		} catch (IOException e) {
			try {
				// TODO: write propper response message
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Facebook login method
	 * 
		{
		  "fb_id" : 13452512645
		}
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/fb")
	@ResponseBody
	public Object fbLogin(HttpServletRequest req, HttpServletResponse resp) {
		String fbId = null;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("fb_id") == null) {
				throw new InvalidCredentialsException(Configuration.FACEBOOK_LOGIN_ERROR);
			}

			fbId = obj.get("fb_id").getAsString();
			
			Map<Token, User> data = UserDao.getInstance().getUserFromDbOrCache(null, null, fbId, null);
			
			if (data != null && data.isEmpty()) {
				throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
			}

			JsonObject userId = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    userId.addProperty("user_id", pair.getValue().getUserId());
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}
			
			return userId.toString();
		} catch (InvalidCredentialsException ice) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ice.getMessage());
				return null;
			} catch (IOException ioe1) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe1) {
				return null;
			}
		} catch (IOException e) {
			try {
				// TODO: write propper response message
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Google login method
	 * 
		{
		    "google_id" : 123423423423
		}
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/google")
	@ResponseBody
	public Object googleLogin(HttpServletRequest req, HttpServletResponse resp) {
		String googleId = null;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			System.out.println(requestData);
			
			if (obj.get("google_id") == null) {
				throw new InvalidCredentialsException(Configuration.GOOGLE_LOGIN_ERROR);
			}
			
			
			googleId = obj.get("google_id").getAsString();
			
			System.out.println(googleId);
			
			Map<Token, User> data = UserDao.getInstance().getUserFromDbOrCache(null, null, null, googleId);
			
			if (data != null && data.isEmpty()) {
				throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
			}

			JsonObject userId = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    userId.addProperty("user_id", pair.getValue().getUserId());
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}
			
			return userId.toString();
		} catch (InvalidCredentialsException ice) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ice.getMessage());
				return null;
			} catch (IOException ioe1) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe1) {
				return null;
			}
		} catch (IOException e) {
			try {
				// TODO: write propper response message
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
}
 