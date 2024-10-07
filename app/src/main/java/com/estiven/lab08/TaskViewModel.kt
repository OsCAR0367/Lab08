import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estiven.lab08.Task
import com.estiven.lab08.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow("date")
    val sortOrder: StateFlow<String> = _sortOrder

    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getSortedTasks(_sortOrder.value, _isAscending.value)
        }
    }

    fun addTask(description: String) {
        viewModelScope.launch {
            val newTask = Task(description = description)
            dao.insertTask(newTask)
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task)
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            loadTasks()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updatedTask)
            loadTasks()
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            loadTasks()
        }
    }

    fun searchTasks(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _tasks.value = if (query.isBlank()) {
                dao.getSortedTasks(_sortOrder.value, _isAscending.value)
            } else {
                dao.searchTasks(query)
            }
        }
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
        loadTasks()
    }

    fun toggleSortDirection() {
        _isAscending.value = !_isAscending.value
        loadTasks()
    }
}