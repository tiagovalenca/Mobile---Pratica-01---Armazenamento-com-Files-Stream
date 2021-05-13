package com.example.armazenamentofilesstream

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class FileDetails : AppCompatActivity() {
    var content: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_details)
        val factory = ViewModelFactory()
        val viewModel = ViewModelProvider(this, factory).get(ViewModelFile::class.java)

        val i = intent
        val fileName = i.extras!!.getString("fileName")
        val dir = i.extras!!.get("dir")

        if(!isExternalStorageReadable()){
            Toast.makeText(this, "Não é possível ler do diretório externo", Toast.LENGTH_SHORT).show()
        }

        var file : File = when(dir){
            filesDir -> File(filesDir, fileName)
            else -> File(getExternalFilesDir("Armazenamento"), fileName)
        }

        var fileContent = ""
        try {
            fileContent = readEncrypted(file)
        } catch (e: IOException) {
            val inputStream = FileInputStream(file)
            fileContent = inputStream.bufferedReader().use { it.readText() }
        }

        setSupportActionBar(findViewById(R.id.toolbarDetails))

        supportActionBar?.apply {
            title = file.nameWithoutExtension

            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        //REFERENCE VIEWS FROM XML
        content = findViewById<View>(R.id.contentDetails) as TextView

        //ASSIGN DATA TO THOSE VIEWS
        content!!.text = fileContent
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun readEncrypted(file: File): String {
        val context : Context = applicationContext
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).
        setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        var result = ""

        encryptedFile.openFileInput().use { reader ->
            result = reader.bufferedReader().use { it.readText() }
        }

        return result
    }
}