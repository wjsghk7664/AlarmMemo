package com.example.alarmmemo.presentation.memoLogin

sealed interface UiState<out T> {
    data class Success<T>(val data:T) :UiState<T>
    data class Failure(val e:String) : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data object Init : UiState<Nothing>
}