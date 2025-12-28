package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.CustomerActivity

interface ActivityRepository {
    suspend fun getCustomerActivities(userId: String, type: String? = null): List<CustomerActivity>
}