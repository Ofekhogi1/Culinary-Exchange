package com.college.culinaryexchange

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.college.culinaryexchange.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val authFragments = setOf(R.id.loginFragment, R.id.registerFragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        binding.fabAdd.setOnClickListener {
            navController.navigate(R.id.addPostFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuth = destination.id in authFragments
            binding.bottomNav.visibility = if (isAuth) View.GONE else View.VISIBLE
            binding.fabAdd.visibility = if (isAuth) View.GONE else View.VISIBLE
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            navController.navigate(R.id.homeFragment)
        }
    }
}
