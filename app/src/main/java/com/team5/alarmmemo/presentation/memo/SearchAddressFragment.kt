package com.team5.alarmmemo.presentation.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.FragmentSearchAddressBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchAddressFragment : DialogFragment() {

    private val addressViewModel:SearchAddressViewModel by activityViewModels()

    private var _binding : FragmentSearchAddressBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        setStyle(STYLE_NO_TITLE,R.style.AlertDialog_FullScreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchAddressBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.searchWv){
            settings.javaScriptEnabled = true
            addJavascriptInterface(BridgeInterface(), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    loadUrl("javascript:sample2_execDaumPostcode();")

                }
            }
            loadUrl("https://alarmmemo-42751.web.app/")
        }

    }

    private inner class BridgeInterface{
        @JavascriptInterface
        fun processDATA(data:String){
            addressViewModel.searchByAddress(data)
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchAddressFragment()
    }
}