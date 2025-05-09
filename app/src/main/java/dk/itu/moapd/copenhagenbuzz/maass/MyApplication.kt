package dk.itu.moapd.copenhagenbuzz.maass

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic colors to all Activities in the app
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
