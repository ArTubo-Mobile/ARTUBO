package org.tensorflow.lite.examples.classification.data_class;

public class UserAdminClass {
    String user_admin_id;
    String firebase_uid;

    String username;
    String email;

    String arduino_uid;

    public UserAdminClass(String user_admin_id, String firebase_uid, String username, String email, String arduino_uid) {
        this.user_admin_id = user_admin_id;
        this.firebase_uid = firebase_uid;
        this.username = username;
        this.email = email;
        this.arduino_uid = arduino_uid;
    }

    public String getUser_admin_id() {
        return user_admin_id;
    }

    public String getFirebase_uid() {
        return firebase_uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getArduino_uid() { return arduino_uid; }
}
