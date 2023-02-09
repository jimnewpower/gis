package com.primalimited.gis;

public class UserData {
    private Object userData;

    UserData(Object userData) {
        this.userData = userData;
    }

    int toInt() {
        if (userData == null)
            return -1;
        try {
            return Integer.parseInt(userData.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
