package com.example.biometricbanjo.utils

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val context: Context) {

    fun getString(@StringRes stringRes: Int) = context.getString(stringRes)
}
