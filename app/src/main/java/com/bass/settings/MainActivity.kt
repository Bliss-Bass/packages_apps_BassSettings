package com.bass.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import java.io.IOException

private const val TAG = "com.bass.settings.MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Pass an empty set to the AppBarConfiguration so that the Up button is always shown
        appBarConfiguration = AppBarConfiguration.Builder(setOf()).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_restart_systemui -> {
                // This is disabled in the menu XML for now
                true
            }
            R.id.action_restart_launcher -> {
                Log.d(TAG, "Restart Launcher menu item clicked.")
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolveInfo != null) {
                    val launcherPackage = resolveInfo.activityInfo.packageName
                    if (launcherPackage != null) {
                        Log.d(TAG, "Found launcher package: $launcherPackage")

                        // Command to stop the launcher
                        val stopCommand = "am force-stop $launcherPackage"
                        Log.d(TAG, "Executing stop command: `$stopCommand`")
                        try {
                            Runtime.getRuntime().exec(arrayOf("sh", "-c", stopCommand))
                            Log.d(TAG, "Successfully executed stop command.")
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to execute stop command", e)
                        }

                        // Command to start the launcher
                        val startCommand = "am start -a android.intent.action.MAIN -c android.intent.category.HOME"
                        Log.d(TAG, "Executing start command: `$startCommand`")
                        try {
                            Runtime.getRuntime().exec(arrayOf("sh", "-c", startCommand))
                            Log.d(TAG, "Successfully executed start command.")
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to execute start command", e)
                        }
                    } else {
                        Log.w(TAG, "Could not find launcher package name.")
                    }
                } else {
                    Log.w(TAG, "Could not resolve launcher activity.")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!navController.navigateUp(appBarConfiguration)) {
            finish()
        }
        return true
    }
}
