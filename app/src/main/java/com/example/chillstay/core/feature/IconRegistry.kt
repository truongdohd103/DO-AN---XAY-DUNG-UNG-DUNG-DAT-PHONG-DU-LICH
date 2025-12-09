package com.example.chillstay.core.feature

import androidx.annotation.DrawableRes
import com.example.chillstay.R
import java.util.concurrent.ConcurrentHashMap

object IconRegistry {
    private val map = ConcurrentHashMap<String, Int>()

    init {
        // Facilities
        register("swimming pool", R.drawable.ic_swimming_pool)
        register("beach front", R.drawable.ic_beach_front)
        register("fitness", R.drawable.ic_fitness)
        register("nightclub", R.drawable.ic_nightclub)
        register("restaurant", R.drawable.ic_restaurant)
        register("spa", R.drawable.ic_spa)

        // Features
        register("city center", R.drawable.ic_city_center)
        register("nature", R.drawable.ic_nature)
        register("near airport", R.drawable.ic_near_airport)
        register("shopping district", R.drawable.ic_shopping_district)
        register("smoking area", R.drawable.ic_smoking_area)
        register("stylish area", R.drawable.ic_stylish_area)
        register("historic area", R.drawable.ic_historic_area)
        register("pet allowed", R.drawable.ic_pet_allowed)
        register("24h reception", R.drawable.ic_24h)

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