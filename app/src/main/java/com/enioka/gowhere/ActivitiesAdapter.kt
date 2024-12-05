package com.enioka.gowhere.model;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.enioka.gowhere.R


class ActivitiesAdapter : ListAdapter<String, ActivitiesAdapter.ActivityViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent:ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.activity_item, parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
    }

    inner class ActivityViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(activity: String) {
            val textView: TextView = view.findViewById(R.id.activityTextView)
            textView.text = activity
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}

