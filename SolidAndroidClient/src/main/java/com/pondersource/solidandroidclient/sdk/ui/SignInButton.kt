package com.pondersource.solidandroidclient.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.pondersource.solidandroidclient.databinding.BtnSignInBinding


class SignInButton: ConstraintLayout {

    private val binding: BtnSignInBinding =
        BtnSignInBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
}