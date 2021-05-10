package com.example.armazenamentofilesstream

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ViewModelFile : ViewModel() {
    var fileList = MutableLiveData<ArrayList<File>>()
    var tempList = ArrayList<File>()
    var firstRun = true
    lateinit var  filesDir: File

    internal fun getFileList(): MutableLiveData<List<File>> {
        if (fileList == null) {
            fileList = MutableLiveData()
        }
        return fileList as MutableLiveData<List<File>>
    }

    internal fun getList(){
        tempList.clear()
        val files = filesDir?.list()

        files?.forEach {
            tempList.add(File(it))
        }

        fileList.value = tempList
    }
}