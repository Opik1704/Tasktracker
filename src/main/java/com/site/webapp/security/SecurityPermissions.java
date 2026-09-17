package com.site.webapp.security;

public final class SecurityPermissions {
    private SecurityPermissions() {}

    public static final String TASK_READ = "TASK_READ";
    public static final String TASK_WRITE = "TASK_WRITE";

    public static final String TASK_DELETE_OWN = "TASK_DELETE_OWN";
    public static final String TASK_DELETE = "TASK_DELETE_DEPT";
    public static final String TASK_DELETE_ADMIN = "TASK_DELETE_ADMIN";

    public static final String USER_BLOCK = "USER_BLOCK";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_CHANGE_ROLE = "USER_CHANGE_ROLE";
}
