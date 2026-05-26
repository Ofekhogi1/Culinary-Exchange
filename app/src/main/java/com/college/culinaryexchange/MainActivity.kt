package com.college.culinaryexchange

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.college.culinaryexchange.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Push all fragment content below the status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBar.top)
            insets
        }

        // Keep bottom bar content above the gesture/button navigation area
        val origBottomPad = binding.bottomBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = origBottomPad + navBar.bottom)
            insets
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        binding.btnNavHome.setOnClickListener { navigateTo(R.id.homeFragment) }
        binding.btnNavMyPosts.setOnClickListener { navigateTo(R.id.myPostsFragment) }
        binding.btnNavAdd.setOnClickListener { navController.navigate(R.id.addPostFragment) }
        binding.btnNavProfile.setOnClickListener { navigateTo(R.id.profileFragment) }

        val noBottomBarFragments = setOf(
            R.id.loginFragment, R.id.registerFragment,
            R.id.addPostFragment, R.id.recipeDetailFragment, R.id.editProfileFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomBar.visibility =
                if (destination.id in noBottomBarFragments) View.GONE else View.VISIBLE
            updateActiveTab(destination.id)
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            navController.navigate(R.id.homeFragment)
        }
    }

    private fun navigateTo(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return
        navController.navigate(
            destinationId, null, NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.homeFragment, false)
                .build()
        )
    }

    private fun updateActiveTab(destinationId: Int) {
        val active = ContextCompat.getColor(this, R.color.primary)
        val inactive = ContextCompat.getColor(this, R.color.text_secondary)

        val homeActive = destinationId == R.id.homeFragment
        binding.ivNavHome.setColorFilter(if (homeActive) active else inactive)
        binding.tvNavHome.setTextColor(if (homeActive) active else inactive)

        val myPostsActive = destinationId == R.id.myPostsFragment
        binding.ivNavMyPosts.setColorFilter(if (myPostsActive) active else inactive)
        binding.tvNavMyPosts.setTextColor(if (myPostsActive) active else inactive)

        val profileActive = destinationId == R.id.profileFragment
        binding.ivNavProfile.setColorFilter(if (profileActive) active else inactive)
        binding.tvNavProfile.setTextColor(if (profileActive) active else inactive)
    }
}
