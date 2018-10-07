package sh.tyy.dimo.logo.example

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import sh.tyy.dimo.logo.DimoLogoView


class MainActivity : AppCompatActivity() {
    private lateinit var logoView: DimoLogoView
    private var hud: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logoView = findViewById(R.id.view_logo)
        logoView.playAnimation()
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
