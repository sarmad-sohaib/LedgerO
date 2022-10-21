package com.ledgero.reminders.ui.completedreminders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ledgero.R
import com.ledgero.databinding.FragmentCompletedRemindersBinding
import com.ledgero.reminders.data.Reminder
import com.ledgero.reminders.ui.reminders.OnItemClick
import com.ledgero.reminders.ui.reminders.ReminderListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompletedRemindersFragment : Fragment(), OnItemClick {

    private lateinit var mBinding: FragmentCompletedRemindersBinding
    private val mViewModel: CompletedReminderViewModel by viewModels()
    private val mAdapter = ReminderListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentCompletedRemindersBinding.inflate(inflater)

        mBinding.apply {
            recyclerViewCompletedReminders.layoutManager = LinearLayoutManager(context)
            recyclerViewCompletedReminders.adapter = mAdapter
        }

        //collecting ui states
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                mViewModel.uiState.collect { event ->
                    updateUi(event)
                }
            }
        }

        // Inflate the layout for this fragment
        return mBinding.root
    }

    private fun updateUi(event: CompletedReminderFragmentUiState) {
        when(event) {
            CompletedReminderFragmentUiState.Empty -> Unit
            is CompletedReminderFragmentUiState.ShowAllCompletedReminders -> showAllCompletedReminders(event.list)
        }
    }

    private fun showAllCompletedReminders(list: List<Reminder?>) {
        mAdapter.submitList(list)
    }

    override fun onReminderClick(reminder: Reminder) {
        TODO("Not yet implemented")
    }

    override fun onReminderCompleteCheckBoxClick(reminder: Reminder, checkboxValue: Boolean) {
        TODO("Not yet implemented")
    }
}