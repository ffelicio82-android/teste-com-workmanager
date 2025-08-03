package br.com.nukes.testeworkmanager.di

import br.com.nukes.testeworkmanager.data.remote.SyncApi
import br.com.nukes.testeworkmanager.data.remote.SyncRemoteDataSource
import br.com.nukes.testeworkmanager.data.remote.SyncRemoteDataSourceImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single<Moshi> {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://68842230745306380a379f4b.mockapi.io/api/")
            .client(get<OkHttpClient>())
            .addConverterFactory(MoshiConverterFactory.create(get<Moshi>()))
            .build()
    }

    single<SyncApi> { get<Retrofit>().create(SyncApi::class.java) }
    single<SyncRemoteDataSource> { SyncRemoteDataSourceImpl(get()) }
}