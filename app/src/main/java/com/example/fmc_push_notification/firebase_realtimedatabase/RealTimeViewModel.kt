package com.example.fmc_push_notification.firebase_realtimedatabase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fmc_push_notification.firebase.ResultState
import kotlinx.coroutines.launch

class RealTimeViewModel(private val repository: RealTimeRepository) : ViewModel() {

    private val _res: MutableState<itemsState> = mutableStateOf(itemsState())
    val res: State<itemsState> = _res

    fun insert(items: RealTimeUser.RealTimeItems) = repository.insert(items)

    init {
        viewModelScope.launch {
            repository.getItems().collect { result ->
                when (result) {
                    is ResultState.Error -> {
                        _res.value = itemsState(error = result.error.message ?: "Unknown error")
                    }
                    ResultState.Loading -> {
                        _res.value = itemsState(isLoading = true)
                    }
                    is ResultState.Success -> {
                        _res.value = itemsState(item = result.response)
                    }
                }
            }
        }
    }

    fun delete(key: String) = repository.delete(key)
    fun update(item: RealTimeUser) = repository.update(item)
}

data class itemsState(
    val item: List<RealTimeUser> = emptyList(),
    val error: String = "",
    val isLoading: Boolean = false
)

class RealTimeViewModelFactory(
    private val repository: RealTimeDbRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealTimeViewModel::class.java)) {
            return RealTimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
