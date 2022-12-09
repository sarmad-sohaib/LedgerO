package com.ledgero.more

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ledgero.databinding.LanguageBottomSheetContentBinding
import java.util.*
import kotlin.collections.ArrayList

class LanguageModelBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: LanguageBottomSheetContentBinding

    companion object {
        const val TAG = "LanguageModelBottomSheet"
    }

    @SuppressLint("LongLogTag")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val arrayList = listOf("eng", "urdu")
        val languagesListAdapter = LanguagesListAdapter(requireActivity(), arrayList.map {
            Language(
                name = it
            )
        } as ArrayList<Language>)

        binding = LanguageBottomSheetContentBinding.inflate(inflater)

        binding.listViewLanguages.isClickable = true
        binding.listViewLanguages.adapter = languagesListAdapter
        binding.listViewLanguages.setOnItemClickListener { parent, view, position, id ->

            when (arrayList[position]) {
                "eng" -> {
                    Log.i(TAG, "onCreateView: eng")
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en-EN")
                    AppCompatDelegate.setApplicationLocales(appLocale)

                    this.dismiss()
                }

                "urdu" -> {
                    Log.i(TAG, "onCreateView: urdu")
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("ur-UR")
                    AppCompatDelegate.setApplicationLocales(appLocale)

                    this.dismiss()
                }
            }

        }

        return binding.root
    }
}