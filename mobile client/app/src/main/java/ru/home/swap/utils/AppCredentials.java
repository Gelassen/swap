package ru.home.swap.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;

import okio.ByteString;

public class AppCredentials {

    private AppCredentials() {}

    /** Returns an auth credential for the Basic scheme. */
    public static String basic(String username, String password) {
        return basic(username, password, UTF_8);
    }

    public static String basic(String username, String password, Charset charset) {
        String usernameAndPassword = username + ":" + password;
        String encoded = ByteString.encodeString(usernameAndPassword, charset).base64();
        return "Basic " + encoded;
    }
}
