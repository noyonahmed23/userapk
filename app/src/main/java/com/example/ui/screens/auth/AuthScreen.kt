package com.example.ui.screens.auth

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
  onAuthSuccess: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  var isLoginMode by remember { mutableStateOf(true) }
  var fullName by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
    containerColor = GamingDarkBackground,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Logo & Branding
      Image(
        painter = painterResource(id = R.drawable.img_khelo_logo),
        contentDescription = "খেলো বিডি লোগো",
        modifier = Modifier
          .size(80.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(2.dp, CyberOrange, RoundedCornerShape(16.dp))
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "খেলো বিডি",
        color = TextPrimary,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = 1.sp
      )

      Text(
        text = "ESPORTS TOURNAMENT ARENA",
        color = CyberOrange,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 2.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Card Container
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Tab Switcher (Login / Sign Up)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(GamingDarkSurfaceVariant)
              .padding(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isLoginMode) CyberOrange else Color.Transparent,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  isLoginMode = true
                  errorMessage = null
                  successMessage = null
                }
                .testTag("tab_login")
            ) {
              Text(
                text = "লগইন (Login)",
                color = if (isLoginMode) Color.Black else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (!isLoginMode) CyberCyan else Color.Transparent,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  isLoginMode = false
                  errorMessage = null
                  successMessage = null
                }
                .testTag("tab_signup")
            ) {
              Text(
                text = "রেজিস্ট্রেশন (Sign Up)",
                color = if (!isLoginMode) Color.Black else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Form Fields
          if (!isLoginMode) {
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("পূর্ণ নাম (Full Name)") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_fullname_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = username,
              onValueChange = { username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
              label = { Text("ইউজারনেম (Username)") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_username_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("মোবাইল নম্বর (Phone)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_phone_input")
            )

            Spacer(modifier = Modifier.height(10.dp))
          }

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("ইমেইল ঠিকানা (Email)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_email_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("পাসওয়ার্ড (Password)") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = null,
                  tint = TextSecondary
                )
              }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_password_input")
          )

          if (!isLoginMode) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = confirmPassword,
              onValueChange = { confirmPassword = it },
              label = { Text("পাসওয়ার্ড নিশ্চিত করুন (Confirm)") },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_confirm_password_input")
            )
          }

          errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CyberRed.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = it,
                color = CyberRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          successMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CyberGreen.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = it,
                color = CyberGreen,
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Submit Button
          Button(
            onClick = {
              val trimmedEmail = email.trim()
              val trimmedPass = password.trim()

              if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                errorMessage = "দয়া করে সঠিক ইমেইল ঠিকানা প্রদান করুন।"
                return@Button
              }
              if (trimmedPass.length < 6) {
                errorMessage = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                return@Button
              }

              if (!isLoginMode) {
                if (fullName.trim().isBlank()) {
                  errorMessage = "দয়া করে পূর্ণ নাম লিখুন।"
                  return@Button
                }
                if (username.trim().isBlank()) {
                  errorMessage = "দয়া করে ইউজারনেম লিখুন।"
                  return@Button
                }
                if (trimmedPass != confirmPassword.trim()) {
                  errorMessage = "উভয় পাসওয়ার্ড একই হতে হবে।"
                  return@Button
                }
              }

              errorMessage = null
              isLoading = true

              coroutineScope.launch {
                try {
                  val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
                  if (auth != null) {
                    if (isLoginMode) {
                      val result = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                      val firebaseUser = result.user
                      val user = UserProfile(
                        id = firebaseUser?.uid ?: "user_${System.currentTimeMillis()}",
                        username = firebaseUser?.displayName ?: trimmedEmail.substringBefore("@"),
                        fullName = firebaseUser?.displayName ?: trimmedEmail.substringBefore("@"),
                        phone = phone.trim(),
                        email = trimmedEmail,
                        walletBalance = 0.0,
                        isOnline = true,
                        matchesPlayed = 0,
                        wins = 0,
                        totalEarnings = 0.0,
                        freeFireUid = "",
                        pubgUid = "",
                        isAdmin = false
                      )
                      isLoading = false
                      onAuthSuccess(user)
                    } else {
                      val result = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                      val firebaseUser = result.user
                      firebaseUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                          .setDisplayName(fullName.trim())
                          .build()
                      )?.await()

                      val user = UserProfile(
                        id = firebaseUser?.uid ?: "user_${System.currentTimeMillis()}",
                        username = username.trim(),
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        email = trimmedEmail,
                        walletBalance = 0.0,
                        isOnline = true,
                        matchesPlayed = 0,
                        wins = 0,
                        totalEarnings = 0.0,
                        freeFireUid = "",
                        pubgUid = "",
                        isAdmin = false
                      )
                      isLoading = false
                      onAuthSuccess(user)
                    }
                  } else {
                    throw IllegalStateException(
                      "Firebase Authentication সেটআপ নেই। নিরাপদ লগইন ছাড়া অ্যাকাউন্ট খোলা যাবে না।"
                    )
                  }
                } catch (e: Exception) {
                  isLoading = false
                  errorMessage = e.localizedMessage ?: "অথেন্টিকেশনে সমস্যা হয়েছে! পুনরায় চেষ্টা করুন।"
                }
              }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isLoginMode) CyberOrange else CyberCyan,
              contentColor = Color.Black
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("auth_submit_btn")
          ) {
            if (isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
              Text(
                text = if (isLoginMode) "লগইন করুন" else "রেজিস্ট্রেশন সম্পন্ন করুন",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = GamingCardBorder)
            Text(
              text = "অথবা",
              color = TextSecondary,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = GamingCardBorder)
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Google Sign-In with Credential Manager
          OutlinedButton(
            onClick = {
              coroutineScope.launch {
                isLoading = true
                errorMessage = null
                try {
                  val credentialManager = CredentialManager.create(context)
                  val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("dummy-client-id.apps.googleusercontent.com")
                    .setAutoSelectEnabled(true)
                    .build()

                  val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                  val result = credentialManager.getCredential(
                    request = request,
                    context = context
                  )

                  val credential = result.credential
                  val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                  val idToken = googleIdTokenCredential.idToken

                  val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
                  if (auth != null) {
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCred).await()
                    val firebaseUser = authResult.user
                    val user = UserProfile(
                      id = firebaseUser?.uid ?: "user_g_${System.currentTimeMillis()}",
                      username = firebaseUser?.displayName?.replace(" ", "_") ?: "Google_Player",
                      fullName = firebaseUser?.displayName ?: "Google Player",
                      phone = "",
                      email = firebaseUser?.email ?: "",
                      walletBalance = 0.0,
                      isOnline = true,
                      matchesPlayed = 0,
                      wins = 0,
                      totalEarnings = 0.0,
                      freeFireUid = "",
                      pubgUid = "",
                      isAdmin = false
                    )
                    isLoading = false
                    onAuthSuccess(user)
                  } else {
                    throw IllegalStateException(
                      "Firebase Authentication সেটআপ নেই। Google সাইন-ইন চালু করা যায়নি।"
                    )
                  }
                } catch (e: GetCredentialException) {
                  isLoading = false
                  errorMessage = "Google সাইন-ইন প্রক্রিয়া সম্পন্ন হয়নি (${e.localizedMessage ?: "Cancelled"})."
                } catch (e: Exception) {
                  isLoading = false
                  errorMessage = "Google সাইন-ইন ব্যর্থ হয়েছে। Firebase configuration ও Google Sign-In সেটিংস যাচাই করুন।"

                }
              }
            },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("google_signin_btn")
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Google Sign-In",
              tint = CyberGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Google অ্যাকাউন্ট দিয়ে প্রবেশ করুন",
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = if (isLoginMode) "অ্যাকাউন্ট নেই? রেজিস্ট্রেশন বাটনে ট্যাপ করুন।" else "ইতোমধ্যে অ্যাকাউন্ট আছে? লগইন বাটনে ট্যাপ করুন।",
        color = TextSecondary,
        fontSize = 12.sp
      )
    }
  }
}
