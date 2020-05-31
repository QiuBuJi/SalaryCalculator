package com.example.salarycalculator

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlin.random.Random

const val TAG: String = "msg"

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        title = "关于"

        //添加返回按钮
        supportActionBar?.run {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        about_txt.setOnClickListener {

            val random = Random(System.currentTimeMillis())
            var rgb = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
            getDrawable(R.drawable.bg_round).run {
                setTint(rgb)
                it.background = this
            }
            Log.d(TAG, "onCreate: $rgb")

            val set = ArrayList<Animator>()
            set.add(ObjectAnimator.ofFloat(about_txt, "scaleX", 1f, 2f, 1f))
            set.add(ObjectAnimator.ofFloat(about_txt, "scaleY", 1f, 2f, 1f))
            set.add(ObjectAnimator.ofFloat(about_txt, "rotation", 0f, 180f, 0f, -180f, 0f))

            AnimatorSet().run {
                duration = 2000
                playTogether(set)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        about_txt.text = ""
                    }

                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationCancel(animation: Animator?) = Unit
                    override fun onAnimationStart(animation: Animator?) {
                        about_txt.text = "吴先生"
                    }
                })
                start()
            }
        }

        about_txt.setOnLongClickListener {

            val set = ArrayList<Animator>()
            set.add(ObjectAnimator.ofFloat(about_txt, "scaleX", 1f, 2f, 1f))
            set.add(ObjectAnimator.ofFloat(about_txt, "scaleY", 1f, 2f, 1f))
            set.add(ObjectAnimator.ofFloat(about_txt, "rotation", 0f, 180f, 0f, -180f, 0f))
            set.add(ObjectAnimator.ofFloat(about_txt, "alpha", 1f, 0f, 1f))

            AnimatorSet().run {
                duration = 2000
                playTogether(set)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        about_txt.text = ""
                    }

                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationCancel(animation: Animator?) = Unit
                    override fun onAnimationStart(animation: Animator?) {
                        about_txt.text = "吴攀先生"
                    }
                })
                start()
            }
            return@setOnLongClickListener true
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.run {
            //返回
            if (itemId == android.R.id.home) finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
