package com.example.diplomamobile

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.diplomamobile.databinding.ActivityMainBinding
import io.fusionauth.jwt.Signer
import io.fusionauth.jwt.domain.Algorithm
import io.fusionauth.jwt.domain.JWT
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.time.ZonedDateTime
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView

import java.security.MessageDigest

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

import org.json.JSONObject
//import com.example.diplomamobile.ui.notifications.NotificationsViewModel

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настраиваем навигацию
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Инициализируем BouncyCastle, если он ещё не добавлен
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }



        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        lateinit var sharedPref: SharedPreferences

        try {
            sharedPref = EncryptedSharedPreferences.create(
                this,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            this.deleteSharedPreferences("secret_shared_prefs")

            sharedPref = EncryptedSharedPreferences.create(
                this,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        val token = sharedPref.getString("token", "") ?: ""

//        val txtTicket = findViewById<EditText>(R.id.txtTicket)
//
//        txtTicket.setText(token)

//        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

    }

//    var notificationsViewModel : NotificationsViewModel? = null



    fun parseHistoryRecords(jsonString: String): List<HistoryRecord> {
        val response = Gson().fromJson(jsonString, HistoryResponse::class.java)

        return response.history.map { record ->
            // Форматируем поле datetime. Если формат отличается, настройте форматтер.
            val formattedDatetime = try {
                val zdt = ZonedDateTime.parse(record.datetime)
                val deviceZone = ZoneId.systemDefault()
                val deviceZdt = zdt.withZoneSameInstant(deviceZone)
                // Пример формата: "dd.MM.yyyy HH:mm"
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                deviceZdt.format(formatter)
            } catch (e: Exception) {
                record.datetime
            }
            // Заменяем значение msg:
            val newMsg = when (record.msg) {
                "SUCCESS" -> "✅"
                "ERR" -> "❌"
                else -> record.msg
            }
            record.copy(datetime = formattedDatetime, msg = newMsg)
        }

    }

    class HistoryAdapter(private val historyList: List<HistoryRecord>) :
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val datetimeTextView: TextView = itemView.findViewById(R.id.textViewDatetime)
            val ipTextView: TextView = itemView.findViewById(R.id.textViewIP)
            val msgTextView: TextView = itemView.findViewById(R.id.textViewMsg)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val record = historyList[position]
            holder.datetimeTextView.text = record.datetime
            holder.ipTextView.text = record.ip
            holder.msgTextView.text = record.msg
        }

        override fun getItemCount() = historyList.size
    }



    fun generateRSAKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )
        val parameterSpec = KeyGenParameterSpec.Builder(
            "my_rsa_key",
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setUserAuthenticationRequired(true)
//      .setUserAuthenticationParameters(60, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .setKeySize(2048)
            .build()

        keyPairGenerator.initialize(parameterSpec)
        return keyPairGenerator.generateKeyPair()
    }

    fun getStoredKeyPair(): KeyPair? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateKey = keyStore.getKey("my_rsa_key", null) as? PrivateKey
        val publicKey = keyStore.getCertificate("my_rsa_key")?.publicKey
        return if (privateKey != null && publicKey != null) KeyPair(publicKey, privateKey) else null
    }

    fun exportPublicKeyToSingleLinePEM(publicKey: PublicKey): String {
        val pemObject = PemObject("PUBLIC KEY", publicKey.encoded)
        val writer = StringWriter()
        PemWriter(writer).use { it.writeObject(pemObject) }
        val pemString = writer.toString()

        val cleanedInput = pemString.replace(Regex("""(\r\n)|\n"""), "")

        return cleanedInput
    }

    fun authenticateAndSignJWT(
        activity: AppCompatActivity,
        keyPairArg: KeyPair,
        ticketStr: String,
        pinTxt: String,
        callback: (String?) -> Unit
    ) {
        val signature =
            Signature.getInstance("SHA256withRSA").apply { initSign(keyPairArg.private) }
        val cryptoObject = BiometricPrompt.CryptoObject(signature)
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val unlockedSignature = result.cryptoObject?.signature ?: run {
                        callback(null)
                        return
                    }

                    val signer = object : Signer {
                        override fun getAlgorithm(): Algorithm = Algorithm.RS256
                        override fun sign(payload: String): ByteArray {
                            unlockedSignature.update(payload.toByteArray(Charsets.UTF_8))
                            return unlockedSignature.sign()
                        }

                        override fun getKid(): String? = null
                    }

//                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-DDThh:mm:ss+HH:MM")
                    val dateTime = ZonedDateTime.now(ZoneId.of("GMT+3"))
                    val formatted = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                    val jwt = JWT().apply {
                        addClaim("version", getString(R.string.app_ver_string))
                        addClaim("request_time", formatted)
                        addClaim("public_key", exportPublicKeyToSingleLinePEM(keyPairArg.public))
                        addClaim("pin", pinTxt)
                        addClaim("ticket", ticketStr)
                    }
                    callback("{\"token\": \"${JWT.getEncoder().encode(jwt, signer)}\"}")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    callback(null)
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Подтвердите личность")
            .setSubtitle("Используйте биометрию для подтверждения")
            .setNegativeButtonText("Отмена")
            .build()
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }


    fun getFirst6BytesHash(input: String): String {
        val cleanedInput = input.replace(Regex("""(\r\n)|\n"""), "")
        val digest =
            MessageDigest.getInstance("SHA-256").digest(cleanedInput.toByteArray(Charsets.UTF_8))
        val first6Bytes = digest.take(6)
        return first6Bytes.joinToString(" ") { String.format("%02X", it) }
    }

    fun deleteKeyIfExists(alias: String) {
        InfoHolder.reset()
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)

            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            this.deleteSharedPreferences("secret_shared_prefs")
            val sharedPref = EncryptedSharedPreferences.create(
                this,
                "secret_shared_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            sharedPref.edit { putString("token-dev", "") }
            sharedPref.edit { putString("token-prod", "") }
        }
    }


    fun isInternetAvaliable() : Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        return isConnected
    }


    fun getSharedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val sharedPref = EncryptedSharedPreferences.create(
            this,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return  sharedPref
    }


    fun makePinHash(pinTxt: String, keyImprint: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest((pinTxt + keyImprint).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }


    fun getEnv(context: Context): String {
        val EXAMPLE_COUNTER = stringPreferencesKey("env")
        val exampleCounterFlow: Flow<String> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[EXAMPLE_COUNTER] ?: "prod"
            }
        return runBlocking { exampleCounterFlow.first() }
    }

    fun getUrl(context: Context) : String {

        val EXAMPLE_COUNTER = stringPreferencesKey("env")
        val exampleCounterFlow: Flow<String> = context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[EXAMPLE_COUNTER] ?: "prod"
            }

        var url = "zaqzxcswsde.ru"
        var protocol = "https"
        var port = ""

        if (getEnv(context) == "dev") {
            url = "192.168.1.67"
            protocol = "http"
            port = "8000"
        }

        return "$protocol://$url${if (port.length > 0) ":" else ""}$port"
    }


    fun getErrorResponse(responseBody: String) {

        var isTokenRegistered : Boolean? = null
        var isTokenActive : Boolean? = null

        var toastText = "Неизвестная ошибка"

        if (responseBody.contains("unsupported app version")){
            toastText = "Устаревшее приложение"
        }

        if (responseBody.contains("that token is not registered in the database")){
            toastText = "Токена нет в базе"
            isTokenRegistered = false
        }
        else isTokenRegistered = true

//        sleep(300)
        if (responseBody.contains("that token is not active")){
            toastText = "Токен неактивен"
            if (isTokenRegistered == true) isTokenActive = false
        }
        else if (isTokenRegistered == true) isTokenActive = true

        if (responseBody.contains("ticket has expired") || responseBody.contains("unsupported ticket version")){
            toastText = "Слишком старый тикет"
        }

        if (responseBody.contains("flow control error")){
            toastText = "Подозрительная активность, токен самоуничтожен"
            deleteKeyIfExists("my_rsa_key")
        }

        if (responseBody.contains("not a valid IP")){
            toastText = "Неразрешённый IP"
        }

        if (responseBody.contains("pin is invalid")){
            toastText = "Неправильный PIN-код"
        }

        InfoHolder.isTokenRegistered = isTokenRegistered
        InfoHolder.isTokenActive = isTokenActive

        InfoHolder.updateHomeErr(toastText, "❌")
//        runOnUiThread {
//            Toast.makeText(this@MainActivity, "$toastText ❌", Toast.LENGTH_LONG).show()
//        }

    }


