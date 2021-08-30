package com.example.instagramcontentsaver

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.MediaController
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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
    private lateinit var reqCategory:String

    private var contentUrl=""
    private var searching=true
    private lateinit var mContext: Context
    private lateinit var mediaController:MediaController
    private lateinit var uri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding=FragmentDynamicTabBinding.inflate(layoutInflater, container, false)

        mContext=requireContext()
        reqCategory= requireArguments().getString("category") ?: ""


        mediaController= MediaController(mContext)

        mediaController.apply {
            setAnchorView(binding.videoView)
        }

        binding.getContent.setOnClickListener {
            if(searching){

                val tempLink=binding.linkTextView.text.toString()

                if(tempLink.isEmpty()){
                    Toast.makeText(mContext,"Link Cannot be empty ",Toast.LENGTH_SHORT).show()
                } else {

                    searching=false
                    binding.getContent.setImageResource(R.drawable.ic_close)

                    var url2=StringUtils.substringBefore(tempLink,"/?")
                    url2 += "/?__a=1"

                    processData(url2)

                    hideKeyboard()

                }
            } else {
                binding.linkTextView.setText("")
                searching=true
                binding.getContent.setImageResource(R.drawable.ic_search)
            }

        }

        binding.download.setOnClickListener {
            if(contentUrl.isNotEmpty()){

                val downReq : DownloadManager.Request= DownloadManager.Request(uri)

                downReq.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                downReq.setTitle("Download")
                downReq.setDescription("....")
                downReq.allowScanningByMediaScanner()
                downReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                downReq.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    System.currentTimeMillis().toString()+".mp4"
                )

                val manager:DownloadManager= activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                manager.enqueue(downReq)

                Toast.makeText(mContext,"Downloaded content !!",Toast.LENGTH_SHORT).show()


            } else
            {
                Toast.makeText(mContext,"Could not download content ",Toast.LENGTH_SHORT).show()
            }
        }

        binding.reset.setOnClickListener {

            binding.linkTextView.setText("")

            binding.videoViewPlaceHolder.visibility=View.VISIBLE
            binding.videoViewPlaceHolderBorder.visibility=View.VISIBLE

            try{
                binding.videoView.stopPlayback()
                binding.videoView.visibility=View.INVISIBLE
            } catch (e:Exception){
                Log.d("exception","Couldn't stop video player")
            }

        }


        return binding.root
    }

    private fun hideKeyboard(){
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .apply {
            hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    private fun processData(url:String){

        val queue = Volley.newRequestQueue(mContext)

        val stringRequest=StringRequest(
            Request.Method.GET,
            url,
            {response->
                val gsonBuilder=GsonBuilder()
                val gson=gsonBuilder.create()
                val instaResponse=gson.fromJson(response,InstaResponse::class.java)
                contentUrl=instaResponse.graphql.shortcode_media.video_url
                uri= Uri.parse(contentUrl)

                binding.videoViewPlaceHolder.visibility=View.GONE
                binding.videoViewPlaceHolderBorder.visibility=View.GONE
                binding.videoView.visibility=View.VISIBLE

                binding.videoView.setMediaController(mediaController)
                binding.videoView.setVideoURI(uri)
                binding.videoView.start()
                             },
            {error->

                binding.videoViewPlaceHolder.visibility=View.VISIBLE
                binding.videoViewPlaceHolderBorder.visibility=View.VISIBLE
                Toast.makeText(mContext,"Could not load content ",Toast.LENGTH_SHORT).show()
                Log.d("error",error.toString())
            })

        queue.add(stringRequest)

    }

}