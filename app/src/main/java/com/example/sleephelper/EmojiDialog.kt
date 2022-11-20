package com.example.sleephelper

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sleephelper.databinding.EmojiDialogBinding

class EmojiDialog(private val context : AppCompatActivity) {

    private lateinit var binding : EmojiDialogBinding
    private val dlg = Dialog(context)   //부모 액티비티의 context 가 들어감
    private lateinit var listener : EmojiDialogSeleteListener //선택된 이모지를 writingDiary에 보내줄 인터페이스객체

    fun show() {
        binding = EmojiDialogBinding.inflate(context.layoutInflater)

        dlg.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴

        binding.cancelbutton?.setOnClickListener {
            dlg?.dismiss() // 다이얼로그 닫기
        }
        binding.goodImageView.setOnClickListener {
            listener.onClicked(R.drawable.emoji_good)
            dlg?.dismiss() // 다이얼로그 닫기
        }
        binding.normalImageView.setOnClickListener {
            listener.onClicked(R.drawable.emoji_normal)
            dlg?.dismiss() // 다이얼로그 닫기
        }
        binding.badImageView.setOnClickListener {
            listener.onClicked(R.drawable.emoji_bad)
            dlg?.dismiss() // 다이얼로그 닫기
        }
        dlg.show()
    }

    //선택한 이모지를 writingdiary 액티비티에 보내줄 인터페이스
    interface EmojiDialogSeleteListener {
        fun onClicked(image_id : Int)
    }

    fun setOnEmojiClickedListener(listener: (Int) -> Unit) {
        this.listener = object: EmojiDialogSeleteListener {
            override fun onClicked(image_id: Int) {
                listener(image_id)
            }
        }
    }
}