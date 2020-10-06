package com.example.mypantryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypantryapp.adapter.PantryAdapter;
import com.example.mypantryapp.domain.PantryItem;
import com.example.mypantryapp.domain.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class HomeFragment extends Fragment {
    @SuppressLint("ClickableViewAccessibility")
    @Nullable

    // Declarations
    private static final String TAG = "MainActivity";
    private static final String KEY_NAME = "name";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Field for collection of products in the firebase.
    private CollectionReference productRef = db.collection("products");
    public RecyclerView mRecyclerView;
    public PantryAdapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    String pantryRef;

    private CheckIngredients checkIngredients = new CheckIngredients();

    /**
     * Display all products in database.
     */
    public void onStart(){
        super.onStart();
        //get pantry information
        pantryRef = this.getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
                .getString("pantryRef", null);

        // These need to be initialised for RecyclerView and CardView
        mRecyclerView = getActivity().findViewById(R.id.recyclerPantryItems);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        // Store the items
        ArrayList<PantryItem> exampleList = new ArrayList<>();

        db.collection("pantries").document(pantryRef).collection("products").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                          @Override
                                          public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                              for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                  Long quant = (Long) documentSnapshot.get("quantity");
                                                  String quantity = Long.toString(quant);
                                                  String id = documentSnapshot.getId();
                                                  String expiry = (String) documentSnapshot.get("expiry");
                                                  db.collection("products").document(id).get()
                                                          .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                              @Override
                                                              public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                  if (task.isSuccessful()) {
                                                                      DocumentSnapshot document = task.getResult();
                                                                      if (document.exists()) {
                                                                          Product product = document.toObject(Product.class);

                                                                          String name = product.getName();
                                                                          String brand = product.getBrand();
                                                                          String volume = (String) document.get("volume");
                                                                          String ingredients = (String) document.get("ingredients");
                                                                          String dietTitle = "";
                                                                          String diet = "";

                                                                          if (!ingredients.equals("")) {
                                                                              checkIngredients.setIngredients(ingredients);
                                                                              diet = checkIngredients.checkIngredients();
                                                                              if (diet.equals("No dietary warnings")) {
                                                                                  // Then diet is compatible
                                                                                  dietTitle = "Compatible with your dietary preferences!";
                                                                              } else {
                                                                                  // Then diet is not compatible
                                                                                  dietTitle = "Incompatible with your dietary preferences:";
                                                                              }
                                                                          }

                                                                          exampleList.add(new PantryItem(name, brand, id, volume, quantity, ingredients, dietTitle, diet));
                                                                            String test = name + " " + brand + " " + id + " " + volume + " " + quantity;
                                                                            if (exampleList.size() == queryDocumentSnapshots.size()){
                                                                                mAdapter = new PantryAdapter(exampleList);
                                                                                mRecyclerView.setLayoutManager(mLayoutManager);
                                                                                mRecyclerView.setAdapter(mAdapter);
                                                                            }

                                                                      } else {
                                                                          Log.d(TAG, "No such document");
                                                                      }


                                                                  } else {
                                                                      Log.d(TAG, "get failed with ", task.getException());
                                                                  }
                                                              }
                                                              // These need to be set so that the products are displayed

                                                          });
                                              }

                                          }
                                      });




                    }


    /**
     * Add onclick listeners to static items
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState the saved instance state
     * @return the view
     */
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_home, container, false); // Initialise view

        // POSSIBLY NOT NEEDED. Ensure that bottom navigation is visible.
        @Nullable
        BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation_drawer);
        navBar.setVisibility(View.VISIBLE);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        String docRefPantry = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
                .getString("pantryRef", null);
        DocumentReference docRef = db.collection("pantries").document(docRefPantry);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        if (data.get("pantryName") != null) {
                            toolbar.setTitle((CharSequence) data.get("pantryName"));
                        }

                    }
                }
            }
        });

        EditText editText = v.findViewById(R.id.textSearchPantry);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.getFilter().filter(s);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the toolbar title to pantry name
//        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
//        toolbar.setTitle("My Pantry");
    }
}