package com.captain.voyage.ui.trade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.ShipInventory

@Composable
fun TradeDialog(
    portName: String,
    gold: Long, // Changed to Long
    marketItems: List<MarketItemUi>, // UI용 데이터 (Item + Market 정보 결합)
    onBuy: (MarketItemUi, Int) -> Unit,
    onSell: (MarketItemUi, Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "🏪 $portName 교역소", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "보유 골드: $gold G", fontSize = 14.sp, color = Color(0xFF3E2723))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

                // Item List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(marketItems) { item ->
                        TradeItemRow(item, onBuy, onSell)
                    }
                }
            }
        }
    }
}

data class MarketItemUi(
    val item: Item,
    val market: Market,
    val myQuantity: Int
)

@Composable
fun TradeItemRow(
    itemUi: MarketItemUi,
    onBuy: (MarketItemUi, Int) -> Unit,
    onSell: (MarketItemUi, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = itemUi.item.name, fontWeight = FontWeight.Bold)
                Text(
                    text = "매입: ${itemUi.market.buyPrice}G | 매도: ${itemUi.market.sellPrice}G",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(text = "보유: ${itemUi.myQuantity}개", fontSize = 12.sp, color = Color(0xFF1B5E20))
            }

            // Actions
            Row {
                Button(
                    onClick = { onBuy(itemUi, 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.padding(end = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("구매", fontSize = 12.sp)
                }
                Button(
                    onClick = { onSell(itemUi, 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("판매", fontSize = 12.sp)
                }
            }
        }
    }
}
