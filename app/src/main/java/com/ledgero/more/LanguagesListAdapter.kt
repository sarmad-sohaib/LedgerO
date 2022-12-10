package com.ledgero.more

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ledgero.R
import com.ledgero.databinding.LanguageListItemBinding

class LanguagesListAdapter(
    private val context: Activity,
    private val arrayList: ArrayList<Language>
): ArrayAdapter<Language>(
    context,
    R.layout.language_list_item,
    arrayList
) {

    lateinit var view: View
    lateinit var binding: LanguageListItemBinding

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = LayoutInflater.from(context)

        if (convertView == null) {
            view = inflater.inflate(R.layout.language_list_item, parent, false)
        }
        binding = LanguageListItemBinding.bind(view)

        val language = getItem(position)

        if (language != null) {
            binding.textViewLanguage.text = language.name
        }

        return view
    }
}