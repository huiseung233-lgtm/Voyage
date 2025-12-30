package com.captain.voyage.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.captain.voyage.R
import com.captain.voyage.data.model.Rule
import com.captain.voyage.ui.theme.VoyageTheme
import com.captain.voyage.ui.theme.VoyageBackgroundParchment
import com.captain.voyage.ui.theme.voyageTextFieldColors

@Composable
fun RulesScreen(
    viewModel: RulesViewModel
) {
    val rules by viewModel.rules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<Rule?>(null) }
    var ruleToDelete by remember { mutableStateOf<Rule?>(null) }

    VoyageTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF8D6E63),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Rule")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFD7CCC8)) // Background color from XML
            ) {
                // Header
                Text(
                    text = "📜 선박 규율 (Ship's Rules)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF5D4037))
                        .padding(16.dp),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Search Bar
                var searchQuery by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.setSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color(0xFFF5F5DC), RoundedCornerShape(4.dp)),
                    placeholder = { Text("규칙을 찾아보게...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                // Rules List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rules, key = { it.id }) { rule ->
                        RuleItem(
                            rule = rule,
                            onClick = {
                                editingRule = rule
                                showAddDialog = true
                            },
                            onDeleteClick = { ruleToDelete = rule }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom padding for FAB
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog) {
            RuleEditorDialog(
                rule = editingRule,
                onDismiss = {
                    showAddDialog = false
                    editingRule = null
                },
                onConfirm = { title, desc, reward, penalty ->
                    if (editingRule == null) {
                        viewModel.addRule(title, desc, reward, penalty)
                    } else {
                        viewModel.updateRule(
                            editingRule!!.copy(
                                title = title,
                                description = desc,
                                defaultScore = reward,
                                penalty = penalty
                            )
                        )
                    }
                    showAddDialog = false
                    editingRule = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (ruleToDelete != null) {
            AlertDialog(
                onDismissRequest = { ruleToDelete = null },
                title = { Text("규율 폐기") },
                text = { Text("'${ruleToDelete?.title}' 규율을 정말 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            ruleToDelete?.let { viewModel.deleteRule(it) }
                            ruleToDelete = null
                        }
                    ) {
                        Text("폐기", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ruleToDelete = null }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
fun RuleItem(
    rule: Rule,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag Handle (Visual only for now)
            Icon(
                painter = painterResource(id = R.drawable.ic_drag_handle), // Assuming this drawable exists
                contentDescription = null,
                tint = Color(0xFF8D6E63),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    color = Color(0xFF3E2723),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (rule.description.isNotBlank()) {
                    Text(
                        text = rule.description,
                        color = Color(0xFF4E342E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "+${rule.defaultScore} P | ${rule.penalty} P",
                    color = Color(0xFF5D4037),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete, // Or R.drawable.ic_delete
                    contentDescription = "Delete",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun RuleEditorDialog(
    rule: Rule?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf(rule?.title ?: "") }
    var description by remember { mutableStateOf(rule?.description ?: "") }
    var reward by remember { mutableStateOf(rule?.defaultScore ?: 10) }
    var penalty by remember { mutableStateOf(rule?.penalty ?: -10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoyageBackgroundParchment,
        title = { Text(if (rule == null) "📜 새로운 선박 규율" else "📜 규율 내용 수정", color = Color(0xFF3E2723)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("규율 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = voyageTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("상세 설명") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = voyageTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reward Stepper
                Text("보상 점수 (Reward)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { reward -= 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("-5", color = Color.White) }
                    Text(
                        text = "$reward",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = { reward += 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("+5", color = Color.White) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Penalty Stepper
                Text("패널티 점수 (Penalty)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { penalty -= 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("-5", color = Color.White) }
                    Text(
                        text = "$penalty",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = { penalty += 5 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
                    ) { Text("+5", color = Color.White) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, reward, penalty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Text(if (rule == null) "서명 ✍️" else "수정 완료", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color(0xFF5D4037))
            }
        }
    )
}
