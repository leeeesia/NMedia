package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.model.AuthResponse
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.ApiError
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: PostRepository,
): ViewModel() {


    private val _state = MutableLiveData(AuthModelState())


    fun signUp(login: String, password: String , name:String){
        viewModelScope.launch {
            try {
                val response = repository.signUp(login, password, name)
                response.token?.let { appAuth.setAuth(response.id,response.token) }
                _state.value = AuthModelState(isActing = true)
            }catch (e: Exception){
                val resp = if (e is ApiError) AuthResponse(e.status, e.code) else AuthResponse()
                _state.postValue(
                    AuthModelState(error = true, response = resp)
                )
            }
        }
    }
}
