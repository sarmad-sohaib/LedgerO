package com.ledgero.pushnotifications.di

import com.ledgero.pushnotifications.api.PushNotificationAPI
import com.ledgero.pushnotifications.utill.Constatns
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitPushNotification {
    fun getAPi(): PushNotificationAPI = providesPushNotificationAPI(providesRetrofitBuilder())


    private fun providesRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder().baseUrl(Constatns.FIREBASE_MESSAGE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

    }


    private fun providesPushNotificationAPI(builder: Retrofit.Builder): PushNotificationAPI {
        return builder
            .build()
            .create(PushNotificationAPI::class.java)


    }
}