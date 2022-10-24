package com.ledgero.reminders.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
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
import com.ledgero.R
import com.ledgero.databinding.FragmentRemindersBinding
import com.ledgero.reminders.data.Reminder
import com.ledgero.reminders.reminderalert.AlertReceiver
import com.ledgero.reminders.ui.reminders.ReminderUiEvents.*
import com.ledgero.reminders.ui.reminders.RemindersUiState.*
import com.ledgero.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.ledgero.reminders.ui.reminders.RemindersFragmentDirections as RemindersNav

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

        val menuHost = requireActivity() as MenuHost

        mBinding.apply {
            recyclerViewReminders.adapter = mAdapter
            recyclerViewReminders.layoutManager = LinearLayoutManager(context)

            buttonAddNewReminder.setOnClickListener {
                mViewModel.addNewReminderClicked()
            }

            //inflating options menu
            menuHost.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.reminders_fragment_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.menuItem_completedReminders -> {
                            mViewModel.showCompletedRemindersClicked()
                            return true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)

            //creating delete reminder on swipe
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
            //ReminderUiEvents.kt has been statically imported
            is NavigateToAddReminderScreen -> navigateToAddReminderScreen()
            is NavigateToEditReminderScreen -> navigateToEditReminderScreen(event.reminder)
            is ShowSaveReminderResult -> showSaveReminderResult(event.result)
            is ShowUndoDeleteReminderMessage -> showUndoDeleteReminderMessage(event.reminder)
            is CompleteReminder -> updateCompletedReminder(event.reminder, event.checkboxValue)
            is NavigateToCompletedRemindersScreen -> navigateToCompletedRemindersScreen()
            is ShowReminderCompleteMessage -> showCompleteReminderMessage(event.msg)
        }
    }

    private fun showCompleteReminderMessage(msg: String) {
        view?.showSnackBar(msg)
    }

    private fun navigateToCompletedRemindersScreen() {
        val action = RemindersNav.actionRemindersFragmentToCompletedRemindersFragment()
        findNavController().navigate(action)
    }

    private fun updateCompletedReminder(reminder: Reminder, checkboxValue: Boolean) {
        Log.i(TAG, "onReminderCompleteCheckBoxClick: $reminder")
        mViewModel.updateReminder(reminder, checkboxValue)
        mViewModel.completeReminderClicked()
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
            RemindersNav.actionRemindersFragmentToAddEditReminderFragment(
                reminder
            )
        findNavController().navigate(action)
    }

    private fun navigateToAddReminderScreen() {
        val action =
            RemindersNav.actionRemindersFragmentToAddEditReminderFragment()
        findNavController().navigate(action)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateUiState(event: RemindersUiState) {
        when (event) {
            //ReminderUiState.kt has been statically imported
            is Loading -> loadingUiState()
            is Error -> TODO("Not yet implemented")
            is AllReminders -> allRemindersUiState(event.list)
            is Empty -> noRemindersUiState()
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
                if (result != null && result.timeStamp!! > System.currentTimeMillis()) {
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

    override fun onReminderCompleteCheckBoxClick(reminder: Reminder, checkboxValue: Boolean) {
        mViewModel.checkBoxCompleteReminderChanged(reminder, checkboxValue)
    }
}