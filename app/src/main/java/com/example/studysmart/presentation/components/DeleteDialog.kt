package com.example.studysmart.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteDialog(
    title: String = "Delete subject",
    isOpen: Boolean,
    bodyText: String,
    onDismissRequest: () -> Unit,
    onConfirmButton: () -> Unit,
){
    if (isOpen){
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            title = { Text(text = title) },
            text = {
                Text(text = bodyText)
            },
            dismissButton = {
                TextButton(onClick = { onDismissRequest() }) {
                    Text(text = "Dismiss")
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmButton() }) {
                    Text(text = "Save")
                }
            }
        )
    }
}