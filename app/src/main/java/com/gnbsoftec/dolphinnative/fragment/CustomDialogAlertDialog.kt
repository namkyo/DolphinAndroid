package com.gnbsoftec.dolphinnative.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gnbsoftec.dolphinnative.databinding.CustomDialogAlertBinding

class CustomDialogAlertDialog(alertDialogInterface: CustomDialogAlertBindingInterface,text: String) : DialogFragment() {

    // 뷰 바인딩 정의
    private var _binding: CustomDialogAlertBinding? = null
    private val binding get() = _binding!!

    private var alertDialogInterface: CustomDialogAlertBindingInterface? = null

    private var text: String? = null

    init {
        this.text = text
        this.alertDialogInterface = alertDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomDialogAlertBinding.inflate(inflater, container, false)
        val view = binding.root

        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.confirmTextView.text = text

        // 확인 버튼 클릭
        binding.yesButton.setOnClickListener {
            this.alertDialogInterface?.onYesButtonClick()
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface CustomDialogAlertBindingInterface {
    fun onYesButtonClick()
}
