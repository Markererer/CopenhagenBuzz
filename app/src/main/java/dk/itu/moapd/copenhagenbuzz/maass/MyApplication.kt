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
        lateinit var database: FirebaseDatabase
    }

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
        FirebaseApp.initializeApp(this)
        env = loadEnvFromAssets()

        val databaseUrl = env["DATABASE_URL"]
            ?: throw IllegalStateException("DATABASE_URL not found in env file")

        Log.d("MyApplication", "Loaded DATABASE_URL = $databaseUrl")

        database = FirebaseDatabase.getInstance(databaseUrl)
        database.setPersistenceEnabled(true)
    }

    private fun loadEnvFromAssets(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            assets.open("env").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    lines.forEach { line ->
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