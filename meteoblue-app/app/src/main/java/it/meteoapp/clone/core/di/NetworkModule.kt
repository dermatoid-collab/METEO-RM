package it.meteoapp.clone.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.meteoapp.clone.BuildConfig
import it.meteoapp.clone.data.api.GoogleWeatherApi
import it.meteoapp.clone.data.api.MeteoBlueApi
import it.meteoapp.clone.data.api.NominatimApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("meteoblue")
    fun provideMeteoBlueRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.METEOBLUE_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("nominatim")
    fun provideNominatimRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(
                client.newBuilder()
                    .addInterceptor { chain ->
                        // Nominatim richiede User-Agent
                        val req = chain.request().newBuilder()
                            .header("User-Agent", "MeteoAppClone/1.0")
                            .build()
                        chain.proceed(req)
                    }
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMeteoBlueApi(@Named("meteoblue") retrofit: Retrofit): MeteoBlueApi =
        retrofit.create(MeteoBlueApi::class.java)

    @Provides
    @Singleton
    fun provideNominatimApi(@Named("nominatim") retrofit: Retrofit): NominatimApi =
        retrofit.create(NominatimApi::class.java)

    @Provides
    @Singleton
    @Named("google_weather")
    fun provideGoogleWeatherRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://weather.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGoogleWeatherApi(@Named("google_weather") retrofit: Retrofit): GoogleWeatherApi =
        retrofit.create(GoogleWeatherApi::class.java)
}
