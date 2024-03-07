package com.example.d_id_videostream.commons.di

import com.example.d_id_videostream.commons.Constants
import com.example.d_id_videostream.commons.network.IDNetwork
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ApplicationBuilder {

    @Provides
    @Singleton
    fun getRetrofitBuilder() = Retrofit.Builder()
        .baseUrl(IDNetwork.BaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun getIdNetwork(retrofit: Retrofit) = retrofit.create(IDNetwork::class.java)
}