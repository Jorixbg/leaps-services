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
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.token.Token;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

@RestController
public class RegisterController {
	
	/**
	 * User registration method
	 * 
		  {
		    "email_address" : "krika_original@mail.bg",
		    "first_name" : "Mityo" / null,
		    "last_name" : "Krika" / null,
		    "birthday" : "2264545662345632" / null,
		    "password" : "baizai",
			"fb_id" : 13452512645,
			"google_id" : 23423423423
		  }
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/register")
	@ResponseBody
	public Object registerUser(HttpServletRequest req, HttpServletResponse resp){
		String pass = null;
		String hashedPass = null;
		String email = null;
		String firstName = null;
		String lastName = null;
		Long birthday = null;
		String facebookId = null;
		String googleId = null;
		
		try (Scanner sc = new Scanner(req.getInputStream())) {
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();
			
			if (obj.get("email_address") == null || obj.get("first_name") == null ||
				obj.get("last_name") == null || obj.get("birthday") == null || obj.get("password") == null) {
				throw new InvalidParametersException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			pass = obj.get("password").getAsString();
			email = obj.get("email_address").getAsString();
			firstName = obj.get("first_name").getAsString();
			lastName = obj.get("last_name").getAsString();
			birthday = obj.get("birthday").getAsLong();
			
			if (obj.get("fb_id") != null) {
				facebookId = obj.get("fb_id").getAsString();
			}
			
			if (obj.get("google_id") != null) {
				googleId = obj.get("google_id").getAsString();
			}
			
			if (UserDao.getInstance().checkIfUserExistInDbOrCache(email)) {
				throw new UserException(Configuration.USER_ALREADY_EXIST);
			}
			
			hashedPass = LeapsUtils.convertToMd5(pass);
			
			Map<Token, User> data = UserDao.getInstance().registerNewUser(hashedPass, email, firstName, lastName, birthday, facebookId, googleId);
			
			if (data.isEmpty()) {
				throw new UserException(Configuration.ERROR_WHILE_CREATING_NEW_USER);
			}
			
			JsonObject userData = new JsonObject();
			
			Iterator<Map.Entry<Token, User>> it = data.entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry<Token, User> pair = it.next();
			    userData.addProperty(Configuration.USER_ID, pair.getValue().getUserId());
				resp.addHeader("Authorization", String.valueOf(pair.getKey().getId()));
			}
			
			return userData.toString();
		} catch (InvalidParametersException ip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ue.getMessage());
				return null;
			} catch (IOException ioe2) {
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
