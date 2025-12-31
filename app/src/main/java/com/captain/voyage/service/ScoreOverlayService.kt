package com.captain.voyage.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.captain.voyage.data.repository.GoalRepository
import com.captain.voyage.data.repository.VoyageRepository
import com.captain.voyage.ui.home.LogbookContent
import com.captain.voyage.ui.popup.PopupViewModel
import com.captain.voyage.ui.theme.VoyageTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScoreOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    @Inject
    lateinit var voyageRepository: VoyageRepository

    @Inject
    lateinit var goalRepository: GoalRepository

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private var overlayView: View? = null

    private val serviceLifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _viewModelStore = ViewModelStore()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var viewModel: PopupViewModel

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        serviceLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        viewModel = PopupViewModel(voyageRepository, goalRepository)

        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        serviceLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or 
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ScoreOverlayService)
            setViewTreeSavedStateRegistryOwner(this@ScoreOverlayService)
            setViewTreeViewModelStoreOwner(this@ScoreOverlayService)
            
            setContent {
                VoyageTheme {
                    val rules by viewModel.rules.collectAsState()
                    val initialRecords by viewModel.initialRecords.collectAsState()
                    val scope = rememberCoroutineScope()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { stopSelf() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (initialRecords != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(700.dp)
                                    .clickable(enabled = false) {}
                            ) {
                                LogbookContent(
                                    date = viewModel.todayDate,
                                    initialRecords = initialRecords!!,
                                    rules = rules,
                                    onDismiss = { stopSelf() },
                                    onSave = { records ->
                                        scope.launch {
                                            // [New] 정박 상태 체크
                                            if (viewModel.isShipSailing()) {
                                                viewModel.saveBatchRecords(records) {
                                                    stopSelf()
                                                }
                                            } else {
                                                Toast.makeText(applicationContext, "⚓ 정박 중에는 일지를 쓸 수 없습니다. 먼저 출항해주세요!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.White, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF3E2723))
                            }
                        }
                    }
                }
            }
        }

        windowManager.addView(composeView, params)
        overlayView = composeView
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
        serviceScope.cancel()
        
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override val lifecycle: Lifecycle get() = serviceLifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore get() = _viewModelStore
}
