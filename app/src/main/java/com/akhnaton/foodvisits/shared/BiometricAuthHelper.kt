package com.akhnaton.foodvisits.shared

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthHelper(private val activity: FragmentActivity) {

    fun authenticate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            })

        val promptInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            BiometricPrompt.PromptInfo.Builder()
                .setTitle("المصادقة الحيوية")
                .setSubtitle("استخدم بصمتك أو قفل الجهاز للدخول")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        } else {

            BiometricPrompt.PromptInfo.Builder()
                .setTitle("المصادقة بالبصمة")
                .setSubtitle("استخدم بصمتك للدخول")
                .setDescription("قم بمسح بصمتك للاستمرار")
                .setNegativeButtonText("إلغاء")
                .build()
        }

        biometricPrompt.authenticate(promptInfo)
    }
}
