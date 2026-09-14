package it.meteoapp.clone.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.meteoapp.clone.BuildConfig
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Fornisce l'API key MeteoBlue come String iniettabile con @Named("meteoblue").
     * NetworkModule la usa per costruire le chiamate Retrofit.
     */
    @Provides
    @Singleton
    @Named("meteoblue")
    fun provideMeteoBlueApiKey(): String = BuildConfig.METEOBLUE_API_KEY

    @Provides
    @Singleton
    @Named("google")
    fun provideGoogleApiKey(): String = BuildConfig.GOOGLE_WEATHER_API_KEY
}
