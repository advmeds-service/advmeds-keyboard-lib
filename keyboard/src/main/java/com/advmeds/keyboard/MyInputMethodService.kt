package com.advmeds.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat

open class MyInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var mCurKeyboard = InputType.TYPE_NULL
    private var caps = false

    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View {

        keyboardView =
            layoutInflater.inflate(R.layout.keyboard, null).let { it as KeyboardView }.also {

                it.setOnKeyboardActionListener(this)
                it.isPreviewEnabled = false
            }

        return keyboardView
    }

    override fun onPress(primaryCode: Int) {
        keyboardView.isPreviewEnabled = false
    }

    override fun onRelease(primaryCode: Int) { }

    private fun Resources.getDrawableBy(id: Int): Drawable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ResourcesCompat.getDrawable(this, id, null)
        } else {
            @Suppress("DEPRECATION")
            this.getDrawable(id)
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {

        val ic = currentInputConnection

        ic ?: return

//        playSoundEffect(primaryCode)

        when (primaryCode) {

            Keyboard.KEYCODE_DELETE -> {

                ic.deleteSurroundingText(1, 0)
            }
            Keyboard.KEYCODE_CANCEL -> {

                requestHideSelf(0)
            }
            Keyboard.KEYCODE_SHIFT -> {

                caps = !caps
                keyboard.isShifted = caps
                keyboardView.invalidateAllKeys()
                keyboard.keys.asSequence().first { it.codes.first() == primaryCode }?.icon = resources.getDrawableBy(if (caps) R.drawable.ic_keyboard_capslock else R.drawable.ic_keyboard_caps)
            }
            resources.getInteger(R.integer.keycode_undefined) -> {
            }
            else -> {

                var code = primaryCode.toChar()

                if (code.isLetter() && caps) {
                    code = code.toUpperCase()
                }

                ic.commitText(code.toString(), 1)
            }
        }
    }

    private fun playSoundEffect(primaryCode: Int) {

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val effectType = when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> AudioManager.FX_KEYPRESS_DELETE
            Keyboard.KEYCODE_CANCEL -> AudioManager.FX_KEYPRESS_RETURN
            resources.getInteger(R.integer.keycode_spacebar) -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }

        audioManager.playSoundEffect(effectType)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        mCurKeyboard = attribute?.inputType ?: InputType.TYPE_NULL
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        keyboard = when (mCurKeyboard) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE -> Keyboard(
                this,
                R.xml.key_number
            )
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS -> Keyboard(
                this,
                R.xml.key_id
            )
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> Keyboard(
                this,
                R.xml.key_password
            )
            else -> Keyboard(this, R.xml.key_qwerty)
        }

        keyboardView.keyboard = keyboard
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onUpdateExtractingVisibility(ei: EditorInfo?) {
        ei?.imeOptions = ei?.imeOptions ?: EditorInfo.IME_FLAG_NO_EXTRACT_UI
        super.onUpdateExtractingVisibility(ei)
    }

    override fun onText(text: CharSequence?) {}

    override fun swipeRight() {}

    override fun swipeLeft() {}

    override fun swipeUp() {}

    override fun swipeDown() {}
}