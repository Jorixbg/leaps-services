package com.leaps.model.token;

import java.util.Random;
import com.leaps.model.utils.Configuration;

public class TokenManager {
	private static TokenManager instance = null;
	private Random randomizer = null;
	
	private TokenManager() {
		randomizer = new Random();
	}
	
	public static TokenManager getInstance() {
		if(instance == null) {
			instance = new TokenManager();
		}
		return instance;
	}
		
	public Token generateToken() {
		Token token = null;
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < Configuration.TOKEN_SIZE; i++) {
			sb.append(randomizer.nextInt(10));
		}
		
		token = new Token(Long.valueOf(sb.toString()));
		return token;
	}
	
	public void updateTokenTime(Token token) {
		token.setLastModified(System.currentTimeMillis());
	}
}
