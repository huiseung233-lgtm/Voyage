package com.captain.voyage

import android.app.Application
import com.captain.voyage.data.local.VoyageDatabase
import com.captain.voyage.data.repository.VoyageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class VoyageApplication : Application() {

    // 앱 전체에서 공유할 코루틴 스코프 (데이터베이스 작업용) - 기존 코드 유지
    val applicationScope = CoroutineScope(SupervisorJob())

    // 1. 데이터베이스 인스턴스 생성
    val database by lazy { VoyageDatabase.getDatabase(this) }

    // 2. 리포지토리 인스턴스 생성 (수정됨: DAO 3개를 모두 주입)
    val repository by lazy {
        VoyageRepository(
            database.voyageDao(),       // 기존: 규칙, 배, 유저 관리
            database.scoreRecordDao(),  // 신규: 개별 기록 관리
            database.dailyLogDao()      // 신규: 일별 점수판 관리
        )
    }
}