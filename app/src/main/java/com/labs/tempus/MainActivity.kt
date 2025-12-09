package com.labs.openlabor

import android.os.Bundle
import android.view.Menu
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.google.android.material.navigation.NavigationView
import com.labs.openlabor.data.EmployeeRepository
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.labs.openlabor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDialogThemes()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { _ ->
            val repository = EmployeeRepository.getInstance(this)
            com.labs.openlabor.util.AddTimeEntryHelper.showAddTimeEntryDialog(
                this,
                supportFragmentManager,
                repository
            )
        }

        binding.appBarMain.fab.setImageResource(android.R.drawable.ic_menu_my_calendar)
        binding.appBarMain.fab.contentDescription = "Add Time Entry"

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_summary, R.id.nav_settings, R.id.nav_qrcode
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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


    fun refreshData() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            ?.childFragmentManager?.fragments?.get(0)

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

    private fun setupDialogThemes() {
        try {
            AlertDialog.Builder::class.java.getDeclaredField("P").apply {
                isAccessible = true
                getInt(null)

            }
        } catch (e: Exception) {

        }
    }
}
