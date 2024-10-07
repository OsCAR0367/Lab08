package com.estiven.lab08

import TaskViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        TaskDatabase::class.java,
                        "task_db"
                    )
                        .addMigrations(TaskDatabase.MIGRATION_2_3)
                        .build()

                    val taskDao = db.taskDao()
                    val viewModel = TaskViewModel(taskDao)

                    TaskScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isAscending by viewModel.isAscending.collectAsState() // Asegúrate de tener esta propiedad en tu ViewModel
    var newTaskDescription by remember { mutableStateOf("") }
    var showSortOptions by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchTasks(it) },
            label = { Text("Buscar tareas") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        )

        // Sort options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ordenar por: $sortOrder")
            IconButton(onClick = { showSortOptions = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort")
            }
            IconButton(onClick = { viewModel.toggleSortDirection() }) {
                Icon(
                    if (isAscending) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropDown,
                    contentDescription = "Sort Direction"
                )
            }
        }

        // New task input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Nueva tarea") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTaskDescription.isNotEmpty()) {
                        viewModel.addTask(newTaskDescription)
                        newTaskDescription = ""
                    }
                }
            ) {
                Text("Agregar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task list
        LazyColumn {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onEdit = { editingTask = task },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        // Delete all tasks button
        Button(
            onClick = { viewModel.deleteAllTasks() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }
    }

    // Sort options dialog
    if (showSortOptions) {
        AlertDialog(
            onDismissRequest = { showSortOptions = false },
            title = { Text("Ordenar por") },
            text = {
                Column {
                    Button(onClick = { viewModel.setSortOrder("name"); showSortOptions = false }) {
                        Text("Nombre")
                    }
                    Button(onClick = { viewModel.setSortOrder("date"); showSortOptions = false }) {
                        Text("Fecha de creación")
                    }
                    Button(onClick = { viewModel.setSortOrder("status"); showSortOptions = false }) {
                        Text("Estado")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showSortOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Edit task dialog
    editingTask?.let { task ->
        var editedDescription by remember { mutableStateOf(task.description) }
        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = { Text("Editar tarea") },
            text = {
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("Descripción") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateTask(task.copy(description = editedDescription))
                    editingTask = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { editingTask = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompletion() }
            )
            Text(
                text = task.description,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
