package com.esra.goaltrackproject.presentation.components.detail

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.esra.goaltrackproject.data.SMARTGoalEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalBottomSheet(
    goal: SMARTGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (SMARTGoalEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    var editedTitle by remember { mutableStateOf(goal.title) }
    var editedTargetValue by remember { mutableStateOf(goal.targetValue.toString()) }
    var isReminderActive by remember { mutableStateOf(goal.isReminderActive) }
    var selectedHour by remember { mutableStateOf(goal.reminderHour ?: 12) }
    var selectedMinute by remember { mutableStateOf(goal.reminderMinute ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Hedefi Düzenle", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editedTitle,
                onValueChange = { editedTitle = it },
                label = { Text("Hedef Başlığı") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedTargetValue,
                onValueChange = { editedTargetValue = it },
                label = { Text("Hedef Değer") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hatırlatıcı Aktif", fontWeight = FontWeight.Medium)
                Switch(
                    checked = isReminderActive,
                    onCheckedChange = { isReminderActive = it }
                )
            }

            if (isReminderActive) {
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedHour = hour
                                selectedMinute = minute
                            },
                            selectedHour,
                            selectedMinute,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Saat Seç: ${String.format("%02d:%02d", selectedHour, selectedMinute)}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updated = goal.copy(
                        title = editedTitle,
                        targetValue = editedTargetValue.toDoubleOrNull() ?: goal.targetValue,
                        isReminderActive = isReminderActive,
                        reminderHour = if (isReminderActive) selectedHour else null,
                        reminderMinute = if (isReminderActive) selectedMinute else null
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Değişiklikleri Kaydet")
            }
        }
    }
}