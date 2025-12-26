package com.captain.voyage

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.captain.voyage.BuildConfig

/**
 * VoyageApplication: 앱의 전역 상태를 관리하는 커스텀 Application 클래스입니다.
 * 
 * [핵심 역할]
 * 1. @HiltAndroidApp: Hilt 의존성 주입(DI)의 시작점입니다. 
 *    이 어노테이션이 있어야 앱 전체에서 @Inject를 통해 필요한 도구(DB, Repository 등)를 자동으로 가져올 수 있습니다.
 * 2. Application() 상속: 앱이 켜질 때 시스템에 의해 단 한 번 생성되며, 앱이 종료될 때까지 유지됩니다.
 */
@HiltAndroidApp
class VoyageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // [항해 기록 장치 설치]
        // 개발 중(Debug)일 때만 로그를 기록하도록 설정합니다.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("⚓ Voyage 항해 기록 장치가 가동되었습니다! 로그캣에서 저를 찾아보세요.")
        }
    }
}