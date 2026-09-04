package com.example.ui

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    // Please ensure you replace this with your actual Web Client ID from Firebase Console / Google Cloud Console
    private val WEB_CLIENT_ID = "806333696340-pvf4hu1cqfsjdv0g8s7c2bdmh9mq6lpm.apps.googleusercontent.com"

    suspend fun signInWithGoogle(activity: android.app.Activity): Boolean {
        try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity,
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                
                auth.signInWithCredential(firebaseCredential).await()
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e("AuthManager", "Google sign-in failed", e)
            return false
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "signInWithEmail:failure", e)
            throw e
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "createUserWithEmail:failure", e)
            throw e
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            Log.e("AuthManager", "sendPasswordResetEmail:failure", e)
            false
        }
    }

    fun startPhoneLogin(
        activity: android.app.Activity,
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: () -> Unit,
        onVerificationFailed: (Exception) -> Unit
    ) {
        val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) onVerificationCompleted()
                        else onVerificationFailed(task.exception ?: Exception("Auto-verification failed"))
                    }
                }
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    onVerificationFailed(e)
                }
                override fun onCodeSent(verifId: String, token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                    onCodeSent(verifId)
                }
            })
            .build()
        com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(verificationId: String, code: String): Boolean {
        return try {
            val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, code)
            auth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadAvatar(uri: android.net.Uri): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val user = auth.currentUser ?: return@withContext false
            val apiKey = "101032a7d9af432b297103569b141ee9"
            
            // Read image bytes
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes == null) return@withContext false
            
            // Upload to ImgBB using Multipart
            val client = okhttp3.OkHttpClient()
            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("key", apiKey)
                .addFormDataPart("image", "avatar.jpg", okhttp3.RequestBody.create(null, bytes))
                .build()
                
            val request = okhttp3.Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build()
                
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            
            val json = org.json.JSONObject(response.body?.string() ?: "")
            val imageUrl = json.getJSONObject("data").getString("url")
            
            // Update Auth Profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setPhotoUri(android.net.Uri.parse(imageUrl))
                .build()
                
            user.updateProfile(profileUpdates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
