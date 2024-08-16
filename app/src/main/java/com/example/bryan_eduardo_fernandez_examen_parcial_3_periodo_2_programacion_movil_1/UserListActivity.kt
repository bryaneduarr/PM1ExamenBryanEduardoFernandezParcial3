package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity() {
    private lateinit var listView: ListView

    private lateinit var userAdapter: UserAdapter

    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listView = findViewById(R.id.listView)

        val db = FirebaseFirestore.getInstance()

        db.collection("users").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject(User::class.java).copy(id = document.id    )

                    userList.add(user)
                }
                userAdapter = UserAdapter(this, userList)

                listView.adapter = userAdapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al traer los datos", Toast.LENGTH_SHORT).show()
            }
    }
}