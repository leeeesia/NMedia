package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.model.AuthResponse
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.ApiError

class SignUpViewModel(application: Application): AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    private val _state = MutableLiveData(AuthModelState())


    fun signUp(login: String, password: String , name:String){
        viewModelScope.launch {
            try {
                val response = repository.signUp(login, password, name)
                response.token?.let { AppAuth.getInstance().setAuth(response.id,response.token) }
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
