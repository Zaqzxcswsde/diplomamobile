package com.example.diplomamobile.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.diplomamobile.HomeErrStrings
import com.example.diplomamobile.InfoHolder
import com.example.diplomamobile.R
import com.example.diplomamobile.databinding.FragmentHomeBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var isFirstLaunch = true

        val loadingBarElem = view.findViewById<ProgressBar>(R.id.progressBarLoading)
        val txtViewStatusElem = view.findViewById<TextView>(R.id.textViewStat)
        val textViewStatusLabel = view.findViewById<TextView>(R.id.textViewStatusLabel)

        val cardView = view.findViewById<MaterialCardView>(R.id.MaterialCardViewStatus)

//        cardView.translationX = 50f
//        val springAnim = SpringAnimation(cardView, DynamicAnimation.TRANSLATION_X, 0f)
//        springAnim.spring.stiffness = SpringForce.STIFFNESS_HIGH //   // можно поэкспериментировать: VERY_LOW, LOW, MEDIUM, HIGH
//        springAnim.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY // или DAMPING_RATIO_LOW_BOUNCY для более выраженных колебаний
//        springAnim.start()

        // Предположим, cardView уже инициализирован
        val scaleXAnimator = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.1f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.1f, 1f)

        val bounceAnimatorSet = AnimatorSet().apply {
            playTogether(scaleXAnimator, scaleYAnimator)
            duration = 300
            interpolator = BounceInterpolator()
        }

//        println("12312")

        var latestNotifString = ""

        InfoHolder.homeErrLiveData.observe(viewLifecycleOwner) { homeErrStrings ->

            loadingBarElem.visibility = if (homeErrStrings.errEmoji.isEmpty()) View.VISIBLE else View.GONE
            txtViewStatusElem.visibility = if (homeErrStrings.errEmoji.isEmpty()) View.GONE else View.VISIBLE

            textViewStatusLabel.text = homeErrStrings.errMainText
            txtViewStatusElem.text = homeErrStrings.errEmoji


            if (isFirstLaunch){
                isFirstLaunch = false
            }
            else
            {
                if (latestNotifString == homeErrStrings.errMainText){
                    if (homeErrStrings.errEmoji.isNotEmpty() && homeErrStrings.errEmoji != "❓"){

//                        Toast.makeText(context, "${homeErrStrings.errMainText} ${homeErrStrings.errEmoji}", Toast.LENGTH_SHORT).show()
                        bounceAnimatorSet.start()
                    }
                }
            }
            if (homeErrStrings.errEmoji.isNotEmpty()) latestNotifString = homeErrStrings.errMainText
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}