package com.info301.mypantryapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CheckIngredients {

    private String ingredients;
    private Map<String, ArrayList<String>> diets = new HashMap<>();
    private Map<String, String> documentNames = new HashMap<>();

    /**
     * Stores the user selected diet information
     */
    public CheckIngredients () {
        // Stores all diets and blacklisted ingredients from firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid(); //get unique user id
        db.collection("dietary")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String result = "";
                        ArrayList<String> dietNames = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                 String name = document.getString("name");
                                 String id = document.getId();
                                 ArrayList<String> blacklist = (ArrayList<String>) document.get("blacklist");

                                db.collection("users")
                                        .whereEqualTo("userAuthId", userId) //looks for the corresponding value with the field
                                        // in the database
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {

                                                    for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                                        boolean checkIfUserDiet = (boolean) document.get(id);
                                                        if (checkIfUserDiet) {
                                                            diets.put(name, blacklist);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    /**
     * Checks a products ingredients against the users diets.
     *
     * @param ingredients a String of ingredients for a product. Separated by white spaces and possibly
     * with commas.
     * @return dietWarnings a String containing the names of any user diets being breached, and
     * the corresponding ingredients that aren't compatible. If no infringements returns "No dietary warnings"
     */
    public String checkIngredients(String ingredients) {
        String[] ingrs = ingredients.split(" ");
        ArrayList<String> dietNames = new ArrayList<>();
        StringBuilder dietWarnings = new StringBuilder();
        for (Map.Entry<String, ArrayList<String>> entry : diets.entrySet()) {
            String name = entry.getKey();
            Log.i("DIETS", name);
            ArrayList<String> blacklist = entry.getValue();
            for (String ingr : ingrs) {
                ingr = ingr.toLowerCase();
                // removes trailing ,
                if (ingr.length() > 0 && (ingr.charAt(ingr.length()-1) == ',' || ingr.charAt(ingr.length()-1) == '.')) {
                    ingr = ingr.substring(0, ingr.length()-1);
                }
                if (blacklist.contains(ingr)) {
                    if (!dietNames.contains(name)) {
                        if (dietWarnings.length() != 0) {
                            // removes unwanted ", " doesn't work for last line
                            if (dietWarnings.charAt(dietWarnings.length()-2) == ',') {
                                dietWarnings = new StringBuilder(dietWarnings.substring(0, dietWarnings.length() - 2));
                            }
                            dietWarnings.append("\n");
                        }
                        dietWarnings.append(name).append(": ");
                        dietNames.add(name);
                    }
                    dietWarnings.append(ingr).append(", ");
                }
            }
            if (dietWarnings.length() != 0 && dietWarnings.charAt(dietWarnings.length()-2) == ',') {
                // removes final trailing ", "
                dietWarnings = new StringBuilder(dietWarnings.substring(0, dietWarnings.length() - 2));
            }
        }
        if (dietWarnings.toString().equals("")) {
            dietWarnings = new StringBuilder("No dietary warnings");
        }
        return dietWarnings.toString();
    }
}
