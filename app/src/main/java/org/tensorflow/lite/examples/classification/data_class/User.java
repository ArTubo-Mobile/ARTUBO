package org.tensorflow.lite.examples.classification.data_class;

public class User {
    String username;
    String email;
    String arduino_uid;


    public User(String username, String email, String arduino_uid) {
        this.username = username;
        this.email = email;
        this.arduino_uid = arduino_uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getArduino_uid() { return arduino_uid; }
}
