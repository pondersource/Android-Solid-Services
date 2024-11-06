package com.pondersource.solidandroidclient.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.pondersource.solidandroidclient.databinding.BtnSignInBinding


class SignInButton: ConstraintLayout {

    private val binding: BtnSignInBinding
    private val btn: AppCompatButton

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    init {
        binding = BtnSignInBinding.inflate(LayoutInflater.from(context), this, true)
        btn = binding.btn
    }

    public fun setButtonClickListener(listener: OnClickListener) {
        btn.setOnClickListener(listener)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        btn.setOnClickListener(l)
    }
}