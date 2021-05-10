package com.example.armazenamentofilesstream

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FileDetails : AppCompatActivity() {
    var content: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_details)

        val i = intent
        val fileName = i.extras!!.getString("fileName")

        var file = File(filesDir, fileName)
        if(!file.exists()) {
            if(isExternalStorageReadable()){
                file = File(getExternalFilesDir("Armazenamento"), fileName)
            }
            else{
                Toast.makeText(this, "Não é possível ler do diretório externo", Toast.LENGTH_SHORT).show()
            }
        }

        val fileContentStream = file.inputStream()
        val fileContent = fileContentStream.bufferedReader().use { it.readText() }
        setSupportActionBar(findViewById(R.id.toolbarDetails))

        supportActionBar?.apply {
            title = file.nameWithoutExtension

            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        //REFERENCE VIEWS FROM XML
        content = findViewById<View>(R.id.contentDetails) as TextView

        //ASSIGN DATA TO THOSE VIEWS
        content!!.setText("${fileContent}")
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

}