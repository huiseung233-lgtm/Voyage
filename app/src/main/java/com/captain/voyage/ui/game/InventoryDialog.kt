package com.captain.voyage.ui.game

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
import com.captain.voyage.data.model.ItemType

// 인벤토리 아이템 UI 모델
data class InventoryItemUi(
    val item: Item,
    val quantity: Int
)

@Composable
fun InventoryDialog(
    inventoryItems: List<InventoryItemUi>,
    onUseItem: (Long) -> Unit, // 아이템 사용 (보급 등)
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEBE9)) // 갈색 톤 배경
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎒 선박 인벤토리", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

                // Item List
                if (inventoryItems.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("인벤토리가 비어있습니다.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(inventoryItems) { itemUi ->
                            InventoryItemRow(itemUi, onUseItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(
    itemUi: InventoryItemUi,
    onUseItem: (Long) -> Unit
) {
    val isFood = itemUi.item.type == ItemType.FOOD
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if(isFood) Color(0xFFFFCC80) else Color(0xFFB0BEC5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if(isFood) "🍞" else "📦", 
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = itemUi.item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = itemUi.item.description, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                
                if (isFood) {
                    Text(text = "회복량: +${itemUi.item.effectValue}", fontSize = 11.sp, color = Color(0xFFEF6C00))
                }
            }

            // Quantity & Action
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "x ${itemUi.quantity}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isFood) {
                    Button(
                        onClick = { onUseItem(itemUi.item.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("보급", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}