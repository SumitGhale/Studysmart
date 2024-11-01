package com.example.studysmart.presentation.session

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.modal.Session
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.util.SnackBarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class SessionScreenViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository
): ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state = combine(
        _state,
        sessionRepository.getAllSessions(),
        subjectRepository.getAllSubjects()
    ){ state, sessions, subjects ->
        state.copy(
            subjects = subjects,
            sessions = sessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionState()
    )

    fun onEvent(event: SessionEvent){
        when(event){
            SessionEvent.DeleteSession -> {

            }
            SessionEvent.NotifyToUpdateSubject -> {

            }
            is SessionEvent.OnDeleteSessionButtonClick -> {

            }
            is SessionEvent.OnRelatedSubjectChange -> {

            }
            is SessionEvent.SaveSession -> {
                insertSession(event.duration)
            }
            is SessionEvent.UpdateSubjectIdAndRelatedSubject -> {

            }
        }
    }

    private val _snackbarEventFlow = MutableSharedFlow<SnackBarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    private fun insertSession(duration: Long){
        viewModelScope.launch {
            try {
                sessionRepository.insertSession(Session(
                    relatedToSubject =  state.value.relatedToSubject ?: "",
                    date = Instant.now().toEpochMilli(),
                    duration = duration,
                    sessionSubjectId = state.value.subjectId ?: -1
                ))
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        "Session saved successfully",
                    )
                )
            }catch (e:Exception){
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        "couldn't save session. ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }


        }
    }
}