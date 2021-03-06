package com.info301.mypantryapp.domain;

/**
 * Stores details about a product so it can be displayed dynamically in AddItemFragment.
 */
public class ProductItem {
    private String mName;
    private String mBrand;
    private String mId;
    private String mVolume;
    private String mIngredients;
    private Long mShelfLife;

    /**
     * Constructor to set the details
     * @param name the product name
     * @param brand the product brand
     * @param id the product id
     */
    public ProductItem(String name, String brand, String id, String volume, String ingredients, Long shelfLife) {
        mName = name;
        mBrand = brand;
        mId = id;
        mVolume = volume;
        mIngredients = ingredients;
        mShelfLife = shelfLife;
    }

    /**
     * Get the product shelf life
     * @return diet warnings
     */
    public Long getShelfLife() {
        return mShelfLife;
    }

    /**
     * Get the product ingredients
     * @return ingredients
     */
    public String getDietWarnings() {
        return mIngredients;
    }

    /**
     * Get the product name
     * @return name
     */
    public String getName() {
        return mName;
    }

    /**
     * Get the product brand
     * @return brand
     */
    public String getBrand() {
        return mBrand;
    }

    /**
     * Get the product id
     * @return id
     */
    public String getId() {
        return mId;
    }

    /**
     * Get the product volume
     * @return volume
     */
    public String getVolume() {
        return mVolume;
    }


}
