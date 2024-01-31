package com.gnbsoftec.dolphinnative.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.databinding.DialogAlertBinding

class CustomAlert (context: Context, mTitle:String, mText:String, mBtnStrOk:String, mBtnStrCancel: String
                   , private val okCallback:((Boolean) -> Unit)
                   , private val cancelCallback:((Boolean) -> Unit)?): Dialog(context) {
    private lateinit var binding: DialogAlertBinding
    private val title = mTitle
    private val text = mText
    private val btnStr = mBtnStrOk
    private val btnStr2 = mBtnStrCancel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.text = title
        binding.title.contentDescription=title
        binding.message.text = text
        binding.message.contentDescription = text
        setCancelable(false)
//        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenWidth = context.resources.displayMetrics.widthPixels
        var screenHeight = context.resources.displayMetrics.heightPixels


        //폴더블 펼친화면에서 하단 alert 출력시 투명화된 위젯이랑 alert이랑 겹쳐 이상하게 보여 대응 로직 넣음 alert을 조금 올리니 화면이 맞음
        if(Constants.isFold(context)){
            screenHeight-=80
        }

        window?.setLayout(screenWidth,screenHeight)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btOk.visibility = View.VISIBLE
        binding.btOk.setOnClickListener {
            dismiss()
            okCallback(true)
        }

        if(!TextUtils.isEmpty(btnStr)){
            binding.btOk.text=btnStr
            binding.btOk.contentDescription=btnStr
        }
        if(!TextUtils.isEmpty(btnStr2)){
            binding.btCancel.text=btnStr2
            binding.btCancel.contentDescription=btnStr2
        }

        if(cancelCallback == null){
            binding.btCancel.visibility = View.GONE
//            binding.btEmtpy.visibility = View.VISIBLE
        }else{
            binding.btCancel.visibility = View.VISIBLE
//            binding.btEmtpy.visibility = View.GONE
            binding.btCancel.setOnClickListener {
                cancelCallback.let {
                    dismiss()
                    it(true)
                }
            }
        }
    }
}