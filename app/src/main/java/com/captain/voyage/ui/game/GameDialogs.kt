package com.captain.voyage.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.captain.voyage.data.repository.DailyBriefing

@Composable
fun DailyBriefingDialog(
    data: DailyBriefing,
    onConfirm: (Boolean) -> Unit, // hasConfessed 전달
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var hasConfessed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 0) {
                    ConscienceCheckStep(
                        onPass = {
                            hasConfessed = false
                            step = 1
                        },
                        onConfess = {
                            hasConfessed = true
                            step = 1
                        }
                    )
                } else {
                    ResultStep(
                        data = data,
                        hasConfessed = hasConfessed,
                        onConfirm = { onConfirm(hasConfessed) },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun ConscienceCheckStep(
    onPass: () -> Unit,
    onConfess: () -> Unit
) {
    Text(
        text = "⚓ 선장의 양심",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF3E2723)
    )
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "어제 당직 인계 후,\n몰래 스마트폰을 하거나\n딴짓을 하지는 않았습니까?",
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        color = Color(0xFF5D4037),
        lineHeight = 24.sp
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Button(
        onClick = onPass,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Green
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("✅ 맹세코 없습니다!", color = Color.White, fontWeight = FontWeight.Bold)
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    OutlinedButton(
        onClick = onConfess,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)), // Red
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("😔 사실 있었습니다...", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResultStep(
    data: DailyBriefing,
    hasConfessed: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // 고백했으면 무조건 실패 처리, 아니면 데이터 따름
    val finalSuccess = if (hasConfessed) false else data.isSuccess
    val finalDistance = if (hasConfessed) 50.0 else data.moveDistance // 실패 시 거리

    Text(
        text = "📜 항해 결과 보고",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF3E2723)
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Score Status (고백했어도 점수는 보여줌)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("목표 점수", fontSize = 12.sp, color = Color.Gray)
            Text("${data.targetScore} P", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text("→", fontSize = 24.sp, color = Color.LightGray)
        Column(horizontalAlignment = Alignment.End) {
            Text("획득 점수", fontSize = 12.sp, color = Color.Gray)
            Text(
                "${data.yesterdayScore} P", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold,
                color = if (data.isSuccess) Color(0xFF4CAF50) else Color(0xFFD32F2F)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Achievement Status
    val statusText = if (hasConfessed) "🚨 규율 위반!" else if (finalSuccess) "🎉 목표 달성 성공!" else "☁️ 목표 달성 실패"
    val statusColor = if (hasConfessed || !finalSuccess) Color(0xFFB71C1C) else Color(0xFF1B5E20)
    
    val description = when {
        hasConfessed -> "솔직함은 선장의 미덕입니다.\n하지만 규칙 위반으로 속도가 감소합니다."
        finalSuccess -> "선원들의 사기가 높습니다!\n순풍을 타고 멀리 나아갑니다."
        else -> "목표를 달성하지 못했습니다.\n보급품이 낭비되고 있습니다."
    }

    Text(
        text = statusText,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = statusColor
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = description,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        color = Color(0xFF5D4037)
    )

    Spacer(modifier = Modifier.height(24.dp))

    HorizontalDivider(color = Color(0xFFD7CCC8))

    Spacer(modifier = Modifier.height(16.dp))

    // Result Distance
    Text(
        text = "확정 전진 거리: ${finalDistance.toInt()} km",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF3E2723)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("돛을 올려라! (항해 시작)", color = Color.White, fontWeight = FontWeight.Bold)
    }
    
    TextButton(onClick = onDismiss) {
        Text("나중에 하기", color = Color.Gray)
    }
}