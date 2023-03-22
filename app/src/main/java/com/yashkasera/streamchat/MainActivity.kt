package com.yashkasera.streamchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yashkasera.streamchat.data.model.LoggedInUser
import com.yashkasera.streamchat.databinding.ActivityMainBinding
import com.yashkasera.streamchat.databinding.DrawerLayoutBinding
import com.yashkasera.streamchat.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment)
        initToolbar()
        binding.navView.setupWithNavController(navController)
        binding.navLayout.setupNavView()
        checkPermissionsAndRequest()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        appBarConfiguration =
            AppBarConfiguration.Builder(R.id.homeFragment)
                .setOpenableLayout(binding.drawerLayout)
                .build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.isVisible = destination.id != R.id.chatFragment
        }
    }

    private fun checkPermissionsAndRequest() {
        if (PermissionUtils.isAudioAndStoragePermissionEnabled(this).not()) {
            PermissionUtils.requestAudioAndStoragePermissions(this, object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {}

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
        }
    }

    private fun DrawerLayoutBinding.setupNavView() {
        actionLogout.setOnClickListener {
            binding.drawerLayout.closeDrawers()
            lifecycleScope.launch(Dispatchers.Main) {
                showProgress()
                AppObjectController.logout()
                showProgress(false)
            }
        }
        actionStarred.setOnClickListener {
            binding.drawerLayout.closeDrawers()
            navController.navigate(R.id.starredFragment)
        }
        actionMentions.setOnClickListener {
            binding.drawerLayout.closeDrawers()
            navController.navigate(R.id.mentionsFragment)
        }
        LoggedInUser.getOrNull()?.let {
            name.text = it.displayName
            username.text = it.userId
            avatar.setImageResource(R.drawable.ic_profile)
            avatar.setContentPadding(8, 8, 8, 8)
            avatar.strokeWidth = 4f
            avatar.strokeColor = getColorStateList(R.color.black)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp()
    }

    fun showProgress(shouldShow: Boolean = true) {
        binding.progressBar.isVisible = shouldShow
    }
}