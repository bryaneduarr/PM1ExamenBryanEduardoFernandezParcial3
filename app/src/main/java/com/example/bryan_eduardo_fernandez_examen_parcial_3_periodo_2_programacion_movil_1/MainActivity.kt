package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Camera_Class.Camera
import com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Camera_Class.Camera_Helper
import com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Date_Class.Class_Date_Picker_Helper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class MainActivity : AppCompatActivity() {
    // Image View
    private lateinit var imageView: ImageView

    // Buttons
    private lateinit var grabarAudioButton: Button
    private lateinit var asignarFechaButton: Button
    private lateinit var guardarEntrevistaButton: Button
    private lateinit var verEntrevistasButton: Button

    // Text View
    private lateinit var asignarFechaTextView: TextView

    // Edit Text
    private lateinit var descripcionEntrevistaEditText: EditText
    private lateinit var nombrePeriodistaEditText: EditText

    // Date Class
    private lateinit var classDatePickerHelper: Class_Date_Picker_Helper;

    // Camera Classes
    private lateinit var classCameraHelper: Camera_Helper;
    private lateinit var classCamera: Camera;

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Image View
        imageView = findViewById(R.id.imageView)

        // Buttons
        grabarAudioButton = findViewById(R.id.grabarAudioButton)
        asignarFechaButton = findViewById(R.id.asignarFechaButton)
        guardarEntrevistaButton = findViewById(R.id.guardarEntrevistaButton)
        verEntrevistasButton = findViewById(R.id.verEntrevistasButton)

        // Text View
        asignarFechaTextView = findViewById(R.id.asignarFechaTextView)

        // Edit Text
        descripcionEntrevistaEditText = findViewById(R.id.descripcionEntrevistaEditText)
        nombrePeriodistaEditText = findViewById(R.id.nombrePeriodistaEditText)

        // Date Class
        classDatePickerHelper = Class_Date_Picker_Helper(this, asignarFechaTextView);

        // Camera Classes
        classCameraHelper = Camera_Helper(this, imageView)
        classCamera = Camera(this)

        // Handle imageView Click
        imageView.setOnClickListener {
            classCameraHelper.tomarFoto()
        }

        // Handle Date Click
        asignarFechaTextView.setOnClickListener {
            classDatePickerHelper.datePicker()
        }

        asignarFechaButton.setOnClickListener {
            classDatePickerHelper.datePicker()
        }

        // Handle Record Audio Click
        grabarAudioButton.setOnClickListener {
            requestMicrophonePermission()
        }

        // Handle View Entrevistas Click
        verEntrevistasButton.setOnClickListener {
            val intent = Intent(this, UserListActivity::class.java)
            startActivity(intent)
        }

        // Handle Save Click
        guardarEntrevistaButton.setOnClickListener {
            val descripcionEntrevista = descripcionEntrevistaEditText.text.toString()
            val nombrePeriodista = nombrePeriodistaEditText.text.toString()
            val asignarFecha = asignarFechaTextView.text.toString()

            classCamera.subirImageFirebaseStorage(onSuccess = { imageUrl ->
                subirAudioFirebase(audioFilePath = "${externalCacheDir?.absolutePath}/audioFile.3gp",
                    onSuccess = { audioUrl ->
                        guardarEntrevista(
                            imageUrl = imageUrl,
                            audioUrl = audioUrl,
                            descripcionEntrevista = descripcionEntrevista,
                            nombrePeriodista = nombrePeriodista,
                            asignarFecha = asignarFecha
                        )
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            this,
                            "Error al subir el audio: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            }, onFailure = { exception ->
                Toast.makeText(
                    this, "Error al subir la imagen: ${exception.message}", Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    fun guardarEntrevista(
        imageUrl: String,
        audioUrl: String,
        descripcionEntrevista: String,
        nombrePeriodista: String,
        asignarFecha: String
    ) {
        val db = FirebaseFirestore.getInstance();

        val entrevistaData = hashMapOf(
            "imageUrl" to imageUrl,
            "audioUrl" to audioUrl,
            "descripcionEntrevista" to descripcionEntrevista,
            "nombrePeriodista" to nombrePeriodista,
            "asignarFecha" to asignarFecha
        )

        db.collection("users").add(entrevistaData).addOnSuccessListener { documentReference ->
            Toast.makeText(this, "Entrevista guardada exitosamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e("Error", exception.toString())
            Toast.makeText(this, "Error al guardar la entrevista", Toast.LENGTH_SHORT).show()
        }
    }

    fun subirAudioFirebase(
        audioFilePath: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val audioURi = Uri.fromFile(File(audioFilePath))

        val storageReference: StorageReference =
            FirebaseStorage.getInstance().reference.child("Audios/${System.currentTimeMillis()}")

        storageReference.putFile(audioURi).addOnSuccessListener { taskSnapshot ->

            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener { error ->
            onFailure(error)

            Log.e("Error", error.toString())
        }
    }


    private fun requestMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show()
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startRecording()
            } else {
                Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startRecording() {
        audioFilePath = "${externalCacheDir?.absolutePath}/audioFile.3gp"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)

            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            setOutputFile(audioFilePath)

            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)

            try {
                prepare()

                start()

                Toast.makeText(this@MainActivity, "Grabando audio", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }

        grabarAudioButton.setOnClickListener {
            stopRecording()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()

            release()
        }
        mediaRecorder = null

        Toast.makeText(this, "Grabacion realizada", Toast.LENGTH_SHORT).show()
    }

    // Request Permissions Camera
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