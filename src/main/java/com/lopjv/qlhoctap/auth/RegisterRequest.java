package com.lopjv.qlhoctap.auth;

public record RegisterRequest(String username, String email, String password, String fullName, String role) {

}
