package com.sranker.shoppinglistmanager.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sranker.shoppinglistmanager.data.repository.Language

object LocaleHelper {
    fun setLocale(language: Language) {
        val localeCode = when (language) {
            Language.ENGLISH -> "en"
            Language.HUNGARIAN -> "hu"
            Language.GERMAN -> "de"
        }
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