//    fun setStatusTextAndLabel(labelText: String, labelEmoji: String = "❓") {
//
//        val isLoading : Boolean =  labelEmoji.isEmpty()
//
//        val loadingBarElem = findViewById<ProgressBar>(R.id.progressBarLoading)
//        val txtViewStatusElem = findViewById<TextView>(R.id.textViewStat)
//        val textViewStatusLabel = findViewById<TextView>(R.id.textViewStatusLabel)
//
//        loadingBarElem.visibility = if (isLoading) View.VISIBLE else View.GONE
//        txtViewStatusElem.visibility = if (isLoading) View.GONE else View.VISIBLE
//
//        textViewStatusLabel.text = labelText
//        txtViewStatusElem.text = labelEmoji
//    }



//    val cardView = findViewById<MaterialCardView>(R.id.MaterialCardViewStatus)
//
//    val animator = ObjectAnimator.ofFloat(cardView, "translationX", -20f, 20f).apply {
//        duration = 500                // длительность одного цикла
//        repeatMode = ValueAnimator.REVERSE
//        repeatCount = ValueAnimator.INFINITE
//    }


    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private var isRequestInProgress = false

    fun btnTestClick(view: View?) {

        if (isRequestInProgress){
            val cardView = findViewById<MaterialCardView>(R.id.MaterialCardViewStatus)

            cardView.translationX = 50f
            val springAnim = SpringAnimation(cardView, DynamicAnimation.TRANSLATION_X, 0f)
            springAnim.spring.stiffness = SpringForce.STIFFNESS_HIGH //   // можно поэкспериментировать: VERY_LOW, LOW, MEDIUM, HIGH
            springAnim.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY // или DAMPING_RATIO_LOW_BOUNCY для более выраженных колебаний
            springAnim.start()

            return
        }

        InfoHolder.reset(resetStatusStrings=false)
        InfoHolder.wasActivated = true

        if (!isInternetAvaliable()){

            InfoHolder.updateHomeErr("No internet connection", "❌")
//            setStatusTextAndLabel("No internet connection", "❌")
//            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show()
            InfoHolder.isConnected = false
            return
        }
        else
            InfoHolder.isConnected = true


        val editTextTextPassword = findViewById<TextView>(R.id.editTextTextPassword)
        val pinTxtStr = editTextTextPassword.text.toString()
        editTextTextPassword.text = ""
        hideKeyboard(this)
        editTextTextPassword.clearFocus()

        if (pinTxtStr.length < 10) {

            InfoHolder.updateHomeErr("Min 10 characters for pin", "❌")
//            setStatusTextAndLabel("Min 10 characters for pin", "❌")
//            Toast.makeText(this, "Min 10 characters for pin", Toast.LENGTH_SHORT).show()
            return
        }

        val keyPair = getStoredKeyPair() ?: generateRSAKeyPair()

//        val txt = findViewById<TextView>(R.id.text_home)

        val pinDigest = makePinHash(pinTxtStr, getFirst6BytesHash(exportPublicKeyToSingleLinePEM(keyPair.public)))

        val token = getSharedPrefs().getString("token-${getEnv(view!!.context)}", "") ?: ""

        isRequestInProgress = true
        authenticateAndSignJWT(
            this,
            keyPair,
            ticketStr = token,
            pinDigest
        ) { jwt ->
            if (jwt != null) {

                    InfoHolder.updateHomeErr("Запрос в процессе", "")
//                setStatusTextAndLabel("Запрос в процессе", "")

                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jwt.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("${getUrl(view.context)}/mainrequest/")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        isRequestInProgress = false
                        InfoHolder.isServerOnline = false
                        InfoHolder.errString = "Ошибка сети: ${e.message}"

//                        Log.e("NetworkRequest", "Ошибка сети: ${e.message}")
                        InfoHolder.updateHomeErr("Сервер недоступен", "❌")
//                        runOnUiThread {
//                            Toast.makeText(this@MainActivity, "Сервер недоступен", Toast.LENGTH_SHORT).show()
//                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        isRequestInProgress = false
                        InfoHolder.isServerOnline = true
                        val responseCode = response.code
                        val responseBody = response.body?.string() ?: ""

                        if (responseCode == 502) {
                            InfoHolder.isServerOnline = false
                            InfoHolder.updateHomeErr("Сервер недоступен", "❌")
//                            runOnUiThread {
//                                Toast.makeText(this@MainActivity, "Сервер недоступен", Toast.LENGTH_LONG).show()
//                            }
                            return
                        }


                        if (responseCode == 500) {
                            InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"
                            InfoHolder.updateHomeErr("Неизвестная ошибка сервера", "❌")
//                            runOnUiThread {
//                                Toast.makeText(this@MainActivity, "Неизвестная ошибка сервера", Toast.LENGTH_LONG).show()
//                            }
                            return
                        }

                        if (responseBody.isEmpty()) {
                            InfoHolder.updateHomeErr("Неизвестная ошибка сервера", "❌")
                            InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"
//                            runOnUiThread {
//                                Toast.makeText(this@MainActivity, "Пустой ответ от сервера", Toast.LENGTH_LONG).show()
//                            }
                            return
                        }

                        val jsonObject: JSONObject?
                        try {
                            jsonObject = JSONObject(responseBody)
                        } catch (e: JSONException) {
                            InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"
                            InfoHolder.updateHomeErr("Неожиданный ответ от сервера", "❌")
//                            runOnUiThread {
//                                Toast.makeText(this@MainActivity, "Неожиданный ответ от сервера", Toast.LENGTH_LONG).show()
//                            }
                            return
                        }

                        if (responseCode == 200) {
                            val ticket = jsonObject.optString("ticket", "")
                            if (ticket.isEmpty()) {
                                InfoHolder.updateHomeErr("Неизвестная ошибка сервера", "❌")
                                InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"
                            }
                            getSharedPrefs().edit { putString("token-${getEnv(view.context)}", ticket) }
                            InfoHolder.isTokenRegistered = true
                            InfoHolder.isTokenActive = true
                            InfoHolder.updateHomeErr("Успешное подтверждение", "✅")
//                            InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"

                            val historyData = jsonObject.optString("history", "")
                            if (historyData.isNotEmpty()) {
                                InfoHolder.historyString = parseHistoryRecords( "{\"history\": $historyData}")
                            }

                            return
                        }

                        if (responseCode == 400) {
                            InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"
                            getErrorResponse(responseBody)

                            return
                        }

                        InfoHolder.updateHomeErr("Неизвестный код ответа", "❌")
//                        runOnUiThread {
//                            Toast.makeText(this@MainActivity, "Неизвестный код ответа", Toast.LENGTH_LONG).show()
//                        }
                        InfoHolder.errString = "\nКод ответа: $responseCode\nОтвет: $responseBody"
                    }
                })
            }
            else {
                InfoHolder.updateHomeErr("Ошибка биометрической\nаутентификации", "❌")
                isRequestInProgress = false
//                Toast.makeText(this, "Ошибка биометрической аутентификации", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun txtClick(view: View?) {
        val textView = view as? TextView ?: return
        var textToCopy = textView.text.toString()

        val prefixes = arrayOf("Текущий публичный ключ: ")

        val prefix = prefixes.find { textToCopy.startsWith(it) }

        if (!prefix.isNullOrEmpty()) {
            textToCopy = textToCopy.removePrefix(prefix)
        }

        val clipboard =
            textView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", textToCopy)
        clipboard.setPrimaryClip(clip)

    }


//    fun clck1(view: View) {
//        val txtelem = findViewById<TextView>(R.id.txtViewFingerprint)
//        val currentSizePx = txtelem.textSize
//        val currentSizeSp = currentSizePx / resources.displayMetrics.scaledDensity
//        val newSizeSp = currentSizeSp + 1f
//        txtelem.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSizeSp)
//    }
//
//    fun clck2(view: View) {
//        val txtelem = findViewById<TextView>(R.id.txtViewFingerprint)
//        val currentSizePx = txtelem.textSize
//        val currentSizeSp = currentSizePx / resources.displayMetrics.scaledDensity
//        val newSizeSp = currentSizeSp - 1f
//        txtelem.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSizeSp)
//    }

}
