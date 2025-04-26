package ar_tubo.classes;

public class User {
    String username;
    String email;
    String arduino_uid;
    String firebase_id;

    public User(String firebase_id, String username, String email, String arduino_uid) {
        this.username = username;
        this.email = email;
        this.arduino_uid = arduino_uid;
        this.firebase_id = firebase_id;
    }

    public String getFirebaseId() {return firebase_id;}
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
    public String getArduino_uid() { return arduino_uid; }
}