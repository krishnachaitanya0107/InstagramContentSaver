package com.example.instagramcontentsaver.fragments

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.instagramcontentsaver.R
import com.example.instagramcontentsaver.activities.MainActivity
import com.example.instagramcontentsaver.database.InstaResponse
import com.example.instagramcontentsaver.databinding.FragmentDynamicTabBinding
import com.google.gson.GsonBuilder
import org.apache.commons.lang3.StringUtils

class DynamicTabFragment : Fragment() {

    companion object {

        private const val CATEGORY = "category"
        fun getInstance(category: String) = DynamicTabFragment().apply {
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

        binding.getContent.setOnClickListener {
            search()
        }

        binding.download.setOnClickListener {
            when {
                contentUrl.isNotEmpty() -> {
                    showFileNameDialog()

                }
                binding.linkTextView.text.toString().isNullOrEmpty() -> {
                    Toast.makeText(mContext, "Please enter content link", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(mContext, "Could not download content ", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.reset.setOnClickListener {
            reset()
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

        binding.linkTextView.setText("")
        searching = true
        binding.getContent.setImageResource(R.drawable.ic_search)

        contentUrl = ""
        isVideo = false


        binding.videoViewPlaceHolder.visibility = View.VISIBLE
        binding.videoViewPlaceHolderBorder.visibility = View.VISIBLE

        try {
            binding.videoView.stopPlayback()
            binding.videoView.visibility = View.INVISIBLE
        } catch (e: Exception) {
            Log.d("exception", "Couldn't stop video player")
        }

    }

    private fun showFileNameDialog() {
        Dialog(mContext).apply {

            setContentView(R.layout.file_name_dialog)
            window?.setDimAmount(0.40F)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)

            val fileNameTextView = findViewById<EditText>(R.id.nameTextView)

            findViewById<TextView>(R.id.confirmButton).setOnClickListener {
                val fileName = fileNameTextView.text.toString()
                downloadResource(fileName)
                hideKeyboard()
                dismiss()
            }

            findViewById<TextView>(R.id.cancelButton).setOnClickListener {
                hideKeyboard()
                dismiss()
            }

        }.show()
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

    private fun downloadResource(fileName: String?) {

        val downReq: DownloadManager.Request = DownloadManager.Request(uri)

        val tempFileName: String = if (fileName.isNullOrEmpty()) {
            System.currentTimeMillis().toString()
        } else {
            fileName
        }

        downReq.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
        )
        downReq.setTitle("Download")
        downReq.setDescription("$tempFileName.mp4")
        downReq.allowScanningByMediaScanner()
        downReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)


        downReq.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$tempFileName.mp4"
        )

        val manager: DownloadManager =
            activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        manager.enqueue(downReq)

        Toast.makeText(mContext, "Downloaded content !!", Toast.LENGTH_SHORT).show()

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