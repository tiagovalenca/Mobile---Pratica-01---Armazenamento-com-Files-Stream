package com.example.armazenamentofilesstream

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import java.io.File


internal class Adapter(val viewModel: ViewModelFile, val fileList: ArrayList<File>, val context: Context, val clickListener: (File) -> Unit)  :
    RecyclerView.Adapter<Adapter.MyViewHolder>() {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var fileName: TextView = view.findViewById(R.id.fileName)
        var deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val file = fileList[position]
        holder.fileName.text = file.nameWithoutExtension

        holder.deleteButton.setOnClickListener(View.OnClickListener {
            File(viewModel.filesDir,file.name).delete()
            viewModel.getList()
            notifyDataSetChanged()
        })

        holder?.itemView?.setOnClickListener { clickListener(file) }
    }
    override fun getItemCount(): Int {
        return fileList.size
    }

}