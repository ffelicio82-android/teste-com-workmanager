package br.com.nukes.testeworkmanager.data.local.preferences.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.syncDataStore by preferencesDataStore(name = "sync_prefs")