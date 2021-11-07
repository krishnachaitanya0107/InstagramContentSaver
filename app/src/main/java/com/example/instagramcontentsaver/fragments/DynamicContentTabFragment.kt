package com.example.instagramcontentsaver.fragments

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.instagramcontentsaver.R
import com.example.instagramcontentsaver.activities.MainActivity
import com.example.instagramcontentsaver.database.InstaResponse
import com.example.instagramcontentsaver.database.SharedPrefDb
import com.example.instagramcontentsaver.databinding.FragmentDynamicTabBinding
import com.example.instagramcontentsaver.utilities.Constants
import com.google.gson.GsonBuilder
import org.apache.commons.lang3.StringUtils
import java.io.File

class DynamicContentTabFragment : Fragment() {

    companion object {

        private const val CATEGORY = "category"
        fun getInstance(category: String) = DynamicContentTabFragment().apply {
            arguments = bundleOf(CATEGORY to category)
        }

    }

    private lateinit var binding: FragmentDynamicTabBinding
    private lateinit var reqCategory: String

    private var contentUrl = ""
    private var isVideo = false
    private var searching = true
    private lateinit var mContext: Context
    private lateinit var mediaController: MediaController
    private lateinit var uri: Uri
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDynamicTabBinding.inflate(layoutInflater, container, false)

        mContext = requireContext()
        reqCategory = requireArguments().getString("category") ?: ""


        mediaController = MediaController(mContext)

        mediaController.apply {
            setAnchorView(binding.videoView)
        }

