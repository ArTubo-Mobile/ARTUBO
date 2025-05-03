package com.asucare.app.services;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.asucare.app.classes.User;

/**
 * Service class that handles all database operations related to users
 */
public class UserDataService {

    private final DatabaseReference userReference;

    public UserDataService() {
        userReference = FirebaseDatabase.getInstance().getReference("/user");
    }

    /**
     * Save a new user to the database
     * @param user The user object to save
     * @param callback Callback to handle success/failure
     */
    public void saveUser(User user, DatabaseCallback callback) {
        if (user == null || user.getFirebaseId() == null) {
            callback.onError("Invalid user data");
            return;
        }

        userReference.child(user.getFirebaseId())
                .setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    /**
     * Check if a username already exists
     * @param username Username to check
     * @param callback Callback with result
     */
    public void checkUsernameExists(String username, UsernameCheckCallback callback) {
        Query query = userReference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                callback.onResult(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Get a user by their Firebase UID
     * @param firebaseUid The UID to search for
     * @param callback Callback with the user data
     */
    public void getUserByFirebaseUid(String firebaseUid, UserCallback callback) {
        userReference.child(firebaseUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            callback.onUserLoaded(user);
                        } else {
                            callback.onUserNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // Callback interfaces
    public interface DatabaseCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface UsernameCheckCallback {
        void onResult(boolean exists);
        void onError(String errorMessage);
    }

    public interface UserCallback {
        void onUserLoaded(User user);
        void onUserNotFound();
        void onError(String errorMessage);
    }
}