/*
 * Copyright 2025 Mark Assejev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.itu.moapd.copenhagenbuzz.maass

import android.app.Application
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Custom [Application] class for initializing global app resources.
 *
 * - Applies dynamic colors to activities.
 * - Initializes Firebase and loads environment variables from the assets.
 * - Sets up the Firebase Realtime Database with offline persistence.
 *
 * @property env Static map containing environment variables loaded from the `env` asset file.
 * @property database Static instance of [FirebaseDatabase] configured with the provided database URL.
 */
class MyApplication : Application() {

    companion object {
        /**
         * Map of environment variables loaded from the `env` asset file.
         */
        lateinit var env: Map<String, String>

        /**
         * Instance of [FirebaseDatabase] configured for the app.
         */
        lateinit var database: FirebaseDatabase
    }

    /**
     * Called when the application is starting, before any activity, service, or receiver objects have been created.
     * Initializes dynamic colors, Firebase, environment variables, and the database.
     */
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

    /**
     * Loads environment variables from the `env` file in the assets directory.
     *
     * @return A map containing key-value pairs from the env file.
     */
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