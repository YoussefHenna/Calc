package com.gooofystudios.calc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.history_item.view.*

class HistoryAdapter(private val list: MutableList<HistoryItem>,private val context: Context): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val v: View): RecyclerView.ViewHolder(v)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item,parent,false))
    }


    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val v = holder.v
        val item = list[position]
        v.answer.text = item.answer
        v.expression.text = item.exp
    }

    override fun getItemCount(): Int {
        return list.size
    }

}