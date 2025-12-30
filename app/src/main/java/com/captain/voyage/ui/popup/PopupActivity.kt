package com.captain.voyage.ui.popup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.unit.dp
import com.captain.voyage.ui.home.LogbookContent
import com.captain.voyage.ui.theme.VoyageTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PopupActivity : ComponentActivity() {

    private val viewModel: PopupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            VoyageTheme {
                val rules by viewModel.rules.collectAsState()
                val initialRecords by viewModel.initialRecords.collectAsState()
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { finish() },
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
                                onDismiss = { finish() },
                                onSave = { records ->
                                    scope.launch {
                                        // [New] 정박 상태 체크
                                        if (viewModel.isShipSailing()) {
                                            viewModel.saveBatchRecords(records) {
                                                finish()
                                            }
                                        } else {
                                            Toast.makeText(this@PopupActivity, "⚓ 정박 중에는 일지를 쓸 수 없습니다. 먼저 출항해주세요!", Toast.LENGTH_SHORT).show()
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
}