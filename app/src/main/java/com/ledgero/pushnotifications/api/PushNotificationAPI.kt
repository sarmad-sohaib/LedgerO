package com.ledgero.pushnotifications.api

import com.ledgero.pushnotifications.data.PushNotificationData
import com.ledgero.pushnotifications.utill.Constatns.CONTENT_TYPE
import com.ledgero.pushnotifications.utill.Constatns.FIREBASE_SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PushNotificationAPI {

    @Headers("Authorization: key=$FIREBASE_SERVER_KEY","Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body pushNotificationData: PushNotificationData
    ): Response<ResponseBody>
}