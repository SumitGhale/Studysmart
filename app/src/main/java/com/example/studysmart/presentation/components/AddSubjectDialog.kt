package com.example.studysmart.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studysmart.domain.modal.Subject

@Composable
fun AddSubjectDialog(
    title: String = "Add / Update subject",
    isOpen: Boolean,
    subjectName: String,
    goalHours: String,
    onDismissRequest: () -> Unit,
    onConfirmButton: () -> Unit,
    selectedColor:List<Color>,
    onColorChange: (List<Color>) -> Unit,
    onSubjectNameChange: (String) -> Unit,
    onGoalHourChange: (String) -> Unit
){
    var subjectnameError by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var goalHourError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    subjectnameError = when{
        subjectName.isBlank() -> "Please enter subject name"
        subjectName.length > 20 -> "Subject name is too long"
        subjectName.length < 2 -> "Subject name is too short"
        else -> null
    }
    goalHourError = when{
        goalHours.isBlank() -> "Please enter target hour"
        goalHours.toFloatOrNull() == null -> "Invalid number"
        goalHours.toFloat() > 24F -> "Maximum 24 hours allowed"
        goalHours.toFloat() < 1F-> "Minimum 1 hours allowed"
        else -> null
    }

    if (isOpen){
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            title = { Text(text = title) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ){
                        Subject.subjectcardColors.forEach { colors -> 
                            Box(modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 1.dp,
                                    if (colors == selectedColor) Color.Black else Color.Transparent,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .background(brush = Brush.verticalGradient(colors))
                                .clickable { onColorChange(colors) })
                        }
                    }
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = { Text(text = "Subject name")},
                        singleLine = true,
                        isError = subjectnameError != null && subjectName.isNotBlank(),
                        supportingText = { Text(text = subjectnameError.orEmpty())}
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = goalHours,
                        onValueChange = onGoalHourChange,
                        label = { Text(text = "Goal study hours")},
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = goalHourError != null && goalHours.isNotBlank(),
                        supportingText = { Text(text = goalHourError.orEmpty())}
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissRequest() }) {
                    Text(text = "Dismiss")
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmButton() },
                    enabled = subjectnameError == null && goalHourError == null) {
                    Text(text = "Save")
                }
            }
        )
    }
}