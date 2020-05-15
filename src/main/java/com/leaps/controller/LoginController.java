package com.leaps.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leaps.model.db.DBUserDao;
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

	@GetMapping
	public void checkLogin() {
		System.out.println("IN");
		// TODO
	}
	
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
		String firebaseToken = null;
		long userId = -1;
		Token token = null;
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		
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
			
			// update the token only if it is available
			if (obj.get("firebase_token") != null) {
				firebaseToken = obj.get("firebase_token").getAsString();
				map.put("firebase_token", new HashMap<String, Object>());
				map.get("firebase_token").put("string", firebaseToken);
			}
			
			userData = obj.get("name").getAsString();
			pass = obj.get("password").getAsString();
			hashedPass = LeapsUtils.convertToMd5(pass);
			Map<Token, User> data = UserDao.getInstance().getUserFromDbOrCache(userData, hashedPass, null, null);
			
			if (data != null && data.isEmpty()) {
				throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
			}
			
			JsonObject jsonUserId = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    token = pair.getKey();
			    userId = pair.getValue().getUserId();
			    jsonUserId.addProperty("user_id", userId);
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}
			
			if (!map.isEmpty()) {
				DBUserDao.getInstance().updateUser(map, userId);
				User user = DBUserDao.getInstance().getUserFromDbById(userId);
				UserDao.getInstance().updateUserInCache(user, token.getId());
			}
			
			return jsonUserId.toString();
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
		long userId = -1;
		String firebaseToken = null;
		Token token = null;
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		
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

			// update the token only if it is available
			if (obj.get("firebase_token") != null) {
				firebaseToken = obj.get("firebase_token").getAsString();
				map.put("firebase_token", new HashMap<String, Object>());
				map.get("firebase_token").put("string", firebaseToken);
			}
			
			fbId = obj.get("fb_id").getAsString();
			
			Map<Token, User> data = UserDao.getInstance().getUserFromDbOrCache(null, null, fbId, null);
			
			if (data != null && data.isEmpty()) {
				throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
			}

			JsonObject jsonUserId = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    token = pair.getKey();
			    userId = pair.getValue().getUserId();
			    jsonUserId.addProperty("user_id", userId);
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}

			if (!map.isEmpty()) {
				DBUserDao.getInstance().updateUser(map, userId);
				User user = DBUserDao.getInstance().getUserFromDbById(userId);
				UserDao.getInstance().updateUserInCache(user, token.getId());
			}
			
			return jsonUserId.toString();
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
		long userId = -1;
		String firebaseToken = null;
		Token token = null;
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		
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

			// update the token only if it is available
			if (obj.get("firebase_token") != null) {
				firebaseToken = obj.get("firebase_token").getAsString();
				map.put("firebase_token", new HashMap<String, Object>());
				map.get("firebase_token").put("string", firebaseToken);
			}
			
			googleId = obj.get("google_id").getAsString();
			
			System.out.println(googleId);
			
			Map<Token, User> data = UserDao.getInstance().getUserFromDbOrCache(null, null, null, googleId);
			
			if (data != null && data.isEmpty()) {
				throw new UserException(Configuration.WRONG_USERNAME_PASSWORD_MESSAGE);
			}

			JsonObject jsonUserId = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    token = pair.getKey();
			    userId = pair.getValue().getUserId();
			    jsonUserId.addProperty("user_id", userId);
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}

			if (!map.isEmpty()) {
				DBUserDao.getInstance().updateUser(map, userId);
				User user = DBUserDao.getInstance().getUserFromDbById(userId);
				UserDao.getInstance().updateUserInCache(user, token.getId());
			}
			
			return jsonUserId.toString();
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
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
}
 