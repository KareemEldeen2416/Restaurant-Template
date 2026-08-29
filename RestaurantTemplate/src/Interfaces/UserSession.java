package Interfaces;

/**
 * Global User Session manager holding authenticated user information.
 * Allows all windows and views across the application to display the current logged-in user and role.
 * 
 * @author KareemEldeen
 */
public class UserSession {

    private static String username = "admin";
    private static String fullName = "المدير العام";
    private static String userRole = "مدير النظام";
    private static String accessRights = "11111111";

    /**
     * Updates the active user session upon successful login.
     * 
     * @param uName User's login username
     * @param fName User's full name (or null)
     * @param role User's job title / role (or null)
     * @param rights User's 8-bit permission string (or null)
     */
    public static void setSession(String uName, String fName, String role, String rights) {
        username = uName != null && !uName.trim().isEmpty() ? uName.trim() : "admin";
        fullName = fName != null && !fName.trim().isEmpty() ? fName.trim() : username;
        userRole = role != null && !role.trim().isEmpty() ? role.trim() : "موظف";
        accessRights = rights != null && rights.length() >= 8 ? rights : "11111111";
    }

    public static String getUsername() {
        return username;
    }

    public static String getFullName() {
        return fullName;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static String getAccessRights() {
        return accessRights;
    }

    /**
     * Returns the name to display on the top header bar (full name if available, otherwise username).
     */
    public static String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty() && !fullName.equals("المستخدم")) {
            return fullName;
        }
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        return "المستخدم";
    }

    /**
     * Resets the session (e.g. on logout).
     */
    public static void cleanSession() {
        username = "admin";
        fullName = "المدير العام";
        userRole = "مدير النظام";
        accessRights = "11111111";
    }
}
