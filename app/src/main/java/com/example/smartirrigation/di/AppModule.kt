package com.example.smartirrigation.di

import android.content.Context
import android.util.Log
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.data.repositories.IrrigationRepoImpl
import com.example.smartirrigation.data.repositories.PreferencesRepoImpl
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
            install(SSE)

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HttpClient", message)
                    }
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(ResponseObserver) {
                onResponse {
                    Log.i("Response", it.status.value.toString())
                }
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }

    @Provides
    @Singleton
    fun providesIrrigationRepo(httpClient: HttpClient, @ApplicationContext context: Context): IrrigationRepository {
        return IrrigationRepoImpl(httpClient)
    }

    @Provides
    @Singleton
    fun providesPreferencesRepo(@ApplicationContext context: Context) : PreferencesRepository {
        return PreferencesRepoImpl(DatastoreManager(context))
    }

}