package com.example.diplomamobile.ui.notifications

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.icu.text.IDNA.Info
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.diplomamobile.InfoHolder
import com.example.diplomamobile.MainActivity
import com.example.diplomamobile.R
import com.example.diplomamobile.databinding.FragmentNotificationsBinding

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager


import com.example.diplomamobile.dataStore
import com.google.android.material.card.MaterialCardView
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!






    private var isExpanded = false
    private var isExpanded2 = false
    private var isExpanded3 = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        val notificationsViewModel = ViewModelProvider(requireActivity()).get(NotificationsViewModel::class.java)

        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root

    }




    suspend fun editEnv(string: String) {
        val EXAMPLE_COUNTER = stringPreferencesKey("env")
        requireContext().dataStore.edit { settings ->
            settings[EXAMPLE_COUNTER] = string
        }
    }


    fun collapsePanel(parentContainer: LinearLayout, bodyLayout: LinearLayout, arrowIcon: ImageView) {
        TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply { duration = 80 })
        bodyLayout.visibility = View.GONE
        arrowIcon.animate().rotation(0f).setDuration(80).start()
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_delete_keypair).setOnClickListener(::onDeleteButtonClick)


        val textViewTokenDeleteWarning = view.findViewById<TextView>(R.id.textViewTokenDeleteWarning)
        val fullText = "⚠ Внимание!\nУдаление токена необратимо"
        val spannable = SpannableString(fullText)

        spannable.setSpan(
            ForegroundColorSpan(Color.YELLOW),
            0,
            11,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            28,
            38,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            28,
            38,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewTokenDeleteWarning.text = spannable


        val textViewVersion = view.findViewById<TextView>(R.id.textViewVersion)
        textViewVersion.text = textViewVersion.text.toString() + getString(R.string.app_ver_string)

//        setContentView(R.layout.item_expansion_panel)


        var keyText = "❓"
        (activity as? MainActivity)?.let { mainActivity ->
            val keyPair = mainActivity.getStoredKeyPair()
            if (keyPair != null) {
                keyText = mainActivity.exportPublicKeyToSingleLinePEM(keyPair.public)
            }
        }
        view.findViewById<TextView>(R.id.textViewCurrentPublicKey).text = "Текущий публичный ключ: " + keyText
        if (keyText != "❓") {
            viewLifecycleOwner.lifecycleScope.launch {
                val bitmap = generateQR(keyText, 512)
                binding.qrImageView.setImageBitmap(bitmap)
            }
            binding.qrImageView.visibility = View.VISIBLE
        }
        else {
            binding.qrImageView.visibility = View.GONE
        }



        val parentContainer = view.findViewById<LinearLayout>(R.id.linearLayourSettingCards)

        val headerLayout = view.findViewById<LinearLayout>(R.id.headerLayout)
        val bodyLayout = view.findViewById<LinearLayout>(R.id.bodyLayout)
        val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)
        val headerLayout2 = view.findViewById<LinearLayout>(R.id.headerLayout2)
        val bodyLayout2 = view.findViewById<LinearLayout>(R.id.bodyLayout2)
        val arrowIcon2 = view.findViewById<ImageView>(R.id.arrowIcon2)
        val headerLayout3 = view.findViewById<LinearLayout>(R.id.headerLayout3)
        val bodyLayout3 = view.findViewById<LinearLayout>(R.id.bodyLayout3)
        val arrowIcon3 = view.findViewById<ImageView>(R.id.arrowIcon3)

        bodyLayout.visibility = View.GONE
        bodyLayout2.visibility = View.GONE
        bodyLayout3.visibility = View.GONE


        headerLayout.setOnClickListener {

            headerLayout2.isEnabled = false
            headerLayout3.isEnabled = false

            if (!isExpanded){
                if (isExpanded2) {
                    collapsePanel(parentContainer, bodyLayout2, arrowIcon2)
                    isExpanded2 = false
                }
                if (isExpanded3) {
                    collapsePanel(parentContainer, bodyLayout3, arrowIcon3)
                    isExpanded3 = false
                }
            }
            isExpanded = !isExpanded
            TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply { duration = 130 })
            bodyLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            arrowIcon.animate().rotation(if (isExpanded) 180f else 0f).setDuration(250).withEndAction {
                // По окончании анимации возвращаем клики
                headerLayout2.isEnabled = true
                headerLayout3.isEnabled = true
            }.start()
        }

        headerLayout2.setOnClickListener {

            headerLayout.isEnabled = false
            headerLayout3.isEnabled = false

            if (!isExpanded2){
                if (isExpanded) {
                    collapsePanel(parentContainer, bodyLayout, arrowIcon)
                    isExpanded = false
                }
                if (isExpanded3) {
                    collapsePanel(parentContainer, bodyLayout3, arrowIcon3)
                    isExpanded3 = false
                }
            }
            isExpanded2 = !isExpanded2
            TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply { duration = 130 })
            bodyLayout2.visibility = if (isExpanded2) View.VISIBLE else View.GONE
            arrowIcon2.animate().rotation(if (isExpanded2) 180f else 0f).setDuration(250).withEndAction {
                // По окончании анимации возвращаем клики
                headerLayout.isEnabled = true
                headerLayout3.isEnabled = true
            }.start()
        }

        headerLayout3.setOnClickListener {

            headerLayout.isEnabled = false
            headerLayout2.isEnabled = false


            if (!isExpanded3){
                if (isExpanded){
                    collapsePanel(parentContainer, bodyLayout, arrowIcon)
                    isExpanded = false
                }
                if (isExpanded2){
                    collapsePanel(parentContainer, bodyLayout2, arrowIcon2)
                    isExpanded2 = false
                }
            }
            isExpanded3 = !isExpanded3
            TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply { duration = 130 })
            bodyLayout3.visibility = if (isExpanded3) View.VISIBLE else View.GONE
            arrowIcon3.animate().rotation(if (isExpanded3) 180f else 0f).setDuration(250).withEndAction {
                // По окончании анимации возвращаем клики
                headerLayout.isEnabled = true
                headerLayout2.isEnabled = true
            }.start()

        }




        val EXAMPLE_COUNTER = stringPreferencesKey("env")
        val exampleCounterFlow: Flow<String> = requireContext().dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[EXAMPLE_COUNTER] ?: "prod"
            }
