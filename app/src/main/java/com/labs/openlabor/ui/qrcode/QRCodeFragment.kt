package com.labs.openlabor.ui.qrcode

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.labs.openlabor.QRScannerActivity
import com.labs.openlabor.R
import com.labs.openlabor.databinding.FragmentQrCodeBinding
import com.labs.openlabor.model.Employee
import com.labs.openlabor.util.QRCodeGenerator

class QRCodeFragment : Fragment() {

    private var _binding: FragmentQrCodeBinding? = null
    private val binding get() = _binding!!

    private lateinit var qrCodeViewModel: QRCodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        qrCodeViewModel = ViewModelProvider(this).get(QRCodeViewModel::class.java)

        _binding = FragmentQrCodeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupObservers()
        setupSpinners()
        setupScanButton()

        // Load employees when the fragment is created
        qrCodeViewModel.loadEmployees()

        return root
    }

    private fun setupObservers() {
        // Observe employees list changes
        qrCodeViewModel.employees.observe(viewLifecycleOwner) { employees ->
            updateEmployeeSpinner(employees)
        }

        // Observe selected employee changes
        qrCodeViewModel.selectedEmployee.observe(viewLifecycleOwner) { employee ->
            updateTimeEntrySpinner(employee)
            updateQRCode()
        }

        // Observe selected time entry changes
        qrCodeViewModel.selectedTimeEntryPosition.observe(viewLifecycleOwner) { _ ->
            updateQRCode()
        }

        // Observe QR code type changes
        qrCodeViewModel.qrCodeType.observe(viewLifecycleOwner) { _ ->
            updateQRCode()
        }
    }

    private fun setupSpinners() {
        // Set up QR code type spinner
        val qrTypes = arrayOf(
            getString(R.string.qr_code_employee),
            getString(R.string.qr_code_time_entry),
            getString(R.string.qr_code_summary)
        )
        val qrTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, qrTypes)
        qrTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerQrType.adapter = qrTypeAdapter
        binding.spinnerQrType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                qrCodeViewModel.setQRCodeType(position)

                // Show/hide time entry spinner based on selected QR type
                binding.spinnerTimeEntry.visibility = if (position == 1) View.VISIBLE else View.GONE
                binding.textTimeEntryLabel.visibility = if (position == 1) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Set up employee spinner
        binding.spinnerEmployee.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                qrCodeViewModel.selectEmployee(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Set up time entry spinner
        binding.spinnerTimeEntry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                qrCodeViewModel.selectTimeEntry(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun updateEmployeeSpinner(employees: List<Employee>) {
        val employeeNames = employees.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerEmployee.adapter = adapter

        // Show/hide "no employees" message
        binding.textNoEmployees.visibility = if (employees.isEmpty()) View.VISIBLE else View.GONE
        binding.spinnerEmployee.visibility = if (employees.isEmpty()) View.GONE else View.VISIBLE
        binding.spinnerQrType.visibility = if (employees.isEmpty()) View.GONE else View.VISIBLE
        binding.imageViewQrCode.visibility = if (employees.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateTimeEntrySpinner(employee: Employee?) {
        employee?.let {
            val timeEntryStrings = it.timeEntries.map { entry ->
                "${entry.getFormattedDate()} (${entry.getFormattedHours()})"
            }

            if (timeEntryStrings.isEmpty()) {
                val noEntriesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
                    listOf("No time entries"))
                noEntriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerTimeEntry.adapter = noEntriesAdapter
                binding.spinnerTimeEntry.isEnabled = false
            } else {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeEntryStrings)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerTimeEntry.adapter = adapter
                binding.spinnerTimeEntry.isEnabled = true
            }
        }
    }

    private fun updateQRCode() {
        val employee = qrCodeViewModel.selectedEmployee.value ?: return

        // Generate QR code based on selected type
        val qrCodeBitmap = when (qrCodeViewModel.qrCodeType.value) {
            0 -> { // Employee Info
                QRCodeGenerator.generateEmployeeQRCode(employee)
            }
            1 -> { // Time Entry
                val position = qrCodeViewModel.selectedTimeEntryPosition.value ?: 0
                if (employee.timeEntries.isNotEmpty() && position < employee.timeEntries.size) {
                    val timeEntry = employee.timeEntries[position]
                    QRCodeGenerator.generateTimeEntryQRCode(timeEntry, employee.name)
                } else {
                    null
                }
            }
            2 -> { // Summary
                QRCodeGenerator.generateSummaryQRCode(employee)
            }
            else -> null
        }

        // Update QR code image view
        if (qrCodeBitmap != null) {
            binding.imageViewQrCode.setImageBitmap(qrCodeBitmap)
            binding.textNoQrCode.visibility = View.GONE
        } else {
            binding.imageViewQrCode.setImageDrawable(null)
            binding.textNoQrCode.visibility = View.VISIBLE
        }
    }

    /**
     * Refreshes employee data from the repository
     */
    fun refreshData() {
        qrCodeViewModel.loadEmployees()
    }

    /**
     * Sets up the QR code scanner button
     */
    private fun setupScanButton() {
        binding.buttonScanQr.setOnClickListener {
            // Launch the QR scanner activity
            val intent = Intent(requireContext(), QRScannerActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
