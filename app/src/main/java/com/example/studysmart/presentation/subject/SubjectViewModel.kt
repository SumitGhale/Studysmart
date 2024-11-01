package com.example.studysmart.presentation.subject

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.modal.Subject
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.presentation.navArgs
import com.example.studysmart.util.SnackBarEvent
import com.example.studysmart.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _state = MutableStateFlow(SubjectState())

    private val navArgs: SubjectScreenNavArgs = savedStateHandle.navArgs()

    val state = combine(
        _state,
        taskRepository.getUpcomingTasksForSubject(navArgs.subjectId),
        taskRepository.getCompletedTasksForSubject(navArgs.subjectId),
        sessionRepository.getRecentTenSessionsForSubject(navArgs.subjectId),
        sessionRepository.getTotalSessionsDurationBySubject(navArgs.subjectId)
    ){state, upcomingTasks, completedTasks, recentTenSessions, totalsessionDuration ->
        state.copy(
            upcomingTasks = upcomingTasks,
            completedTasks = completedTasks,
            recentSessions = recentTenSessions,
            studiedHours = totalsessionDuration.toHours()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SubjectState()
    )

    private val _snackBarEventFlow = MutableSharedFlow<SnackBarEvent>()
    val snackBarEventFlow = _snackBarEventFlow.asSharedFlow()

    init {
        fetchSubject()
    }

    fun event(event: SubjectEvent){
        when(event){
            SubjectEvent.DeleteSession -> {
                deleteSubject()
            }
            SubjectEvent.DeleteSubject -> {

            }
            is SubjectEvent.OnDeleteSessionButtonClick -> TODO()
            is SubjectEvent.OnGoalStudyHoursChange -> {
                _state.update {
                    it.copy(
                        goalStudyHours = event.hours
                    )
                }
            }
            is SubjectEvent.OnSubjectCardColorChange -> {
                _state.update {
                    it.copy(
                        subjectCardColors = event.color
                    )
                }
            }
            is SubjectEvent.OnSubjectNameChange ->  {
                _state.update {
                    it.copy(
                        subjectName = event.name
                    )
                }
            }
            is SubjectEvent.OnTaskIsCompleteChange -> {
                _state.update {
                    it.copy(

                    )
                }
            }
            SubjectEvent.UpdateProgress -> {
                val goalStudyHours = state.value.goalStudyHours.toFloatOrNull()?: 1f
                _state.update {
                    it.copy(
                        progress = (state.value.studiedHours / goalStudyHours).coerceIn(0f, 1f)
                    )
                }
            }
            SubjectEvent.UpdateSubject -> updateSubject()
        }
    }

    private fun updateSubject() {
        viewModelScope.launch {
        try {
                subjectRepository.upsertSubject(Subject(
                    subjectId = state.value.currentSubjectId,
                    name = state.value.subjectName,
                    goalHour = state.value.goalStudyHours.toFloatOrNull() ?: 1F,
                    color = state.value.subjectCardColors.map { it.toArgb() }
                ))
                _snackBarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar("Subject updated successfully.")
                )

            } catch (e: Exception){
                _snackBarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar("Subject update failed + ${e.localizedMessage}.",
                        SnackbarDuration.Long
                    )
                )
            }
        }

    }

    private fun fetchSubject(){
        viewModelScope.launch {
            subjectRepository.getSubjectById(navArgs.subjectId)?.let { subject: Subject ->
                _state.update {
                    it.copy(
                        subjectName = subject.name,
                        goalStudyHours = subject.goalHour.toString(),
                        subjectCardColors = subject.color.map { Color(it) },
                        currentSubjectId = subject.subjectId
                    )
                }
            }
        }
    }

    private fun deleteSubject() {
        viewModelScope.launch {
            try {
                val currentSubjectId = state.value.currentSubjectId
                if (currentSubjectId != null){
                    withContext(Dispatchers.IO){
                        state.value.currentSubjectId?.let {
                            subjectRepository.deleteSubject(it)
                        }
                        _snackBarEventFlow.emit(
                            SnackBarEvent.ShowSnackBar(message = "Subject deleted successfully")
                        )
                        _snackBarEventFlow.emit(
                            SnackBarEvent.NavigateUp
                        )
                    }
                }else{
                    _snackBarEventFlow.emit(
                        SnackBarEvent.ShowSnackBar("No subject to delete")
                    )
                }
            }catch (e: Exception){
                _snackBarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(message = "Failed to delete subject. ${e.localizedMessage}",
                        SnackbarDuration.Long)
                )
            }
        }
    }
}