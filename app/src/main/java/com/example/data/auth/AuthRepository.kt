package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dedsec_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PRIMARY_OWNER = "jeanpierreowner@gmail.com"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
        private const val KEY_CURRENT_USER_NAME = "current_user_name"
        private const val KEY_USERS_JSON = "all_registered_users"
    }

    private val _currentUser = MutableStateFlow<DedsecUser?>(null)
    val currentUser: StateFlow<DedsecUser?> = _currentUser.asStateFlow()

    private val _usersList = MutableStateFlow<List<DedsecUser>>(emptyList())
    val usersList: StateFlow<List<DedsecUser>> = _usersList.asStateFlow()

    init {
        loadUsers()
        loadSession()
    }

    private fun loadUsers() {
        val saved = prefs.getString(KEY_USERS_JSON, null)
        val list = mutableListOf<DedsecUser>()

        // Always ensure primary admin exists and is admin & unblocked
        var primaryFound = false
        if (!saved.isNullOrBlank()) {
            try {
                val array = JSONArray(saved)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val email = obj.getString("email").trim().lowercase()
                    val isPrimary = email.equals(PRIMARY_OWNER, ignoreCase = true)
                    if (isPrimary) primaryFound = true
                    list.add(
                        DedsecUser(
                            email = email,
                            displayName = obj.optString("displayName", email.substringBefore("@")),
                            isAdmin = if (isPrimary) true else obj.optBoolean("isAdmin", false),
                            isBlocked = if (isPrimary) false else obj.optBoolean("isBlocked", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            lastLoginAt = obj.optLong("lastLoginAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                // fallback
            }
        }

        if (!primaryFound) {
            list.add(
                DedsecUser(
                    email = PRIMARY_OWNER.lowercase(),
                    displayName = "Jean Pierre (Primary Owner)",
                    isAdmin = true,
                    isBlocked = false,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
            )
            saveUsersInternal(list)
        }

        _usersList.value = list
    }

    private fun saveUsersInternal(list: List<DedsecUser>) {
        try {
            val array = JSONArray()
            list.forEach { u ->
                val obj = JSONObject().apply {
                    put("email", u.email.lowercase())
                    put("displayName", u.displayName)
                    put("isAdmin", u.isAdmin || u.email.equals(PRIMARY_OWNER, ignoreCase = true))
                    put("isBlocked", if (u.email.equals(PRIMARY_OWNER, ignoreCase = true)) false else u.isBlocked)
                    put("createdAt", u.createdAt)
                    put("lastLoginAt", u.lastLoginAt)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_USERS_JSON, array.toString()).apply()
            _usersList.value = list
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun loadSession() {
        val email = prefs.getString(KEY_CURRENT_USER_EMAIL, null)
        if (email.isNullOrBlank()) {
            _currentUser.value = null
            return
        }
        val cleanEmail = email.trim().lowercase()
        val user = _usersList.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
        if (user != null && !user.isBlocked) {
            _currentUser.value = user
        } else {
            // Blocked or missing session
            _currentUser.value = null
            prefs.edit().remove(KEY_CURRENT_USER_EMAIL).remove(KEY_CURRENT_USER_NAME).apply()
        }
    }

    /**
     * Sign in or register user with Google OAuth credentials
     */
    fun onGoogleSignInSuccess(email: String, displayName: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        val isOwner = cleanEmail.equals(PRIMARY_OWNER, ignoreCase = true)

        val currentList = _usersList.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            if (existing.isBlocked && !isOwner) {
                return Pair(false, "ACESSO BLOQUEADO // Este usuário foi desativado pelo administrador.")
            }
            val updated = existing.copy(
                displayName = displayName.ifBlank { existing.displayName },
                isAdmin = if (isOwner) true else existing.isAdmin,
                isBlocked = if (isOwner) false else existing.isBlocked,
                lastLoginAt = System.currentTimeMillis()
            )
            currentList[existingIndex] = updated
            saveUsersInternal(currentList)
            _currentUser.value = updated
            prefs.edit()
                .putString(KEY_CURRENT_USER_EMAIL, updated.email)
                .putString(KEY_CURRENT_USER_NAME, updated.displayName)
                .apply()
            return Pair(true, "AUTORIZADO // Bem-vindo de volta: ${updated.displayName}")
        } else {
            // New user registering via Google Login
            val newUser = DedsecUser(
                email = cleanEmail,
                displayName = displayName.ifBlank { cleanEmail.substringBefore("@") },
                isAdmin = isOwner,
                isBlocked = false,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            currentList.add(newUser)
            saveUsersInternal(currentList)
            _currentUser.value = newUser
            prefs.edit()
                .putString(KEY_CURRENT_USER_EMAIL, newUser.email)
                .putString(KEY_CURRENT_USER_NAME, newUser.displayName)
                .apply()
            return Pair(true, "NOVA CONTA VINCULADA // Acesso concedido para $cleanEmail")
        }
    }

    fun signOut() {
        _currentUser.value = null
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).remove(KEY_CURRENT_USER_NAME).apply()
    }

    /**
     * Add an admin by email
     */
    fun addAdminEmail(email: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Pair(false, "E-mail inválido. Digite um e-mail válido (ex: user@gmail.com)")
        }

        val list = _usersList.value.toMutableList()
        val index = list.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }

        if (index >= 0) {
            val user = list[index]
            list[index] = user.copy(isAdmin = true, isBlocked = false)
            saveUsersInternal(list)
            // If current user is this one, update session
            if (_currentUser.value?.email.equals(cleanEmail, ignoreCase = true)) {
                _currentUser.value = list[index]
            }
            return Pair(true, "SUCESSO // Privilégios de Administrador concedidos para $cleanEmail")
        } else {
            // Pre-authorize new admin user
            val newUser = DedsecUser(
                email = cleanEmail,
                displayName = cleanEmail.substringBefore("@"),
                isAdmin = true,
                isBlocked = false
            )
            list.add(newUser)
            saveUsersInternal(list)
            return Pair(true, "SUCESSO // Novo Administrador pré-cadastrado: $cleanEmail")
        }
    }

    /**
     * Block/unblock or revoke access to a user
     */
    fun setUserBlocked(email: String, blocked: Boolean): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.equals(PRIMARY_OWNER, ignoreCase = true)) {
            return Pair(false, "ERRO // Não é permitido bloquear o proprietário principal ($PRIMARY_OWNER)")
        }

        val list = _usersList.value.toMutableList()
        val index = list.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }
        if (index >= 0) {
            val user = list[index]
            list[index] = user.copy(isBlocked = blocked)
            saveUsersInternal(list)
            // If the blocked user is currently logged in, invalidate immediately
            if (blocked && _currentUser.value?.email.equals(cleanEmail, ignoreCase = true)) {
                signOut()
            }
            return Pair(true, if (blocked) "USUÁRIO BLOQUEADO // $cleanEmail não tem mais acesso." else "USUÁRIO DESBLOQUEADO // $cleanEmail liberado.")
        } else {
            return Pair(false, "Usuário não encontrado.")
        }
    }

    /**
     * Delete user completely
     */
    fun removeUser(email: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.equals(PRIMARY_OWNER, ignoreCase = true)) {
            return Pair(false, "ERRO // O proprietário principal não pode ser excluído.")
        }
        val list = _usersList.value.filterNot { it.email.equals(cleanEmail, ignoreCase = true) }
        saveUsersInternal(list)
        if (_currentUser.value?.email.equals(cleanEmail, ignoreCase = true)) {
            signOut()
        }
        return Pair(true, "USUÁRIO REMOVIDO // Registro excluído: $cleanEmail")
    }

    /**
     * Promote / Demote admin
     */
    fun toggleAdmin(email: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.equals(PRIMARY_OWNER, ignoreCase = true)) {
            return Pair(false, "O proprietário primário é permanentemente Administrador.")
        }
        val list = _usersList.value.toMutableList()
        val index = list.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }
        if (index >= 0) {
            val user = list[index]
            val newAdminState = !user.isAdmin
            list[index] = user.copy(isAdmin = newAdminState)
            saveUsersInternal(list)
            if (_currentUser.value?.email.equals(cleanEmail, ignoreCase = true)) {
                _currentUser.value = list[index]
            }
            return Pair(true, if (newAdminState) "$cleanEmail agora é Administrador" else "Privilégio de administrador removido de $cleanEmail")
        }
        return Pair(false, "Usuário não encontrado.")
    }
}
