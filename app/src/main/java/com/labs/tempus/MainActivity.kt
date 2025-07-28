package com.labs.tempus

import android.os.Bundle
import android.view.Menu
import android.app.AlertDialog
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.material.navigation.NavigationView
import com.labs.tempus.data.EmployeeRepository
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.labs.tempus.databinding.ActivityMainBinding
import com.labs.tempus.ui.theme.TempusTheme
import androidx.core.os.bundleOf
import kotlinx.parcelize.IgnoredOnParcel

class MainActivity : AppCompatActivity() {

    @IgnoredOnParcel
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    @IgnoredOnParcel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge UI
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Apply custom dialog themes
        setupDialogThemes()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Set up FAB to add time entries
        binding.appBarMain.fab.setOnClickListener { view ->
            val repository = EmployeeRepository.getInstance(this)
            com.labs.tempus.util.AddTimeEntryHelper.showAddTimeEntryDialog(
                this,
                supportFragmentManager,
                repository
            )
        }
        
        // Update FAB icon and description to make it clear it adds time entries
        binding.appBarMain.fab.setImageResource(android.R.drawable.ic_menu_my_calendar)
        binding.appBarMain.fab.contentDescription = "Add Time Entry"
        
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_summary, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    
    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    /**
     * Refresh all data in the app
     */
    fun refreshData() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            ?.childFragmentManager?.fragments?.get(0)
            
        // Refresh data based on current fragment
        when (navController.currentDestination?.id) {
            R.id.nav_home -> {
                currentFragment?.let {
                    try {
                        val method = it.javaClass.getDeclaredMethod("refreshData")
                        method.isAccessible = true
                        method.invoke(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            R.id.nav_summary -> {
                currentFragment?.let {
                    try {
                        val method = it.javaClass.getDeclaredMethod("refreshData")
                        method.isAccessible = true
                        method.invoke(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    /**
     * Sets custom themes for dialogs to ensure dark mode compatibility
     */
    private fun setupDialogThemes() {
        // Material 3 handles theming automatically based on system settings
        // No manual theme configuration needed
    }
    
    /**
     * A helper function to create Compose content in traditional View-based activities
     */
    @Composable
    private fun ComposeContent(content: @Composable () -> Unit) {
        // Always use dynamic colors and follow system theme settings
        TempusTheme(
            darkTheme = isSystemInDarkTheme(),
            dynamicColor = true
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    }
    
    /**
     * Helper method to set content with Compose
     */
    private fun setupComposeContent(content: @Composable () -> Unit) {
        setContent {
            ComposeContent(content = content)
        }
    }
}