//        val dataStoreString : String = runBlocking { exampleCounterFlow.first() }

        val radioButtonDevElem = view.findViewById<RadioButton>(R.id.radioButtonDev)
        val radioButtonProdElem = view.findViewById<RadioButton>(R.id.radioButtonProd)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupEnv)
        viewLifecycleOwner.lifecycleScope.launch {
            val dataStoreString = exampleCounterFlow.first()

            when (dataStoreString) {
                "dev" -> radioGroup.check(R.id.radioButtonDev)
                "prod" -> radioGroup.check(R.id.radioButtonProd)
                else -> radioGroup.check(R.id.radioButtonProd)
            }

            radioButtonDevElem.jumpDrawablesToCurrentState()
            radioButtonProdElem.jumpDrawablesToCurrentState()



            radioGroup.setOnCheckedChangeListener { _, checkedId ->

                InfoHolder.wasActivated = false
                InfoHolder.reset()
                when (checkedId) {
                    R.id.radioButtonDev -> {
                        viewLifecycleOwner.lifecycleScope.launch{
                            editEnv("dev")
                        }
                    }
                    R.id.radioButtonProd -> {
                        viewLifecycleOwner.lifecycleScope.launch{
                            editEnv("prod")
                        }
                    }
                }
            }


        }


        val txtelem = view.findViewById<TextView>(R.id.text_notifications)
        InfoHolder.errStringLiveData.observe(viewLifecycleOwner) { errString ->
            txtelem.text = errString ?: "Пока ошибок не было"
        }

//        sleep(300)


    }

    suspend fun generateQR(content: String, size: Int): Bitmap =
        withContext(Dispatchers.Default) {
            BarcodeEncoder().encodeBitmap(content, BarcodeFormat.QR_CODE, size, size)
    }


    private var timesClicked = 0

    fun onDeleteButtonClick(view: View) {
        if (timesClicked <5){
            timesClicked++
            return
        }
        else {
            timesClicked = 0
        }

        showConfirmationDialog(requireContext()) {
            // Здесь выполняем удаление, если пользователь подтвердил
            (activity as? MainActivity)?.deleteKeyIfExists("my_rsa_key")
            requireView().findViewById<TextView>(R.id.textViewCurrentPublicKey).text = "Текущий публичный ключ: ❓"
            binding.qrImageView.visibility = View.GONE
            Toast.makeText(view.context, "Ключ удалён", Toast.LENGTH_SHORT).show()
        }
    }


    fun showConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите удалить?")
            .setPositiveButton("Да") { dialog, which ->
                onConfirm()
            }
            .setNegativeButton("Нет") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}