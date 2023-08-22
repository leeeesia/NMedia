package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.AuthModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

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

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface  AppAuthEntryPoint{
        fun getApiService(): PostApiService
    }

    fun sendPushToken(token: String? = null){
        GlobalScope.launch {
            val tokenDto = PushToken(token ?: Firebase.messaging.token.await())
            val entryPoint =
            EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            kotlin.runCatching {
                entryPoint.getApiService() .sendPushToken(tokenDto)
            }

            pushToken = tokenDto
        }
    }



    fun isUserValid() = state.value != null

    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"


    }
}