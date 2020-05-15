package com.leaps.auth;

import com.leaps.model.utils.LeapsUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;

public class CustomPasswordEncoder implements PasswordEncoder {


    @Override
    public String encode(CharSequence rawPassword) {
        String hashed = null;
        try {
            hashed = LeapsUtils.convertToMd5(String.valueOf(rawPassword));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hashed;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String hashed = null;
        try {
            hashed = LeapsUtils.convertToMd5(String.valueOf(rawPassword));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedPassword.equals(hashed);
    }

}
