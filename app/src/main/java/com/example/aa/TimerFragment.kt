package com.example.aa

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.aa.Util.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timer.*
import java.util.*

class TimerFragment : Fragment() {
    
    private var timer: CountDownTimer? = null
    private var timerLengthSeconds = 0L
    private var secondsRemaining = 0L
    private var timerState = TimerFragment.TimerState.Stopped
    private var customTextToSpeech: CustomTextToSpeech? = null
    private var blinds = ArrayDeque<String>()
    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab_play.setOnClickListener {
            startTimer()
            timerState = TimerFragment.TimerState.Running
            updateButtons()
        }
        fab_pause.setOnClickListener { pause() }

        fab_on_off.setOnClickListener {
            stopTimer()
            updateButtons()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun pause() {
        timer?.cancel()
        timerState = TimerFragment.TimerState.Paused
        updateButtons()
    }

    private fun initBlinds() {
        //TODO: Определить что лучше стек или очередь и хранить только малый/большой блайнд в виде int
        blinds.clear()
        blinds.addLast("20, 40")
        blinds.addLast("30, 60")
        blinds.addLast("50, 100")
        blinds.addLast("75, 150")
        blinds.addLast("100, 200")
        blinds.addLast("150, 300")
        blinds.addLast("200, 400")
        blinds.addLast("300, 600")
        blinds.addLast("400, 800")
        blinds.addLast("500, 1000")
        blinds.addLast("750, 1500")
        blinds.addLast("1000, 2000")
    }

    private fun updateButtons() {
        //TODO: Здесь запрещается нажимать на определенные кнопки при определенном состоянии таймера
        //TODO: Как минимум сделать это визуально более понятным для юзера...
        when (timerState) {
            TimerFragment.TimerState.Running -> {
                fab_play.isEnabled = false
                fab_pause.isEnabled = true
                fab_on_off.isEnabled = true
            }
            TimerFragment.TimerState.Stopped -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_on_off.isEnabled = false
            }
            TimerFragment.TimerState.Paused -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_on_off.isEnabled = true
            }
        }
    }

    private fun startTimer() {
        //TODO:Можно ли это как-то сделать в самой сигнатуре метода, а не в теле? (типа по умолчанию)
        timerState = TimerFragment.TimerState.Running

        //TODO: Пишем текущий блайнд (без удаления из очереди) всегда при старте таймера
        tv_blinds.setTextBlinds(blinds)

        //TODO: Озвучивать текущий блайнд (без удаления из очереди) всегда при старте таймера
        customTextToSpeech?.speak(blinds)

        //TODO: Начать таймер с определенным количеством секунд (берем общее время таймера и текущее из prefs)
        timer = object : CountDownTimer(secondsRemaining * TIMER_ONE_SECOND, TIMER_TICK_INTERVAL) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / TIMER_ONE_SECOND
                updateCountdownUI()
            }
        }
        timer?.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(context)
//        timerLengthSeconds = (lengthInMinutes * 60L)
        timerLengthSeconds = (lengthInMinutes * 6L)
        pb_timer.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(context)
        pb_timer.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        //TODO: ТУТ ВРОДЕ ВСЕ НОРМ
        //TODO: не, нихуя))
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val secondsUntilFinished = if (secondsStr.length == 2) secondsStr else "0$secondsStr"
        textView.text = getString(R.string.timeUntilFinished, minutesUntilFinished, secondsUntilFinished)
//        textView.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0$secondsStr"}"
        pb_timer.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(context)

        if (timerState != TimerState.Stopped) {
            tv_blinds.setTextBlinds(blinds)
        }
    }

    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running) {
            timer?.cancel()
            val wakeUpTime = setAlarm(context, nowSeconds, secondsRemaining)
        } else if (timerState == TimerState.Paused) {
            //TODO: show notification
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, context)
        PrefUtil.setSecondsRemaining(secondsRemaining, context)
//        if (blinds.isEmpty()) pause()
        PrefUtil.setTimerState(timerState, context)
        PrefUtil.setBlindsState(blinds, context)
    }

    private fun initTimer() {
        disposable = Completable.create {
            customTextToSpeech = CustomTextToSpeech()
            customTextToSpeech?.init(activity?.applicationContext, it)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val act = activity
                val intentM = act?.intent
                val fromReceiver = intentM?.getBooleanExtra(
                    "TAG_FROM_TIMER_RECEIVER",
                    false
                )?:false
                if (fromReceiver) {
                    activity?.intent?.putExtra("TAG_FROM_TIMER_RECEIVER", false)
                    customTextToSpeech?.speak(blinds)
                }
            }

//        customTextToSpeech = CustomTextToSpeech()
//        customTextToSpeech?.init(this)

        //TODO: Сделать чтобы если нет записи в prefs возвращал не нул, а предопределенный список.
        val blindsTemp = PrefUtil.getBlindsState(context)
        if (blindsTemp == null) {
            initBlinds()
        } else {
            blinds = blindsTemp
        }

        timerState = PrefUtil.getTimerState(context)

        //TODO: Если таймер не был остановлен, то берем предыдущую его макс длину
        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        //TODO: Если таймер не был остановлен, то берем из настроек сколько времени осталось
        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(context)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(context)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
//        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        mp = MediaPlayer.create(this, soundUri)
    }

    fun onTimerFinished() {
//        mp.start()
//        customTextToSpeech?.speak(blinds)
        if (blinds.isNotEmpty())
            blinds.pop()

        timer?.cancel()
        setNewTimerLength()
        PrefUtil.setSecondsRemaining(timerLengthSeconds, context)
        secondsRemaining = timerLengthSeconds
        updateCountdownUI()
        startTimer()
    }

    fun stopTimer() {
        blinds.clear()
        initBlinds()
        tv_blinds.text = ""
        timer?.cancel()
        timerState = TimerState.Stopped
        setNewTimerLength()
//        PrefUtil.setCurrentBlind("", this)
        PrefUtil.setBlindsState(null, context)
        PrefUtil.setSecondsRemaining(timerLengthSeconds, context)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    override fun onDestroy() {
        disposable?.dispose()
        fab_on_off?.setOnClickListener(null)
        fab_pause?.setOnClickListener(null)
        fab_play?.setOnClickListener(null)
        super.onDestroy()
    }

    companion object {
        fun setAlarm(context: Context?, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context?) {
            val intent = Intent(context, TimerReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }
}
