package com.example.aa

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.test_activity.*


class MainActivity : AppCompatActivity() {
    private val timerFragment = TimerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: Написать работающий код для открытия активности из сна для всех API
        openWindowActivity()

        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fl_content, timerFragment).commit()


        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
//            this,
//            drawerLayout,
//            toolbar,
//            R.string.test_opened,
//            R.string.test_closed
//        ){
//            override fun onDrawerClosed(view: View){
//                super.onDrawerClosed(view)
//                //toast("Drawer closed")
//            }
//
//            override fun onDrawerOpened(drawerView: View){
//                super.onDrawerOpened(drawerView)
//                //toast("Drawer opened")
//            }
//        }
//        drawerToggle.isDrawerIndicatorEnabled = true
//        drawerLayout.addDrawerListener(drawerToggle)
//        drawerToggle.syncState()
//
//        setupDrawerContent()
    }

//    private fun setupDrawerContent() {
//        navigationView.setNavigationItemSelectedListener {
//            selectDrawerItem(it)
//            true
//        }
//    }
//
//    private fun selectDrawerItem(menuItem: MenuItem) {
//        // Создать новый фрагмент и задать фрагмент для отображения
//        // на основе нажатия на элемент навигации
////        var fragment: Fragment? = null
////        val fragmentClass: Class<*>
////        when (menuItem.itemId) {
////            R.id.item_menu_settings -> {} /*fragmentClass = FirstFragment::class.java*/
////            R.id.item_menu_statistics -> {}
////            R.id.item_menu_eval_cards -> {}
////            else -> {} /*fragmentClass = FirstFragment::class.java*/
////        }
//
////        try {
//////            fragment = fragmentClass.newInstance() as Fragment
////        } catch (e: Exception) {
////            e.printStackTrace()
////        }
////
////        // Вставить фрагмент, заменяя любой существующий
////        val fragmentManager = supportFragmentManager
////        if (fragment != null)
////        fragmentManager.beginTransaction().replace(R.id.fl_content, fragment).commit()
//
//        // Выделение существующего элемента выполнено с помощью
//        // NavigationView
//        menuItem.isChecked = true
//        // Установить заголовок для action bar'а
//
//        title = menuItem.title
//        // Закрыть navigation drawer
//        drawerLayout.closeDrawer(GravityCompat.START)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            android.R.id.home -> {
//                drawerLayout.openDrawer(GravityCompat.START)
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun openWindowActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
//            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }
    }

    private fun clearFlags() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

    override fun onDestroy() {
        clearFlags()
        super.onDestroy()
    }
}
