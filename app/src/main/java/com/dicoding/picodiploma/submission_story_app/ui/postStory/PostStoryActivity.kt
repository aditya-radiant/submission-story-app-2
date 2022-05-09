package com.dicoding.picodiploma.submission_story_app.ui.postStory

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.submission_story_app.data.helper.Result
import com.dicoding.picodiploma.submission_story_app.R
import com.dicoding.picodiploma.submission_story_app.databinding.ActivityPostStoryBinding
import com.dicoding.picodiploma.submission_story_app.model.LoginModel
import com.dicoding.picodiploma.submission_story_app.ui.Utils
import com.dicoding.picodiploma.submission_story_app.ui.ViewModelFactory
import com.dicoding.picodiploma.submission_story_app.ui.camera.CameraActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostStoryActivity : AppCompatActivity() {
    private lateinit var login: LoginModel
    private var getFile: File? = null
    private var result: Bitmap? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null

    private val binding: ActivityPostStoryBinding by lazy {
        ActivityPostStoryBinding.inflate(layoutInflater)
    }

    private val postViewModel: PostStoryViewModel by viewModels{
        ViewModelFactory.getInstance(this)
    }

    companion object {
        const val EXTRA_DATA = "data"
        const val CAMERA_X_RESULT = 200
        private const val TAG = "PostStoryActivity"

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.camera_not_allowed),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.post_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        login = intent.getParcelableExtra(EXTRA_DATA)!!

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.camBtn.setOnClickListener { startCameraX() }
        binding.galleryBtn.setOnClickListener { startGallery() }
        binding.btnPost.setOnClickListener { uploadImage() }
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLastLocation()
            } else {
                this.location = null
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun startCameraX() {
        launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            result =
                Utils.rotateBitmap(
                    BitmapFactory.decodeFile(getFile?.path),
                    isBackCamera
                )
        }
        binding.imgPost.setImageBitmap(result)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImg: Uri = it.data?.data as Uri
            val myFile = Utils.uriToFile(selectedImg, this@PostStoryActivity)
            getFile = myFile
            binding.imgPost.setImageURI(selectedImg)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        Log.d(TAG, "$it")
        if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getLastLocation()
        } else binding.switchLocation.isChecked = false
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this.location = location
                    Log.d(TAG, "getLastLocation: ${location.latitude}, ${location.longitude}")
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_activate_your_gps),
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.switchLocation.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun uploadImage() {
        if (binding.etCaption.text.toString().isBlank()) {
            binding.etCaption.error = getString(R.string.fill_caption)
        }

        if (getFile != null) {
            val file = Utils.reduceFileImage(getFile as File)
            val textPlainMediaType = "text/plain".toMediaType()
            val description = binding.etCaption.text.toString()
            val desReqBody = description.toRequestBody(textPlainMediaType)
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            var lat: RequestBody? = null
            var lon: RequestBody? = null

            if (location != null) {
                lat =
                    location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                lon =
                    location?.longitude.toString().toRequestBody("text/plain".toMediaType())
            }

            postViewModel.postStory(
                login.token,
                desReqBody,
                imageMultipart,
                lat,
                lon).observe(this){
                if (it != null) {
                    when (it) {
                        is Result.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                this,
                                getString(R.string.upload_succes),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        is Result.Error -> {
                            binding.progressBar.visibility = View.GONE
                            AlertDialog.Builder(this@PostStoryActivity).apply {
                                setTitle(getString(R.string.information))
                                setMessage(getString(R.string.failed) + ", ${it.error}")
                                setPositiveButton(getString(R.string.continue_)) { _, _ ->
                                    binding.progressBar.visibility = View.GONE
                                }
                                create()
                                show()
                        }
                    }
                }
            }
        }
    }
}
}