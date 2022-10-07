package com.ledgero.reminders.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ledgero.databinding.FragmentRemindersBinding
import com.ledgero.reminders.reminderalert.AlertReceiver
import com.ledgero.reminders.reminders.data.Reminder
import com.ledgero.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


private const val TAG = "ReminderFragment"

@AndroidEntryPoint
class RemindersFragment : Fragment(), OnItemClick {

    private lateinit var mBinding: FragmentRemindersBinding
    private val mViewModel: RemindersViewModel by viewModels()
    private val mAdapter = ReminderListAdapter(this)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentRemindersBinding.inflate(layoutInflater)

        mBinding.apply {
            recyclerViewReminders.adapter = mAdapter
            recyclerViewReminders.layoutManager = LinearLayoutManager(context)

            buttonAddNewReminder.setOnClickListener {
                mViewModel.addNewReminderClicked()
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val reminder = mAdapter.currentList[viewHolder.adapterPosition]
                    mViewModel.onReminderSwiped(reminder)
                }

            }).attachToRecyclerView(recyclerViewReminders)
        }

        //collecting UI state
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.uiState.collect { event ->
                    updateUiState(event)
                }
            }
        }

        //getting reminder saves or updated result from AddEditReminderFragment
        setFragmentResultListener("save-result") { _, bundle ->
            bundle.getString("result")?.let { mViewModel.onSaveReminderResult(it) }
        }

        //collecting UI events
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.uiEvents.collect { event ->
                    reactOnUiEvents(event)
                }
            }
        }

        // Inflate the layout for this fragment
        return mBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun reactOnUiEvents(event: ReminderUiEvents) {
        when (event) {
            is ReminderUiEvents.NavigateToAddReminderScreen -> navigateToAddReminderScreen()
            is ReminderUiEvents.NavigateToEditReminderScreen -> navigateToEditReminderScreen(event.reminder)
            is ReminderUiEvents.ShowSaveReminderResult -> showSaveReminderResult(event.result)
            is ReminderUiEvents.ShowUndoDeleteReminderMessage -> showUndoDeleteReminderMessage(event.reminder)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showUndoDeleteReminderMessage(reminder: Reminder) {
        Snackbar.make(
            requireView(), "Reminder deleted successfully", Snackbar.LENGTH_LONG
        ).setAction("UNDO") {
            mViewModel.onUndoDeleteClicked(reminder)
        }.show()
        cancelAlarm(reminder)
    }

    private fun showSaveReminderResult(result: String) {
        view?.showSnackBar(result)
    }

    private fun navigateToEditReminderScreen(reminder: Reminder) {
        val action =
            RemindersFragmentDirections.actionRemindersFragmentToAddEditReminderFragment(
                reminder
            )
        findNavController().navigate(action)
    }

    private fun navigateToAddReminderScreen() {
        val action =
            RemindersFragmentDirections.actionRemindersFragmentToAddEditReminderFragment()
        findNavController().navigate(action)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateUiState(event: RemindersUiState) {
        when (event) {
            is RemindersUiState.Loading -> loadingUiState()
            is RemindersUiState.Error -> TODO("Not yet implemented")
            is RemindersUiState.AllReminders -> allRemindersUiState(event.list)
            is RemindersUiState.Empty -> noRemindersUiState()
        }
    }

    private fun noRemindersUiState() {
        mBinding.apply {
            recyclerViewReminders.isVisible = false
            progressbarLoading.isVisible = false
            textViewNoReminders.isVisible = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun allRemindersUiState(remindersList: List<Reminder?>) {
        mBinding.apply {
            progressbarLoading.isVisible = false
            recyclerViewReminders.isVisible = true
            textViewNoReminders.isVisible = false
            recyclerViewReminders.adapter = mAdapter

            mAdapter.submitList(remindersList)

            for (result in remindersList) {
                if (result != null) {
                    cancelAlarm(result)
                }
            }

            for (result in remindersList) {

                if (result != null) {
                    setAlarm(result)
                }
            }
        }
    }

    private fun loadingUiState() {
        mBinding.apply {
            recyclerViewReminders.isVisible = false
            progressbarLoading.isVisible = true
            textViewNoReminders.isVisible = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setAlarm(reminder: Reminder) {
        val intent = Intent(requireContext(), AlertReceiver::class.java)
        intent.putExtra("reminder", reminder)
        val pendingIntent = reminder.id.take(5).let {
            PendingIntent.getBroadcast(
                requireContext(), it.toInt(), intent, PendingIntent.FLAG_IMMUTABLE
            )
        }

        reminder.timeStamp?.let {
            (activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setExact(
                AlarmManager.RTC_WAKEUP, it, pendingIntent
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun cancelAlarm(reminder: Reminder) {

        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val intent = Intent(requireContext(), AlertReceiver::class.java)
        val pendingIntent = reminder.id.take(5).let {
            PendingIntent.getBroadcast(
                requireContext(), it.toInt(), intent, PendingIntent.FLAG_IMMUTABLE
            )
        }

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent?.cancel()
        }
    }

    override fun onReminderClick(reminder: Reminder) {
        mViewModel.onReminderClicked(reminder)
    }
}