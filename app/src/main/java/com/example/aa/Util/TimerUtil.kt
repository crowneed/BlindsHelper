package com.example.aa.Util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import io.reactivex.CompletableEmitter
import java.util.*

class CustomTextToSpeech {

    private var tts: TextToSpeech? = null
    private var ttsEnabled = false


    fun speak(blinds: Deque<String>) {
        if (ttsEnabled) {
            tts?.speak(
                "Текущие блайнды ${if (blinds.size != 0) blinds.peekFirst() else "дальше считайте сами"}",
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }
    }

    fun init(context: Context, emitter: CompletableEmitter) {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                if (tts?.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    tts?.language = Locale(Locale.getDefault().language)
                } else {
                    tts?.language = Locale.UK
                }
                tts?.setPitch(1.3f)
                tts?.setSpeechRate(1f)
                ttsEnabled = true
                emitter.onComplete()
//                tts?.speak("Как же долго я создавался", TextToSpeech.QUEUE_FLUSH, null, null)
            } else if (it == TextToSpeech.ERROR) {
                Toast.makeText(context, "ОШИБКА ОЗВУЧИВАНИЯ", Toast.LENGTH_LONG).show()
                ttsEnabled = false
            }
        })
    }
}