package ru.home.swap.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.home.swap.App
import ru.home.swap.model.Person
import ru.home.swap.model.PersonProfile
import java.util.*

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Cache(val context: Context) {
    val PROFILE_CONTACT_KEY = stringPreferencesKey("PROFILE_CONTACT_KEY")
    val PROFILE_SECRET_KEY = stringPreferencesKey("PROFILE_SECRET_KEY")
    val PROFILE_NAME_KEY = stringPreferencesKey("PROFILE_NAME_KEY")

    suspend fun saveProfile(person: PersonProfile) {
        Log.d(App.TAG, "[cache] save profile call")
        context.dataStore.edit { settings ->
            Log.d(App.TAG, "[cache] Profile is saved")
            settings[PROFILE_CONTACT_KEY] = person.contact
            settings[PROFILE_SECRET_KEY] = person.secret
            settings[PROFILE_NAME_KEY] = person.person.name
        }
    }

    fun getProfile(): Flow<PersonProfile> {
        return context.dataStore.data
            .map { preferences ->
                Log.d(App.TAG, "[cache] get profile from cache")
                PersonProfile(
                    preferences[PROFILE_CONTACT_KEY] ?: "",
                    preferences[PROFILE_SECRET_KEY] ?: "",
                    Person(
                        preferences[PROFILE_NAME_KEY] ?: "",
                        Collections.emptyList(),
                        Collections.emptyList()
                    )
                )
            }
    }
}