package com.example.armazenamentofilesstream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ViewModelFile::class.java)){
            return ViewModelFile() as T
        }
        throw IllegalArgumentException ("UnknownViewModel")
    }

}