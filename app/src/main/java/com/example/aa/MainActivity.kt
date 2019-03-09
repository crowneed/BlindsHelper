package com.example.aa

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.view.WindowManager
import com.example.aa.Util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var timerLengthSeconds = 0L
    private var secondsRemaining = 0L
    private var timerState = TimerState.Stopped
    private var customTextToSpeech: CustomTextToSpeech? = null
    private var blinds = ArrayDeque<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: Написать работающий код для открытия активности из сна для всех API
        val win = window
//        setShowWhenLocked(true)
//        setTurnScreenOn(true)
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        setContentView(R.layout.activity_main)

        fab_play.setOnClickListener {
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }
        fab_pause.setOnClickListener { pause() }

        fab_on_off.setOnClickListener {
            stopTimer()
            updateButtons()
        }
    }

    private fun pause() {
        timer?.cancel()
        timerState = TimerState.Paused
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
            TimerState.Running -> {
                fab_play.isEnabled = false
                fab_pause.isEnabled = true
                fab_on_off.isEnabled = true
            }
            TimerState.Stopped -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_on_off.isEnabled = false
            }
            TimerState.Paused -> {
                fab_play.isEnabled = true
                fab_pause.isEnabled = false
                fab_on_off.isEnabled = true
            }
        }
    }

    private fun startTimer() {
        //TODO:Можно ли это как-то сделать в самой сигнатуре метода, а не в теле? (типа по умолчанию)
        timerState = TimerState.Running

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
        val lengthInMinutes = PrefUtil.getTimerLength(this)
//        timerLengthSeconds = (lengthInMinutes * 60L)
        timerLengthSeconds = (lengthInMinutes * 6L)
        pb_timer.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        pb_timer.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        //TODO: ТУТ ВРОДЕ ВСЕ НОРМ
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0$secondsStr"}"
        pb_timer.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }


    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(this)

        if (timerState != TimerState.Stopped){
           tv_blinds.setTextBlinds(blinds)
        }
    }

    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running) {
            timer?.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
        } else if (timerState == TimerState.Paused) {
            //TODO: show notification
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
//        if (blinds.isEmpty()) pause()
        PrefUtil.setTimerState(timerState, this)
        PrefUtil.setBlindsState(blinds, this)
    }

    fun initTimer() {
        customTextToSpeech = CustomTextToSpeech()
        customTextToSpeech?.init(this)

        //TODO: Сделать чтобы если нет записи в prefs возвращал не нул, а предопределенный список.
        val blindsTemp = PrefUtil.getBlindsState(this)
        if (blindsTemp == null) {
            initBlinds()
        } else {
            blinds = blindsTemp
        }

        timerState = PrefUtil.getTimerState(this)

        //TODO: Если таймер не был остановлен, то берем предыдущую его макс длину
        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        //TODO: Если таймер не был остановлен, то берем из настроек сколько времени осталось
        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
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
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds
        updateCountdownUI()
        startTimer()
    }

    fun stopTimer() {
        blinds.clear()
        initBlinds()
        tv_blinds.text = ""
        if (timer != null)
            timer?.cancel()
        timerState = TimerState.Stopped
        setNewTimerLength()
//        PrefUtil.setCurrentBlind("", this)
        PrefUtil.setBlindsState(null, this)
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
