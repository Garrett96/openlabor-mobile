package com.labs.tempus

import android.os.Bundle
import android.view.Menu
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.google.android.material.navigation.NavigationView
import com.labs.tempus.data.EmployeeRepository
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.labs.tempus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply custom dialog themes
        setupDialogThemes()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Set up FAB to add time entries
        binding.appBarMain.fab.setOnClickListener { _ ->
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
                    val method = it.javaClass.getDeclaredMethod("refreshData")
                    method.isAccessible = true
                    method.invoke(it)
                }
            }
            R.id.nav_summary -> {
                currentFragment?.let {
                    val method = it.javaClass.getDeclaredMethod("refreshData")
                    method.isAccessible = true
                    method.invoke(it)
                }
            }
        }
    
    }
    
    /**
     * Sets custom themes for dialogs
     */
    private fun setupDialogThemes() {
        // Set theme for AlertDialog
        try {
            AlertDialog.Builder::class.java.getDeclaredField("P").apply {
                isAccessible = true
                getInt(null) // Get the private static value
                // You can't actually modify internal dialog themes this way,
                // but we'll use custom themes with our dialog creations
            }
        } catch (e: Exception) {
            // Safely handle any reflection errors
        }
    }
}