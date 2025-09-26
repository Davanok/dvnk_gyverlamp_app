package com.davanok.firelamp.ui.pages.lampControl

import androidx.lifecycle.ViewModel
import com.davanok.firelamp.data.repositories.FireLampRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LampControlViewModel @Inject constructor(
    private val repository: FireLampRepository
) : ViewModel() {

}