package com.captain.voyage

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

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
        // 앱 전체에서 공통적으로 필요한 초기화 로직(로그 설정, 분석 도구 등)을 여기에 배치합니다.
    }
}