        binding.apply {
            getContent.setOnClickListener {
                search()
            }
            download.setOnClickListener {
                when {
                    contentUrl.isNotEmpty() -> {
                        dialog = showFileNameDialog()

                    }
                    binding.linkTextView.text.toString().isNullOrEmpty() -> {
                        Toast.makeText(mContext, "Please enter content link", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        Toast.makeText(mContext, "Could not download content ", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            reset.setOnClickListener {
                reset()
            }
        }


        return binding.root
    }

    private fun search() {

        if (searching) {

            binding.progressBar.visibility = View.VISIBLE
            val tempLink = binding.linkTextView.text.toString()

            if (tempLink.isEmpty()) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(mContext, "Link Cannot be empty ", Toast.LENGTH_SHORT).show()
            } else {

                searching = false
                binding.getContent.setImageResource(R.drawable.ic_close)

                var url2 = StringUtils.substringBefore(tempLink, "/?")
                url2 += "/?__a=1"

                processData(url2)

                hideKeyboard()

            }
        } else {
            binding.linkTextView.setText("")
            searching = true
            binding.getContent.setImageResource(R.drawable.ic_search)
        }

    }

    private fun reset() {

        searching = true
        contentUrl = ""
        isVideo = false

        binding.apply {

            linkTextView.setText("")
            getContent.setImageResource(R.drawable.ic_search)
            videoViewPlaceHolder.visibility = View.VISIBLE
            videoViewPlaceHolderBorder.visibility = View.VISIBLE

            try {
                videoView.stopPlayback()
                videoView.visibility = View.INVISIBLE
            } catch (e: Exception) {
                Log.d("exception", "Couldn't stop video player")
            }
        }

    }

    private fun showFileNameDialog(): Dialog {
        return Dialog(mContext).apply {

            setContentView(R.layout.file_name_dialog)
            window?.setDimAmount(Constants.DIM_AMOUNT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)

            val fileNameTextView = findViewById<EditText>(R.id.nameTextView)

            findViewById<TextView>(R.id.fileExtension).text = ".mp4"

            findViewById<TextView>(R.id.confirmButton).setOnClickListener {
                val fileName = fileNameTextView.text.toString()
                hideKeyboard()
                if (checkStoragePermission()) {
                    if (getAllFileNames().contains(fileName)) {
                        Toast.makeText(
                            mContext,
                            "File Already Exists , Try a different name",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        downloadResource(fileName)
                        dismiss()
                    }
                } else {
                    requestPermission()
                }
            }

            findViewById<TextView>(R.id.cancelButton).setOnClickListener {
                hideKeyboard()
                dismiss()
            }
            show()
        }

    }

    fun showDifferentContentDialog(link: String) {

        Dialog(mContext).apply {

            setContentView(R.layout.different_content_dialog)
            window?.setDimAmount(0.40F)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(true)

            findViewById<TextView>(R.id.confirmButton).setOnClickListener {

                (activity as MainActivity).apply {
                    pagerAdapter.setImageDataToFragment(link)
                    binding.viewPager2.setCurrentItem(0, true)
                }

                reset()
                dismiss()
            }

            findViewById<TextView>(R.id.cancelButton).setOnClickListener {
                dismiss()
            }

        }.show()

    }

    fun setData(link: String) {
        binding.linkTextView.setText(link)
        search()
    }

    fun getAllFileNames(): ArrayList<String> {
        val sharedPrefDb = SharedPrefDb(mContext)
        val fileNameList = ArrayList<String>()
        val tempFileNameList = sharedPrefDb.getListString(Constants.File_NAMES_LIST)

        if (tempFileNameList.size == 0) {
            var folder = File(
                mContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath +
                        Constants.FILE_PATH
            )

            val filesInFolder = folder.listFiles()

            if (filesInFolder != null) {
                for (file in filesInFolder) {
                    fileNameList.add((file.nameWithoutExtension))
                }
            }
            Log.d("fileNames", fileNameList.toString())
            sharedPrefDb.putListString(Constants.File_NAMES_LIST, fileNameList)

            return fileNameList
        } else {
            return tempFileNameList
        }


    }

    private fun downloadResource(fileName: String?) {

        val downReq: DownloadManager.Request = DownloadManager.Request(uri)

        val tempFileName: String = if (fileName.isNullOrEmpty()) {
            System.currentTimeMillis().toString()
        } else {
            fileName
        }


        var filePath =
            mContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + Constants.FILE_PATH
        var file = File(filePath)

        /*
        var filePath=mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath+"/$tempFileName.mp4"

        var file=File(filePath)

        var finalFileName=tempFileName
        var i=1

        while(file.exists()){
            finalFileName="$tempFileName-$i"
            filePath=mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath+"/${finalFileName}.mp4"
            file=File(filePath)
            i++
        }

        Log.d("testing",finalFileName)

        downReq.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                "$finalFileName.mp4"
        )

        */

        downReq.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
        )
        downReq.setTitle("Download")
        downReq.setDescription("$tempFileName.mp4")
        downReq.allowScanningByMediaScanner()
        downReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)


        downReq.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DCIM,
            "/InstaContentSaver/$tempFileName.mp4"
        )

        val manager: DownloadManager =
            activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        manager.enqueue(downReq)

        Toast.makeText(mContext, "Downloaded content !!", Toast.LENGTH_SHORT).show()

        /*
        if(finalFileName.equals(tempFileName)){
            Toast.makeText(mContext,"Downloaded content !!",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(mContext,"File already exists , renamed to $finalFileName.mp4 and saved ",Toast.LENGTH_SHORT).show()
        }
        */
        var fileUri = file.absolutePath.toUri()

        Log.d("testing2", fileUri.toString())

    }


    fun requestPermission() {
        val requestCodeAskPermissions = 123
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
            ),
            requestCodeAskPermissions
        )
    }

    fun checkStoragePermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            "android.permission.READ_EXTERNAL_STORAGE"
        ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
            requireContext(),
            "android.permission.WRITE_EXTERNAL_STORAGE"
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val fileName = dialog.findViewById<EditText>(R.id.nameTextView).text.toString()
            if (getAllFileNames().contains(fileName)) {
                Toast.makeText(
                    mContext,
                    "File Already Exists , Try a different name",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                downloadResource(fileName)
                dialog.dismiss()
            }
        } else {
            Log.d("testing2", grantResults.toString())
        }

    }


    private fun hideKeyboard() {
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .apply {
                hideSoftInputFromWindow(binding.root.windowToken, 0)
            }
    }

    private fun processData(url: String) {

        val queue = Volley.newRequestQueue(mContext)

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val gsonBuilder = GsonBuilder()
                val gson = gsonBuilder.create()
                val instaResponse = gson.fromJson(response, InstaResponse::class.java)
                contentUrl = instaResponse.graphql.shortcode_media.video_url
                isVideo = instaResponse.graphql.shortcode_media.is_video

                binding.progressBar.visibility = View.GONE

                if (isVideo) {

                    if (contentUrl.isNullOrEmpty()) {

                        binding.videoViewPlaceHolder.visibility = View.VISIBLE
                        binding.videoViewPlaceHolderBorder.visibility = View.VISIBLE
                        Toast.makeText(mContext, "Could not load content ", Toast.LENGTH_SHORT)
                            .show()

                    } else {

                        uri = Uri.parse(contentUrl)

                        binding.videoViewPlaceHolder.visibility = View.GONE
                        binding.videoViewPlaceHolderBorder.visibility = View.GONE
                        binding.videoView.visibility = View.VISIBLE

                        binding.videoView.setMediaController(mediaController)
                        binding.videoView.setVideoURI(uri)


                        binding.videoView.start()

                    }

                } else {
                    showDifferentContentDialog(binding.linkTextView.text.toString())
                }


            },
            { error ->

                binding.videoViewPlaceHolder.visibility = View.VISIBLE
                binding.videoViewPlaceHolderBorder.visibility = View.VISIBLE
                Toast.makeText(mContext, "Could not load content ", Toast.LENGTH_SHORT).show()
                Log.d("error", error.toString())
            })

        queue.add(stringRequest)

    }

}