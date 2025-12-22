package com.captain.voyage.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.captain.voyage.R
import com.captain.voyage.VoyageApplication
import com.captain.voyage.databinding.ActivityMainBinding
import com.captain.voyage.ui.goals.GoalsFragment
import com.captain.voyage.ui.home.HomeFragment
import com.captain.voyage.ui.home.HomeViewModel
import com.captain.voyage.ui.home.HomeViewModelFactory
import com.captain.voyage.ui.rules.RulesFragment
import com.captain.voyage.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // ★ [추가] 프래그먼트와 신호를 공유하기 위한 뷰모델 선언
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as VoyageApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_rules -> replaceFragment(RulesFragment())
                R.id.nav_goals -> replaceFragment(GoalsFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
                else -> false
            }
            true
        }

        // ★ [추가] 앱이 처음 실행될 때 알림 데이터를 확인합니다.
        handleIntent(intent)
    }

    // ★ [추가] 앱이 백그라운드에 있다가 알림 클릭으로 다시 불려올 때 실행됩니다.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 시스템 인텐트를 최신 것으로 교체
        handleIntent(intent)
    }

    // ★ [추가] 알림 전용 로직
    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("OPEN_LOGBOOK", false) == true) {
            // 1. 하단 바 UI를 홈으로 맞춤
            binding.bottomNavigation.selectedItemId = R.id.nav_home

            // 2. 실제 화면을 홈 프래그먼트로 교체
            replaceFragment(HomeFragment())

            // 3. 뷰모델에 팝업을 띄우라는 신호를 보냄
            // (HomeFragment가 이 신호를 관찰하고 있다가 팝업을 띄우게 됩니다)
            homeViewModel.triggerLogbookPopup()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}