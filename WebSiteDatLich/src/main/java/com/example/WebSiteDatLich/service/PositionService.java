package com.example.WebSiteDatLich.service;
import com.example.WebSiteDatLich.model.Position;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class PositionService {

    private final DatabaseReference positionReference;

    public PositionService() {
        // Initialize Firebase Database reference to "positions" node
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        this.positionReference = firebaseDatabase.getReference("positions");
    }

    // Method to retrieve all positions from Firebase Realtime Database
    public List<Position> getAllPositions() throws InterruptedException {
        List<Position> positionList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);  // Used to wait for async data retrieval

        positionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot positionSnapshot : snapshot.getChildren()) {
                        Position position = positionSnapshot.getValue(Position.class);
                        if (position != null) {
                            positionList.add(position);
                        }
                    }
                }
                latch.countDown();  // Release the latch when done
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();  // Release the latch even on error
            }
        });

        latch.await();  // Wait until the data has been loaded
        return positionList;
    }

    // Method to save a new position to Firebase Realtime Database
    public void savePosition(Position position) {
        // Generate a new ID for the position and push it to the "positions" node
        String positionId = positionReference.push().getKey();
        if (positionId != null) {
            position.setPosition_id(Integer.parseInt(positionId));  // Convert ID to Integer if needed
            positionReference.child(positionId).setValueAsync(position);  // Save position data
        }
    }
}

