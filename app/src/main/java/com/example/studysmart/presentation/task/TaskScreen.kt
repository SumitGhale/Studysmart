package com.example.studysmart.presentation.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.presentation.components.DeleteDialog
import com.example.studysmart.presentation.components.SubjectListBottomSheet
import com.example.studysmart.presentation.components.TaskCheckBox
import com.example.studysmart.presentation.components.TaskDatePicker
import com.example.studysmart.util.Priority
import com.example.studysmart.util.SnackBarEvent
import com.example.studysmart.util.changeMillisToDateString
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant

data class TaskScreenNavArgs(
    val taskId: Int?,
    val subjectId: Int?
)

@Destination(navArgsDelegate = TaskScreenNavArgs::class)
@Composable
fun TaskScreenRoute(
    navigator: DestinationsNavigator
){
    val viewModel: TaskViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    TaskScreen(
        state = state,
        snackBarEvent = viewModel.snackbarEventFlow,
        onEvent = viewModel::onEvent,
        onBackButtonClicked = { navigator.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreen(
    state: TaskState,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onEvent: (TaskEvent) -> Unit,
    onBackButtonClicked: () -> Unit
){

    val scope = rememberCoroutineScope()
    var taskTitleError by remember {
        mutableStateOf<String?>(null)
    }

    var isDeleteDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var isDatePickerDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()
    var isBottomSheetOpen by remember {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli(),
        selectableDates = object : SelectableDates{
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis()
            }
        }
    )

    taskTitleError = when {
        state.title.isBlank() -> "Please enter task title"
        state.title.length < 4 -> "Title should have more than 4 charaters"
        state.title.length > 24 -> "Title should not have more than 24 characters"
        else -> null
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

                SnackBarEvent.NavigateUp -> {
                    onBackButtonClicked()
                }
            }
        }
    }

    DeleteDialog(isOpen = isDeleteDialogOpen,
        bodyText = "Do you want to delete this task?",
        onDismissRequest = { isDeleteDialogOpen = false },
        onConfirmButton = {
            onEvent(TaskEvent.DeleteTask)
            isDeleteDialogOpen = false
        }
    )

    TaskDatePicker(
        state = datePickerState,
        isOpen = isDatePickerDialogOpen,
        onConfirmButtonClicked = { isDatePickerDialogOpen = false },
        onDismissButtonClicked = {
            onEvent(TaskEvent.OnDateChange(millis = datePickerState.selectedDateMillis))
            isDatePickerDialogOpen = false
        }
    )

    SubjectListBottomSheet(
        sheetState = sheetState,
        isOpen = isBottomSheetOpen,
        subjects = state.subjects,
        onSubjectClicked = {subject ->
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) isBottomSheetOpen = false
            }
            onEvent(TaskEvent.OnRelatedSubjectSelect(subject))
        },
        onDismissRequest = {isBottomSheetOpen = false}
    )



    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TaskScreenTopBar(
                isTaskExist = state.currentTaskId != null,
                isComplete = state.isTaskComplete,
                checkBoxBorderColor = state.priority.color,
                onBackButtonClicked = onBackButtonClicked,
                onDeleteButtonClicked = {isDeleteDialogOpen = true},
                onCheckBoxClick = {
                    onEvent(TaskEvent.OnIsCompleteChange)
                })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 12.dp)
                .verticalScroll(state = rememberScrollState())
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                singleLine = true,
                label = { Text(text = "Title")},
                onValueChange = {onEvent(TaskEvent.OnTitleChange(it))},
                isError = taskTitleError != null && state.title.isNotBlank(),
                supportingText = { Text(text = taskTitleError.orEmpty())}
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.description,
                label = { Text(text = "Description")},
                onValueChange = {onEvent(TaskEvent.OnDescriptionChange(it))}
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Due Date", style = MaterialTheme.typography.bodySmall)
            Row (modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text(text = state.dueDate.changeMillisToDateString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { isDatePickerDialogOpen = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calender icon")
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Priority.entries.forEach{priority ->
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        backgroundColor = priority.color,
                        borderColor = if (priority == state.priority) {
                            Color.White
                        }else Color.Transparent,
                        label = priority.title,
                        labelColor = if (priority == state.priority) {
                            Color.White
                        }else Color.White.copy(alpha = 0.7f),
                        onCLick = {onEvent(TaskEvent.OnPriorityChange(priority))}
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Related to Subject", style = MaterialTheme.typography.bodySmall)
            Row (modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                val firstSubject = state.subjects.firstOrNull()?. name ?:""
                Text(
                    text = state.relatedToSubject?:firstSubject,
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = { isBottomSheetOpen = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Drop down icon")
                }
            }

            Button(
                enabled = taskTitleError == null,
                onClick = { onEvent(TaskEvent.SaveTask) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)) {
                Text(text = "Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreenTopBar(
    isTaskExist: Boolean,
    isComplete: Boolean,
    checkBoxBorderColor: Color,
    onBackButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onCheckBoxClick: () -> Unit
){
    TopAppBar(
        title = { Text(text = "Task", style = MaterialTheme.typography.headlineMedium)},
        navigationIcon =
        {
            IconButton(onClick = onBackButtonClicked ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back button")
        }},
        actions = {
            if(isTaskExist){
                TaskCheckBox(
                    isComplete = isComplete,
                    borderColor = checkBoxBorderColor,
                    onCheckBoxClicked = onCheckBoxClick
                )
                IconButton(onClick = onDeleteButtonClicked) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Button")
                }
            }
        }
    )
}

@Composable
fun PriorityButton(
    modifier: Modifier,
    backgroundColor: Color,
    borderColor: Color,
    label: String,
    labelColor: Color,
    onCLick: () -> Unit
){
    Box(modifier = modifier
        .background(backgroundColor)
        .clickable { onCLick() }
        .padding(5.dp)
        .border(1.dp, borderColor, RoundedCornerShape(5.dp))
        .padding(5.dp),
        contentAlignment = Alignment.Center){
        Text(text = label, color = labelColor)
    }
}