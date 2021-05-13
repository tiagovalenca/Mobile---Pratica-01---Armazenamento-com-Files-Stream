package com.example.armazenamentofilesstream

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.example.armazenamentofilesstream.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: ViewModelFile
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedFile: File
    private val SECOND_ACTIVITY_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar).apply {
            title = "Exercício Arm. Interno e Externo"
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        layoutManager = LinearLayoutManager(applicationContext)

        recyclerView = findViewById(R.id.recyclerView)
        val factory = ViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(ViewModelFile::class.java)

        if(viewModel.firstRun){
            viewModel.filesDir = filesDir
            viewModel.getList()
            viewModel.firstRun = false
        }


        initialiseAdapter()
    }

    private fun initialiseAdapter(){
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        observeData()
    }

    fun observeData(){
        viewModel.fileList.observe(this, Observer {
            recyclerView.adapter = Adapter(viewModel, it, this) { file: File ->
                adapterOnClick(
                    file
                )
            }
            recyclerView.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
        })
    }

    private fun adapterOnClick(file: File) {
        val intent = Intent(this, FileDetails()::class.java)
        intent.putExtra("fileName", file.name)
        intent.putExtra("dir", viewModel.filesDir)
        this.startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun filterList(view: View) {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val selectedOption = radioGroup!!.checkedRadioButtonId

        when(selectedOption){
            R.id.radioInternal -> {
                viewModel.filesDir = filesDir
                viewModel.getList()
            }
            R.id.radioExternal -> {
                viewModel.filesDir = getExternalFilesDir("Armazenamento")!!
                viewModel.getList()
            }
        }

        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun buildEncrypted(file: File, content : String) {
        val context : Context = applicationContext
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).
                                setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { writer ->
            writer.write(content.toByteArray())
        }
    }

    fun buttonClickSave(view: View) {
        val fileName = findViewById<EditText>(R.id.fileNameAdd)
        val stringName = fileName.text.toString() + ".txt"

        val content = findViewById<EditText>(R.id.contentAdd)
        val stringContent = content.text.toString()

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val check = findViewById<CheckBox>(R.id.checkBoxJetPack)
        var selectedOption = -1
        selectedOption = radioGroup!!.checkedRadioButtonId

        when(selectedOption){
            R.id.radioInternal -> {
                if(!check.isChecked) {
                    openFileOutput(stringName, Context.MODE_PRIVATE).use {
                        it.write(stringContent.toByteArray())
                    }
                } else {
                    val file = File(filesDir, stringName)
                    if(file.exists()){
                        file.delete()
                    }
                    buildEncrypted(file, stringContent)
                }
                fileName.text.clear()
                content.text.clear()
                viewModel.filesDir = filesDir
                viewModel.getList()
                recyclerView.adapter?.notifyDataSetChanged()
            }
            R.id.radioExternal -> {
                if(isExternalStorageWritable()){
                    if(!check.isChecked) {
                        val extFile = File(getExternalFilesDir("Armazenamento"), stringName)
                        val file = FileOutputStream(extFile).use { stream ->
                            stream.write(stringContent.toByteArray())
                        }
                    } else {
                        val file = File(getExternalFilesDir("Armazenamento"), stringName)
                        if(file.exists()){
                            file.delete()
                        }
                        buildEncrypted(file, stringContent)
                    }
                    fileName.text.clear()
                    content.text.clear()
                    viewModel.filesDir = getExternalFilesDir("Armazenamento")!!
                    viewModel.getList()
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                else{
                    Toast.makeText(this, "O diretório externo não tem permissão para escrever", Toast.LENGTH_SHORT).show()
                }

            }
            else -> {
                Toast.makeText(this, "Escolha Onde Salvar o Arquivo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}