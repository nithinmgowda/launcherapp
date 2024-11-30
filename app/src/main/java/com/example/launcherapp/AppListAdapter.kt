package com.example.launcherapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(
    val name: String,
    val packageName: String,
    val launchIntent: Intent?
)

class AppListAdapter(
    private var apps: MutableList<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private var onLongClickListener: ((AppInfo) -> Boolean)? = null

    fun updateApps(newApps: List<AppInfo>) {
        apps.clear()
        apps.addAll(newApps)
        notifyDataSetChanged()
    }

    fun getPositionForChar(char: Char): Int {
        return apps.indexOfFirst { 
            it.name.firstOrNull()?.uppercaseChar() == char.uppercaseChar() ||
            (char.isDigit() && it.name.firstOrNull()?.isDigit() == true)
        }
    }

    fun setOnLongClickListener(listener: (AppInfo) -> Boolean) {
        onLongClickListener = listener
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)

        init {
            itemView.setOnClickListener {
                onAppClick(apps[adapterPosition])
            }
            itemView.setOnLongClickListener {
                onLongClickListener?.invoke(apps[adapterPosition]) ?: false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.appNameTextView.text = apps[position].name
    }

    override fun getItemCount(): Int = apps.size
} 