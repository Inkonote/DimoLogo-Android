package sh.tyy.dimo.logo.example

import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import sh.tyy.dimo.logo.DimoLogoView


class MainActivity : AppCompatActivity() {
    private lateinit var logoView: DimoLogoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layout = findViewById<ConstraintLayout>(R.id.layout_main)
        val layoutParams = ConstraintLayout.LayoutParams(dpToPx(60f), dpToPx(60f))
        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        logoView = DimoLogoView(this)
        logoView.layoutParams = layoutParams
        logoView.setBackgroundColor(Color.BLACK)
        layout.addView(logoView)
        logoView.playAnimation()
    }

    fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun onPlayAnimation(view: View) {
        logoView.playAnimation()
    }

    fun onStopAnimation(view: View) {
        logoView.stopAnimation()
    }
}
