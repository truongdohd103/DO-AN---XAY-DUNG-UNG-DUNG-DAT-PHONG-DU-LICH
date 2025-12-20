package com.example.chillstay.core.feature

import androidx.annotation.DrawableRes
import com.example.chillstay.R
import java.util.concurrent.ConcurrentHashMap

object IconRegistry {
    private val map = ConcurrentHashMap<String, Int>()

    init {
        // Facilities
        register("Swimming Pool", R.drawable.ic_swimming_pool)
        register("Beachfront", R.drawable.ic_beach_front)
        register("Gym", R.drawable.ic_fitness)
        register("Nightclub", R.drawable.ic_nightclub)
        register("Restaurant", R.drawable.ic_restaurant)
        register("Spa", R.drawable.ic_spa)

        // Features
        register("City Center", R.drawable.ic_city_center)
        register("Nature", R.drawable.ic_nature)
        register("Near Airport", R.drawable.ic_near_airport)
        register("Shopping District", R.drawable.ic_shopping_district)
        register("Smoking Area", R.drawable.ic_smoking_area)
        register("Stylish Area", R.drawable.ic_stylish_area)
        register("Historic Area", R.drawable.ic_historic_area)
        register("Pet Allowed", R.drawable.ic_pet_allowed)
        register("24h Reception", R.drawable.ic_24h)

        // Languages
        register("Chinese", R.drawable.ic_chinese)
        register("English", R.drawable.ic_english)
        register("Hindi", R.drawable.ic_hindi)
        register("Indonesian", R.drawable.ic_indonesian)
        register("Japanese", R.drawable.ic_japanese)
        register("Malaysian", R.drawable.ic_malaysian)
        register("Spanish", R.drawable.ic_spanish)
        register("French", R.drawable.ic_french)
        register("Italian", R.drawable.ic_italian)
        register("Vietnamese", R.drawable.ic_vietnamese)
    }

    /** Register by resource id */
    fun register(key: String, @DrawableRes resId: Int) {
        map[key] = resId
    }

    public fun getIconResId(key: String): Int? {
        return map[key]
    }
}