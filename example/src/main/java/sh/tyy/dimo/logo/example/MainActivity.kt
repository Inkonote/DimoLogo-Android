package sh.tyy.dimo.logo.example

import android.app.Dialog
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import sh.tyy.dimo.logo.DimoLogoView


class MainActivity : AppCompatActivity() {
    private lateinit var logoView: DimoLogoView
    private var hud: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logoView = findViewById(R.id.view_logo)
        logoView.playAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.min = 0
        }
        seekBar.max = 100
        progress_text_view.text = "${seekBar.progress / 100.toFloat()}"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = seekBar.progress / 100.toFloat()
                progress_text_view.text = "$progress"
                logoView.progress = progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }

    fun onPlayAnimation(view: View) {
        logoView.playAnimation()
    }

    fun onStopAnimation(view: View) {
        logoView.stopAnimation()
    }

    fun onShowHUD(view: View) {
        hud = DimoLogoView.hud(this)
        hud?.show()
        Handler().postDelayed({
            hud?.dismiss()
            hud = DimoLogoView.hud(this, "Nothing")
            hud?.show()
            Handler().postDelayed({
                hud?.dismiss()
            }, 3000)
        }, 3000)
    }
}
