package dk.itu.moapd.copenhagenbuzz.maass

import android.app.Application
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader
import java.io.InputStreamReader

class MyApplication : Application() {

    companion object {
        lateinit var env: Map<String, String>
    }

    override fun onCreate() {
        super.onCreate()

        // 1) Apply dynamic colors
        DynamicColors.applyToActivitiesIfAvailable(this)

        // 2) Initialize Firebase core (reads google-services.json)
        FirebaseApp.initializeApp(this)

        // 3) Load and parse assets/env into a Map
        env = loadEnvFromAssets()

        // 4) Fetch your DATABASE_URL
        val databaseUrl = env["DATABASE_URL"]
            ?: throw IllegalStateException("DATABASE_URL not found in env file")

        Log.d("MyApplication", "Loaded DATABASE_URL = $databaseUrl")

        // 5) Initialize Realtime Database with that URL
        val database = FirebaseDatabase.getInstance(databaseUrl)
        database.setPersistenceEnabled(true)
    }

    private fun loadEnvFromAssets(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            assets.open("env").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    lines.forEach { line ->
                        // skip empty or comment lines
                        val trimmed = line.trim()
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                        val parts = trimmed.split("=", limit = 2)
                        if (parts.size == 2) {
                            map[parts[0].trim()] = parts[1].trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Error loading env from assets", e)
        }
        return map
    }
}
