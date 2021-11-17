package com.example.fragment.library.common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.example.fragment.library.base.utils.WebHelper
import com.example.fragment.library.common.constant.Keys
import com.example.fragment.library.common.databinding.FragmentWebBinding
import com.tencent.smtt.sdk.WebView

class WebFragment : RouterFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): WebFragment {
            return WebFragment()
        }
    }

    private lateinit var webHelper: WebHelper
    private var url: String? = null
    private var html: String? = null

    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        webHelper.onDestroy()
    }

    override fun initView() {

        val onBackPressedCallback =
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if (webHelper.webView.canGoBack()) {
                    webHelper.webView.goBack()
                } else {
                    this.isEnabled = false
                    activity.onBackPressed()
                }
            }
        binding.black.setOnClickListener {
            onBackPressedCallback.isEnabled = false
            activity.onBackPressed()
        }
        webHelper = WebHelper.with(binding.webContainer)
        webHelper.onReceivedTitleListener = object : WebHelper.OnReceivedTitleListener {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                binding.title.text = title
            }
        }
    }

    override fun initViewModel() {
        arguments?.apply {
            url = this.getString(Keys.URL)
            html = this.getString(Keys.HTML)
        }
    }

    override fun onLoad() {
        html?.let {
            if (it.isNotBlank()) {
                webHelper.loadHtml(it)
            }
        }
        url?.let {
            if (it.isNotBlank()) {
                webHelper.loadUrl(it)
            }
        }
    }

}