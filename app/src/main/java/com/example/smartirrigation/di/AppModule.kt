package com.example.smartirrigation.di

import android.content.Context
import android.util.Log
import com.example.smartirrigation.data.repositories.IrrigationRepoImpl
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesService(): HttpClient {
        return HttpClient(OkHttp) {

            install(HttpTimeout) {
                requestTimeoutMillis = 10000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 10000
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HttpClient", message)
                    }
                }
            }

            install(ResponseObserver) {
                onResponse {
                    Log.i("Response", it.status.value.toString())
                }
            }

            defaultRequest {
                accept(ContentType.Text.Plain)
            }
        }
    }

    @Provides
    @Singleton
    fun providesIrrigationRepo(httpClient: HttpClient, @ApplicationContext context: Context): IrrigationRepository {
        return IrrigationRepoImpl(httpClient, context)
    }


}