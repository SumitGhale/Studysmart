package com.example.studysmart.presentation.session

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.DeleteDialog
import com.example.studysmart.presentation.components.SubjectListBottomSheet
import com.example.studysmart.presentation.components.studySessionList
import com.example.studysmart.presentation.theme.Red
import com.example.studysmart.util.Constants.ACTION_SERVICE_CANCEL
import com.example.studysmart.util.Constants.ACTION_SERVICE_START
import com.example.studysmart.util.Constants.ACTION_SERVICE_STOP
import com.example.studysmart.util.SnackBarEvent
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit

@Destination(
    deepLinks = [
        DeepLink(
            action = Intent.ACTION_VIEW,
            uriPattern = "study_smart://dashboard/session"
        )
    ]
)
@Composable
fun SessionScreenRoute(
    navigator: DestinationsNavigator,
    timerService: StudySessionTimerService
){
    val viewModel: SessionScreenViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SessionScreen(
        onBackButtonClicked = {navigator.navigateUp()},
        timerService = timerService,
        sessionState = state,
        onEvent = viewModel::onEvent,
        snackBarEvent = viewModel.snackbarEventFlow,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreen(
    sessionState: SessionState,
    onEvent: (SessionEvent) -> Unit,
    onBackButtonClicked: () -> Unit,
    timerService: StudySessionTimerService,
    snackBarEvent: SharedFlow<SnackBarEvent>,
){
    val hours by timerService.hours
    val minutes by timerService.minutes
    val seconds by timerService.seconds
    val currentTimerState = timerService.currentTimerState

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState()
    var isBottomSheetOpen by remember {
        mutableStateOf(false)
    }
    var isDeleteDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(key1 = true) {
        snackBarEvent.collectLatest{event ->
            when(event){
                is SnackBarEvent.ShowSnackBar ->{
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackBarEvent.NavigateUp -> TODO()
            }
        }
    }

    SubjectListBottomSheet(
        sheetState = sheetState,
        isOpen = isBottomSheetOpen,
        subjects = sessionState.subjects,

        onSubjectClicked = {subject ->
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) isBottomSheetOpen = false
            }
            onEvent(SessionEvent.OnRelatedSubjectChange(subject))
        },
        onDismissRequest = {isBottomSheetOpen = false}
    )

    DeleteDialog(isOpen = isDeleteDialogOpen,
        bodyText = "Do you want to delete this task?",
        onDismissRequest = { isDeleteDialogOpen = false },
        onConfirmButton = {
            onEvent(SessionEvent.DeleteSession)
            isDeleteDialogOpen = false
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = { SessionScreenTopBar( onBackButtonClicked = onBackButtonClicked) }
    ) {
        LazyColumn(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            item {
                TimerSection(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds)
            }
            item {
                RelatedtoSubjectSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    relatedToSubject = "",
                    onDropDownClicked = {isBottomSheetOpen = true}
                )
            }
            item { ButtonSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                onStartButtonClicked = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = if (currentTimerState.value == TimerState.STARTED){
                            ACTION_SERVICE_STOP
                        }else{
                            ACTION_SERVICE_START
                        }
                    )
                },
                onCancelButtonClicked = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_CANCEL)
                },
                onFinishButtonClicked = {
                    val duration = timerService.duration.toLong(DurationUnit.SECONDS)
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_CANCEL)
                    onEvent(SessionEvent.SaveSession(duration))
                },
                timerState = currentTimerState.value,
                seconds = seconds)
            }
            studySessionList(
                sectionTitle = "Study session history",
                sessions = sessionState.sessions,
                onDeleteIconClick = { isDeleteDialogOpen = true
                onEvent(SessionEvent.DeleteSession)}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreenTopBar(
    onBackButtonClicked: () -> Unit,
){
    TopAppBar(
        title = { Text(text = "Study Sessions",
            style = MaterialTheme.typography.headlineMedium) },
        navigationIcon =
        {
            IconButton(onClick = onBackButtonClicked ) {
                Icon(imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back button")
            }
        },
    )
}


@Composable
fun TimerSection(
    modifier: Modifier,
    hours: String,
    minutes: String,
    seconds: String

) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .border(5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row {
                AnimatedContent(
                    targetState = hours,
                    label = hours,
                    transitionSpec = { timerTextAnimation()}) { hours ->
                    Text(
                        text = hours,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
                AnimatedContent(
                    targetState = minutes,
                    label = minutes,
                    transitionSpec = { timerTextAnimation()}) { minutes ->
                    Text(
                        text = minutes,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
                AnimatedContent(
                    targetState = seconds,
                    label = seconds,
                    transitionSpec = { timerTextAnimation()}) { seconds ->
                    Text(
                        text = seconds,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
            }

        }
    }

}
@Composable
private fun RelatedtoSubjectSection(
    modifier: Modifier,
    relatedToSubject: String,
    onDropDownClicked: () -> Unit
){
    Column(
        modifier = modifier,
    ) {
        Text(text = "Related to Subject", style = MaterialTheme.typography.bodySmall)
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Text(text = "English", style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onDropDownClicked) {
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Drop down icon")
            }
        }
    }

}

@Composable
private fun ButtonSection(
    modifier: Modifier,
    onStartButtonClicked:() -> Unit,
    onCancelButtonClicked:() -> Unit,
    onFinishButtonClicked:() -> Unit,
    timerState: TimerState,
    seconds: String
){
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Button(onClick = onStartButtonClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (timerState == TimerState.STARTED) Red else MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)){
            Text(text = when(timerState){
                TimerState.STARTED-> "Stop"
                TimerState.STOPPED-> "Resume"
                else -> "Start"
                }
            )
        }
        Button(onClick = onFinishButtonClicked,
            enabled = seconds!= "00" && timerState != TimerState.STARTED) {
            Text(text = "Finish",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
        }
        Button(onClick = onCancelButtonClicked,
            enabled = seconds!= "00" && timerState != TimerState.STARTED) {
            Text(text = "Cancel",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
        }
    }
}

private fun timerTextAnimation(duration: Int = 600): ContentTransform{
    return slideInVertically (animationSpec = tween(duration)){ fullHeight -> fullHeight } +
            fadeIn(animationSpec = tween(duration)) togetherWith
            slideOutVertically (animationSpec = tween(duration)){ fullHeight -> -fullHeight } +
            fadeOut(animationSpec = tween(duration))
}