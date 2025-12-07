package com.example.library_management

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.progressindicator.CircularProgressIndicator

class LoadingDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private var message: String = "Loading..."

    fun show(message: String = "Loading...") {
        this.message = message

        if (dialog == null) {
            createDialog()
        }

        val messageText = dialog?.findViewById<TextView>(R.id.loading_message)
        messageText?.text = message

        // Show dialog with entrance animation
        dialog?.show()

        // Animate the dialog entrance
        val loadingCard = dialog?.findViewById<CardView>(R.id.loading_card)
        loadingCard?.let { card ->
            card.alpha = 0f
            card.scaleX = 0.7f
            card.scaleY = 0.7f

            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
        }

        // Add subtle pulsing animation to the message
        val pulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse)
        messageText?.startAnimation(pulseAnimation)
    }

    fun dismiss() {
        val loadingCard = dialog?.findViewById<CardView>(R.id.loading_card)
        loadingCard?.let { card ->
            card.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .withEndAction {
                    dialog?.dismiss()
                }
                .start()
        } ?: run {
            dialog?.dismiss()
        }
    }

    fun setMessage(message: String) {
        this.message = message
        val messageText = dialog?.findViewById<TextView>(R.id.loading_message)
        messageText?.text = message
    }

    private fun createDialog() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Create the dialog content
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)

            // Setup the dialog content
            val loadingCard = view.findViewById<CardView>(R.id.loading_card)
            val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
            val loadingMessage = view.findViewById<TextView>(R.id.loading_message)

            // Apply styling
            loadingMessage.text = message
            loadingMessage.setTextColor(context.getColor(R.color.text_primary))

            // Set the indicator color
            loadingIndicator.setIndicatorColor(context.getColor(R.color.primary))

            setContentView(view)
        }
    }
}