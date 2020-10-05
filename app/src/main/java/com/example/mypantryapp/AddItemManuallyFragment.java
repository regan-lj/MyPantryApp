package com.example.mypantryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AddItemManuallyFragment extends Fragment {


    private Spinner spinner;
    private DatabaseReference mDatabase;

    EditText enterIngredientsText;
    String updateIngredientsText;
    TextView viewDietaryWarning;

    private static final String TAG = "AddItemManually";
    private static final String KEY_NAME= "name";
    private static final String KEY_BRAND = "brand";
    private static final String KEY_BARCODE = "barcodeNum";
    private static final String KEY_SHELFLIFE = "shelfLife";
    private static final String KEY_VOLUME = "volume";
//    private  static final String KEY_CATEGORY = "categoryName";
//    private static final String KEY_DIETARY = "dietaryType";
//    private static final String KEY_ALLERGY = "allergens";

    private EditText editTextName;
    private EditText editTextBrand;
    private EditText editTextBarcode;
    private EditText editTextShelfLife;
    private EditText editTextQuantity;
//    private Spinner spinnerCategory;
//    private Spinner spinnerDietary;
//    private Spinner spinnerAllergy;

    private Button saveButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CheckIngredients checkIngredients = new CheckIngredients();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * Set any static listeners and data
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState the saved instance state
     * @return the view
     */
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_add_item_manually, container, false);

        // POSSIBLY NOT NEEDED. Show bottom navigation.
        BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation_drawer);
        navBar.setVisibility(View.VISIBLE);

        // When the camera icon for 'Ingredients' is selected, the user should be navigated to the scan ingredients fragment.
        // First need to set a listener to the whole view. The camera icon is on the far right of the view.
        final TextView ingredientsTitle = v.findViewById(R.id.ingredientsTitle);
        ingredientsTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int[] textLocation = new int[2];
                    ingredientsTitle.getLocationOnScreen(textLocation);
                    if (event.getRawX() >= textLocation[0] + ingredientsTitle.getWidth() - ingredientsTitle.getTotalPaddingRight()){

                        // Right drawable was tapped
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScanIngredientsFragment()).addToBackStack(null).commit();
                        return true;
                    }
                }
                return true;
            }
        });

        // When the camera icon for 'Barcode Number' is selected, the user should be navigated to the barcode fragment.
        // First need to set a listener to the whole view. The camera icon is on the far right of the view.
        final TextView barcodeTitle = v.findViewById(R.id.barcodeTitle);
        barcodeTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Check that the right drawable was tapped.
                    int[] textLocation = new int[2];
                    barcodeTitle.getLocationOnScreen(textLocation);
                    if (event.getRawX() >= textLocation[0] + barcodeTitle.getWidth() - barcodeTitle.getTotalPaddingRight()){
                        // If it was, replace fragment.
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScanBarcodeFragment()).addToBackStack(null).commit();
                        return true;
                    }
                }
                return true;
            }
        });

        final Button cancelBtn = v.findViewById(R.id.button_cancel_man);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddItemFragment()).addToBackStack(null).commit();
            }
        });


        return v;

    }

    /**
     * This method is called after the parent Activity's onCreate() method has completed.
     * Accessing the view hierarchy of the parent activity must be done in the onActivityCreated.
     * At this point, it is safe to search for activity View objects by their ID, for example.
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * When the user submits the product, the product should go into the product collection in Firestore.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup any handles to view objects here
        editTextName =  (EditText) view.findViewById(R.id.productNameInputMan);
        editTextBrand =  (EditText) view.findViewById(R.id.brandInputMan);
        editTextBarcode =  (EditText) view.findViewById(R.id.barcodeInputMan);
        editTextShelfLife = (EditText) view.findViewById(R.id.shelfLifeInputMan);
        editTextQuantity = (EditText) view.findViewById(R.id.QuantityInputMan);

        enterIngredientsText = (EditText) view.findViewById(R.id.enterIngredientsText);
        viewDietaryWarning = (TextView) view.findViewById(R.id.viewDietaryWarning);

//        spinnerCategory = (Spinner) findViewById(R.id.CategorySpinMan);
//        spinnerDietary = (Spinner) findViewById(R.id.DietSpinMan);
//        spinnerAllergy = (Spinner) findViewById(R.id.AllergenSpinMan);


        // Checks diets as the user types
        enterIngredientsText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkIngredients.setIngredients(enterIngredientsText.getText().toString());
                viewDietaryWarning.setText(checkIngredients.checkIngredients());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        Button save_manually = getActivity().findViewById(R.id.button_save_man);
        save_manually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String brand = editTextBrand.getText().toString();
                Integer barcode = Integer.parseInt(editTextBarcode.getText().toString());
                Integer shelfLife = Integer.parseInt(editTextShelfLife.getText().toString());
                String volume = editTextQuantity.getText().toString();
//              String category = spinnerCategory.getSelectedItem().toString();
//              String dietary =spinnerDietary.getSelectedItem().toString()
//              String allergy =spinnerAllergy.getSelectedItem().toString();


                Map<String, Object> item = new HashMap<>();
                item.put(KEY_NAME, name);
                item.put(KEY_BRAND, brand);
                item.put(KEY_BARCODE, barcode);
                item.put(KEY_SHELFLIFE, shelfLife);
                item.put(KEY_VOLUME, volume);
                //        item.put(KEY_CATEGORY, category);
                //        item.put(KEY_DIETARY, dietary);
                //        item.put(KEY_ALLERGY, allergy);

                db.collection("products").document().set(item)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Item Added", Toast.LENGTH_SHORT).show();
                                editTextName.setText("");
                                editTextBrand.setText("");
                                editTextBarcode.setText("");
                                editTextShelfLife.setText("");
                                editTextQuantity.setText("");
                                enterIngredientsText.setText("");

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, e.toString());
                            }
                        });
            }
        });

    }

    /**
     * Update the ingredients field in onResume to ensure it is displayed
     */
    @Override
    public void onResume() {
        super.onResume();

        if (updateIngredientsText != null) {
            enterIngredientsText.setText(updateIngredientsText);
            checkIngredients.setIngredients(updateIngredientsText);
            viewDietaryWarning.setText(checkIngredients.checkIngredients());
        }
    }

    /**
     * Here to get data from another fragment
     * @param message the result of camera in ScanIngredientsFragment
     */
    protected void displayReceivedData(String message) {
        updateIngredientsText = message;
    }
}