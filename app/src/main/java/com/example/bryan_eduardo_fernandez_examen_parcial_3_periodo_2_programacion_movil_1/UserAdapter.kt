package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class UserAdapter(private val context: Context, private val userList: List<User>) : BaseAdapter() {
    override fun getCount(): Int {
        return userList.size
    }

    override fun getItem(position: Int): Any {
        return userList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup   ?): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_user, parent, false)

        val user = userList[position]

        val nombrePeriodistaTextView = view.findViewById<TextView>(R.id.nombrePeriodistaTextView)
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        nombrePeriodistaTextView.text = user.nombrePeriodista

        Glide.with(context)
            .load(user.imageUrl)
            .into(imageView)

        view.setOnClickListener {
            val intent = Intent(context, ActualizarUser::class.java)
            intent.putExtra("userId", user.id)
            context.startActivity(intent)
        }

        return view
    }
}