package com.example.studysmart.domain.modal

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studysmart.presentation.theme.gradient1
import com.example.studysmart.presentation.theme.gradient2
import com.example.studysmart.presentation.theme.gradient3
import com.example.studysmart.presentation.theme.gradient4
import com.example.studysmart.presentation.theme.gradient5

@Entity
data class Subject(
    val name: String,
    val goalHour: Float,
    val color: List<Int>,
    @PrimaryKey(autoGenerate = true)
    val subjectId: Int? = null
){
    companion object{
        val subjectcardColors = listOf(gradient1, gradient2, gradient3, gradient4, gradient5)
    }
}
