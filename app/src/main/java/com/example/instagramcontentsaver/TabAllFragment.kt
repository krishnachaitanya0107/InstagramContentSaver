package com.example.instagramcontentsaver

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.instagramcontentsaver.databinding.FragmentTabAllBinding
import com.google.gson.GsonBuilder
import org.apache.commons.lang3.StringUtils

class TabAllFragment : Fragment() {

    private lateinit var binding: FragmentTabAllBinding

    private var contentUrl=""
    private lateinit var mContext: Context
    private lateinit var uri: Uri
    private var searching=true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentTabAllBinding.inflate(layoutInflater, container, false)


        mContext=requireContext()


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
                downReq.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    System.currentTimeMillis().toString()+".jpg"
                )

                val manager: DownloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                manager.enqueue(downReq)

                Toast.makeText(mContext,"Downloaded content !!",Toast.LENGTH_SHORT).show()


            } else
            {
                Toast.makeText(mContext,"Could not download content ",Toast.LENGTH_SHORT).show()
            }
        }

        binding.reset.setOnClickListener {

            binding.linkTextView.setText("")
            binding.imageView.setImageResource(R.drawable.ic_image)

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

        val stringRequest= StringRequest(
            Request.Method.GET,
            url,
            {response->
                val gsonBuilder= GsonBuilder()
                val gson=gsonBuilder.create()
                val instaResponse=gson.fromJson(response,InstaResponse::class.java)
                contentUrl=instaResponse.graphql.shortcode_media.display_url
                uri= Uri.parse(contentUrl)

                Glide.with(mContext).load(uri).into(binding.imageView)

            },
            {error->
                Toast.makeText(mContext,"Could not load content ",Toast.LENGTH_SHORT).show()
                Log.d("error",error.toString())
            })

        queue.add(stringRequest)

    }

}