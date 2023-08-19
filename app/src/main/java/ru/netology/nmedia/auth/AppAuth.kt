package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.AuthModelState

class AppAuth private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<AuthModel?>(null)
    val state = _state.asStateFlow()
    var pushToken: PushToken? = null

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0)

        if (!prefs.contains(TOKEN_KEY) || token == null) {
            prefs.edit { clear() }
        } else {
            _state.value = AuthModel(id, token)
        }
        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String){
        prefs.edit {
            putString(TOKEN_KEY,token)
            putLong(ID_KEY, id)
        }

        _state.value = AuthModel(id,token)
        sendPushToken()
    }



    @Synchronized
    fun removeAuth(){
        _state.value = AuthModel()
        with(prefs.edit()){
            clear()
            commit()
        }
        sendPushToken()
    }

    @Synchronized
    fun clearAuth(){
        prefs.edit { clear() }
        _state.value  = null
    }

    fun sendPushToken(token: String? = null){
        GlobalScope.launch {
            val tokenDto = PushToken(token ?: Firebase.messaging.token.await())

            kotlin.runCatching {
                PostApi.service.sendPushToken(tokenDto)
            }

            pushToken = tokenDto
        }
    }



    fun isUserValid() = state.value != null

    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        private var INSTANCE: AppAuth? = null

        //2
        fun getInstance(): AppAuth = requireNotNull(INSTANCE)

        //1
        fun initApp(context: Context) {
            INSTANCE = AppAuth(context)
        }
    }
}