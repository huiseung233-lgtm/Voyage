package com.captain.voyage.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.captain.voyage.databinding.DialogAddRecordBinding

class AddRecordDialog(
    private val onSaveClicked: (String, Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddRecordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddRecordBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {
            val content = binding.etContent.text.toString()
            val score = binding.etScore.text.toString().toIntOrNull() ?: 0

            if (content.isNotBlank()) {
                onSaveClicked(content, score)
                dismiss()
            } else {
                Toast.makeText(context, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}