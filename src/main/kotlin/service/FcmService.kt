package com.home.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.home.model.MesureMessage
import java.io.FileInputStream

class FcmService(serviceAccountPath: String = "firebase-service-account.json") {
    
    init {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = FileInputStream(serviceAccountPath)
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
        }
    }

    fun sendToTopic(topic: String, mesure: MesureMessage): String {
        val notification = Notification.builder()
            .setTitle("Alerte $topic")
            .setBody("Nouvelle mesure: ${mesure.valeur} (${mesure.typeMesureCode})")
            .build()

        val message = Message.builder()
            .setTopic(topic)
            .setNotification(notification)
            .putData("valeur", mesure.valeur.toString())
            .putData("typeMesureCode", mesure.typeMesureCode.toString())
            .putData("mesureAt", mesure.mesureAt.toString())
            .putData("source", topic)
            .build()

        return FirebaseMessaging.getInstance().send(message)
    }
}
