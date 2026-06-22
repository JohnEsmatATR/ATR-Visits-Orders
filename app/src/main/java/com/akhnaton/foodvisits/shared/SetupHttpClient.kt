package com.akhnaton.foodvisits.shared

import android.annotation.SuppressLint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class SetupHttpClient {

    fun setupOkHttpClient(): OkHttpClient {

        val REQUEST_TIMEOUT = 90 // 1.5minute

        val builder = OkHttpClient.Builder()
            .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        try {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        p0: Array<out X509Certificate>?,
                        authType: String?
                    ) {

                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String,
                    ) {

                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)

            // verify hostname
            builder.hostnameVerifier { hostname, session ->
                hostname == "sales.atr-eg.com" || hostname == "preweb.atr-eg.com"
            }

            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            builder.addInterceptor(
                Interceptor { chain ->

                    val token =
                        SharedPreferencesHelper
                            .getInstance()
                            .getUserToken() ?: ""

                    val request =
                        chain.request()
                            .newBuilder()
                            .addHeader(
                                "Authorization",
                                "Bearer $token"
                            )
                            .build()

                    chain.proceed(request)
                }
            )

            builder.authenticator(
                TokenAuthenticator()
            )

            builder.addInterceptor(httpLoggingInterceptor)

            return builder.build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
