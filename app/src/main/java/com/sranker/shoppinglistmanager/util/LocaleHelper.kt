package com.sranker.shoppinglistmanager.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sranker.shoppinglistmanager.data.repository.Language

object LocaleHelper {
    fun setLocale(language: Language) {
        val localeCode = languageTag(language)
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun isLocaleApplied(language: Language): Boolean {
        val currentLanguageTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return currentLanguageTags == languageTag(language)
    }

    fun languageTag(language: Language): String = when (language) {
        Language.ENGLISH -> "en"
        Language.HUNGARIAN -> "hu"
        Language.GERMAN -> "de"
    }
}
