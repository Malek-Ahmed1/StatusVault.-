package com.kaboas.statusvault

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val path = intent.getStringExtra("file_path")
        if (path == null) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val videoPreview = findViewById<VideoView>(R.id.videoPreview)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        val isVideo = file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "avi")

        if (isVideo) {
            imgPreview.visibility = ImageView.GONE
            videoPreview.visibility = VideoView.VISIBLE

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoPreview)
            videoPreview.setMediaController(mediaController)
            videoPreview.setVideoURI(Uri.fromFile(file))
            videoPreview.setOnPreparedListener { it.isLooping = true }
            videoPreview.start()
        } else {
            videoPreview.visibility = VideoView.GONE
            imgPreview.visibility = ImageView.VISIBLE
            Glide.with(this).load(file).into(imgPreview)
        }

        btnClose.setOnClickListener { finish() }
    }
}
