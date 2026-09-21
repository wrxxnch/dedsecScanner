package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles Credential Manager Google Sign-In flow.
 */
class GoogleSignInHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(
        serverClientId: String? = null
    ): Pair<Boolean, Pair<String, String>?> = withContext(Dispatchers.IO) {
        try {
            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)

            // If a web client ID is available, pass it, otherwise fallback gracefully
            if (!serverClientId.isNullOrBlank()) {
                googleIdOptionBuilder.setServerClientId(serverClientId)
            } else {
                // Default placeholder to allow SDK invocation without crash
                googleIdOptionBuilder.setServerClientId("66338cbc-google-auth.apps.googleusercontent.com")
            }

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionBuilder.build())
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                Log.i("GoogleSignInHelper", "Signed in as: $email ($displayName)")
                return@withContext Pair(true, Pair(email, displayName))
            } else {
                Log.w("GoogleSignInHelper", "Unexpected credential type: ${credential.type}")
                return@withContext Pair(false, null)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleSignInHelper", "User cancelled Google sign in: ${e.message}")
            return@withContext Pair(false, null)
        } catch (e: GetCredentialException) {
            Log.w("GoogleSignInHelper", "Credential manager failure: ${e.message}")
            return@withContext Pair(false, null)
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Google Sign In error: ${e.message}", e)
            return@withContext Pair(false, null)
        }
    }
}
