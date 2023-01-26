package com.gnbsoftec.dolphinnative.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gnbsoftec.dolphinnative.databinding.CustomDialogConfirmBinding

class CustomDialogConfirmDialog(confirmDialogInterface: CustomDialogConfirmBindingInterface, text: String,noBtnName:String?,yesBtnName:String?) : DialogFragment() {

    // 뷰 바인딩 정의
    private var _binding: CustomDialogConfirmBinding? = null
    private val binding get() = _binding!!

    private var confirmDialogInterface: CustomDialogConfirmBindingInterface? = null

    private var text: String? = null
    private var noBtnName: String? = null
    private var yesBtnName: String? = null

    init {
        this.text = text
        this.noBtnName = noBtnName
        this.yesBtnName = yesBtnName
        this.confirmDialogInterface = confirmDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomDialogConfirmBinding.inflate(inflater, container, false)
        val view = binding.root

        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.confirmTextView.text = text

        if(!TextUtils.isEmpty(noBtnName)){
            binding.noButton.text=noBtnName
        }
        if(!TextUtils.isEmpty(yesBtnName)){
            binding.yesButton.text=yesBtnName
        }

        // 취소 버튼 클릭
        binding.noButton.setOnClickListener {
            this.confirmDialogInterface?.onNoButtonClick()
            dismiss()
        }

        // 확인 버튼 클릭
        binding.yesButton.setOnClickListener {
            this.confirmDialogInterface?.onYesButtonClick()
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface CustomDialogConfirmBindingInterface {
    fun onYesButtonClick()
    fun onNoButtonClick()
}
