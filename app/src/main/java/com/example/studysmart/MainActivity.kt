package com.example.studysmart

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import com.example.studysmart.domain.modal.Session
import com.example.studysmart.domain.modal.Subject
import com.example.studysmart.domain.modal.Task
import com.example.studysmart.presentation.NavGraphs
import com.example.studysmart.presentation.destinations.SessionScreenRouteDestination
import com.example.studysmart.presentation.session.StudySessionTimerService
import com.example.studysmart.presentation.theme.StudySmartTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isBound by mutableStateOf(false)
    private lateinit var timerService: StudySessionTimerService

    private val connection = object: ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as StudySessionTimerService.StudySessionTimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, StudySessionTimerService::class.java).also { intent ->
            bindService(intent,connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (isBound){
                StudySmartTheme {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        dependenciesContainerBuilder =
                        {
                            dependency(SessionScreenRouteDestination){timerService}
                        }
                        )
                }
            }
        }
        requestPermission()
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}
val subjects = listOf(
    Subject(name = "Math", goalHour = 10f, color = Subject.subjectcardColors[2].map { it.toArgb() }, 0),
    Subject(name = "Science", goalHour = 10f, color = Subject.subjectcardColors[3].map { it.toArgb() },0),
    Subject(name = "Physics", goalHour = 10f, color = Subject.subjectcardColors[1].map { it.toArgb() },0),
    Subject(name = "Bio", goalHour = 10f, color = Subject.subjectcardColors[4].map { it.toArgb() },0),
    Subject(name = "Computer", goalHour = 10f, color = Subject.subjectcardColors[0].map { it.toArgb() },0)

)

val tasks = listOf(
    Task("Preparenote", "", 0L, 2, "", false,0,1),
    Task("Do Homework", "", 0L, 0, "", false,0,1),
    Task("Study", "", 0L, 2, "", false,0,1),
    Task("Chores", "", 0L, 1, "", true,0,1),
    Task("Code", "", 0L, 2, "", false,0,1),
    Task("Code again", "", 0L, 2, "", true,0,1),
)

val sessions = listOf(
    Session(relatedToSubject = "English", date = 0L, duration = 2, sessionSubjectId = 0, sessionId = 0),
    Session(relatedToSubject = "Math", date = 0L, duration = 2, sessionSubjectId = 0, sessionId = 0),
    Session(relatedToSubject = "Nepali", date = 0L, duration = 2, sessionSubjectId = 0, sessionId = 0),
    Session(relatedToSubject = "Science", date = 0L, duration = 2, sessionSubjectId = 0, sessionId = 0),
)