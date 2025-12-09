package com.labs.openlabor-mobile.ui.reset

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.labs.openlabor-mobile.R
import com.labs.openlabor-mobile.data.EmployeeRepository

/**
 * Dialog fragment for confirming and handling data reset
 */
class ResetDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.Theme_openlabor-mobile_Dialog)
            .setTitle(R.string.menu_reset)
            .setMessage(R.string.dialog_reset_confirm)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                resetAllData()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.action_cancel) { _, _ ->
                // User cancelled the dialog
                dismiss()
            }
            .create()
    }

    /**
     * Reset all application data
     */
    private fun resetAllData() {
        val repository = EmployeeRepository.getInstance(requireContext())
        repository.resetAllData()
    }
}
