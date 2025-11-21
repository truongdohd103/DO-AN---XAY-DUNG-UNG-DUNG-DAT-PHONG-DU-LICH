package com.example.chillstay.core.feature

import androidx.annotation.DrawableRes
import com.example.chillstay.R
import java.util.concurrent.ConcurrentHashMap

object IconRegistry {
    private val map = ConcurrentHashMap<String, Int>()

    init {
        // Facilities
        register("swimmingPool", R.drawable.ic_swimming_pool)
        register("beachFront", R.drawable.ic_beach_front)
        register("fitness", R.drawable.ic_fitness)
        register("nightClub", R.drawable.ic_nightclub)
        register("restaurant", R.drawable.ic_restaurant)
        register("spa", R.drawable.ic_spa)

        // Features
        register("cityCenter", R.drawable.ic_city_center)
        register("Nature", R.drawable.ic_nature)
        register("nearAirport", R.drawable.ic_near_airport)
        register("shoppingDistrict", R.drawable.ic_shopping_district)
        register("smokingArea", R.drawable.ic_smoking_area)
        register("stylishArea", R.drawable.ic_stylish_area)
        register("historicArea", R.drawable.ic_historic_area)
        register("petAllowed", R.drawable.ic_pet_allowed)
        register("24hReception", R.drawable.ic_24h)

        // Languages
        register("chinese", R.drawable.ic_chinese)
        register("english", R.drawable.ic_english)
        register("hindi", R.drawable.ic_hindi)
        register("indonesian", R.drawable.ic_indonesian)
        register("japanese", R.drawable.ic_japanese)
        register("malaysian", R.drawable.ic_malaysian)
        register("spanish", R.drawable.ic_spanish)
        register("french", R.drawable.ic_french)
        register("italian", R.drawable.ic_italian)
        register("vietnamese", R.drawable.ic_vietnamese)
    }

    /** Register by resource id */
    fun register(key: String, @DrawableRes resId: Int) {
        map[key] = resId
    }

    public fun getIconResId(key: String): Int? {
        return map[key]
    }
}