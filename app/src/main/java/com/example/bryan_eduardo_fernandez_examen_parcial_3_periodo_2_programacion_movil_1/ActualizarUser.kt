package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Camera_Class.Camera
import com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Camera_Class.Camera_Helper
import com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Date_Class.Class_Date_Picker_Helper
import com.google.firebase.firestore.FirebaseFirestore

class ActualizarUser : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var descripcionEntrevistaEditText: EditText
    private lateinit var nombrePeriodistaEditText: EditText
    private lateinit var asignarFechaTextView: TextView

    private lateinit var eliminarEntrevistasButton: Button
    private lateinit var actualizarEntrevistaButton: Button

    private lateinit var escucharAudioButton: Button

    private lateinit var classCameraHelper: Camera_Helper;
    private lateinit var classCamera: Camera;

    private lateinit var classDatePickerHelper: Class_Date_Picker_Helper;

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actualizar_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        imageView = findViewById(R.id.imageView)
        descripcionEntrevistaEditText = findViewById(R.id.descripcionEntrevistaEditText)
        nombrePeriodistaEditText = findViewById(R.id.nombrePeriodistaEditText)
        asignarFechaTextView = findViewById(R.id.asignarFechaTextView)

        actualizarEntrevistaButton = findViewById(R.id.actualizarEntrevistaButton)
        eliminarEntrevistasButton = findViewById(R.id.eliminarEntrevistasButton)

        escucharAudioButton = findViewById(R.id.escucharAudioButton)

        classCameraHelper = Camera_Helper(this, imageView)
        classCamera = Camera(this)

        classDatePickerHelper = Class_Date_Picker_Helper(this, asignarFechaTextView);

        imageView.setOnClickListener {
            classCameraHelper.tomarFoto()
        }

        asignarFechaTextView.setOnClickListener {
            classDatePickerHelper.datePicker()
        }

        val userId = intent.getStringExtra("userId")

        if (userId != null) {
            loadUserData(userId)
        }

        actualizarEntrevistaButton.setOnClickListener {
            userId?.let { actualizarUsuario(it) }
        }

        eliminarEntrevistasButton.setOnClickListener {
            userId?.let { eliminarUsuario(it) }
        }

        escucharAudioButton.setOnClickListener {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val audioUrl = document.getString("audioUrl")

                        audioUrl?.let { url ->
                            if (isPlaying) {
                                mediaPlayer?.pause()

                                isPlaying = false

                                escucharAudioButton.text = "Escuchar Audio"
                            } else {
                                if (mediaPlayer == null) {
                                    mediaPlayer = MediaPlayer().apply {
                                        setAudioStreamType(AudioManager.STREAM_MUSIC)

                                        setDataSource(url)

                                        prepare()

                                        start()
                                    }
                                } else {
                                    mediaPlayer?.start()
                                }
                                isPlaying = true

                                escucharAudioButton.text = "Pausar Audio"
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Audio no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val user = document.toObject(User::class.java)

                user?.let {
                    descripcionEntrevistaEditText.setText(it.descripcionEntrevista)
                    nombrePeriodistaEditText.setText(it.nombrePeriodista)

                    asignarFechaTextView.text = it.asignarFecha

                    Glide.with(this).load(it.imageUrl).into(imageView)
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this, "Hubo un error trayendo los datos de la entrevista", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun eliminarUsuario(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Documento eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error al eliminar el documento. ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun actualizarUsuario(userId: String) {
        val db = FirebaseFirestore.getInstance()

        classCamera.subirImageFirebaseStorage({ imageUrl ->
            val updatedUser = mapOf(
                "descripcionEntrevista" to descripcionEntrevistaEditText.text.toString(),
                "nombrePeriodista" to nombrePeriodistaEditText.text.toString(),
                "asignarFecha" to asignarFechaTextView.text.toString(),
                "imageUrl" to imageUrl
            )

            db.collection("users").document(userId).update(updatedUser).addOnSuccessListener {
                Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this, "Error al actualizar ${exception.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }, { exception ->
            Toast.makeText(
                this, "Error al subir la imagen. ${exception.message}", Toast.LENGTH_SHORT
            ).show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        classCamera.onRequestPermissionsResult(requestCode, grantResults, {
            val takePictureIntent = classCamera.dispatchTakePictureIntent();

            if (takePictureIntent != null) {
                classCameraHelper.tomarFotoVal.launch(takePictureIntent);
            }
        }, {
            Toast.makeText(this, "Acceso Denegado!", Toast.LENGTH_LONG).show();
        });
    }
}