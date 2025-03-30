package com.example.diplomamobile.ui.dashboard

import android.R.attr.bitmap
import android.R.attr.content
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diplomamobile.InfoHolder
import com.example.diplomamobile.MainActivity
import com.example.diplomamobile.R
import com.example.diplomamobile.databinding.FragmentDashboardBinding
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.KeyStore
import java.security.MessageDigest


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }


        return root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_top).visibility = if (InfoHolder.wasActivated) View.GONE else View.VISIBLE
        val tableRowVisibility = if (InfoHolder.wasActivated) View.VISIBLE else View.GONE
        listOf(R.id.tableRow1, R.id.tableRow2, R.id.tableRow3, R.id.tableRow4).forEach { id ->
            view.findViewById<TableRow>(id).visibility = tableRowVisibility
        }

        InfoHolder.isConnectedLiveData.observe(viewLifecycleOwner) { status ->
//            println("adsadasda")
            view.findViewById<TextView>(R.id.textViewIsConnected).text = when (status) {
                true -> "✅"
                false -> "❌"
                null -> "❓"
            }
        }

        InfoHolder.isServerOnlineLiveData.observe(viewLifecycleOwner) { status ->
            view.findViewById<TextView>(R.id.textViewIsServerOnline).text = when (status) {
                true -> "✅"
                false -> "❌"
                null -> "❓"
            }
        }

        InfoHolder.isTokenRegisteredLiveData.observe(viewLifecycleOwner) { status ->
            view.findViewById<TextView>(R.id.textViewIsTokenRegistered).text = when (status) {
                true -> "✅"
                false -> "❌"
                null -> "❓"
            }
        }

        InfoHolder.isTokenActiveLiveData.observe(viewLifecycleOwner) { status ->
            view.findViewById<TextView>(R.id.textViewIsTokenActive).text = when (status) {
                true -> "✅"
                false -> "❌"
                null -> "❓"
            }
        }


        val txtelem = view.findViewById<TextView>(R.id.txtViewFingerprint)
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val publicKey = keyStore.getCertificate("my_rsa_key")?.publicKey
        var keyImprint : String? = null
        if (publicKey != null) {
            val pemObject = PemObject("PUBLIC KEY", publicKey.encoded)
            val writer = StringWriter()
            PemWriter(writer).use { it.writeObject(pemObject) }
            val pemString = writer.toString()
            val cleanedInput = pemString.replace(Regex("""(\r\n)|\n"""), "")
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(cleanedInput.toByteArray(Charsets.UTF_8))
            val first6Bytes = digest.take(6)
            keyImprint = first6Bytes.joinToString(" ") { String.format("%02X", it) }
        }
        txtelem.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (keyImprint == null) 34f else 19f )
        txtelem.text =  keyImprint ?: "❓"
        txtelem.requestLayout()


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        InfoHolder.historyStringLiveData.observe(viewLifecycleOwner) { history ->
            view.findViewById<LinearLayout>(R.id.linearLayoutHeader).visibility = if (history != null) View.VISIBLE else View.GONE
            if (history != null) {
                recyclerView.adapter = MainActivity.HistoryAdapter(history)
            } else {
                recyclerView.adapter = null
            }
        }



//        val statusMapping = mapOf(
//            R.id.textViewIsConnected to InfoHolder.isConnected,
//            R.id.textViewIsServerOnline to InfoHolder.isServerOnline,
//            R.id.textViewIsTokenRegistered to InfoHolder.isTokenRegistered,
//            R.id.textViewIsTokenActive to InfoHolder.isTokenActive
//        )
//
//        statusMapping.forEach { (id, status) ->
//            view.findViewById<TextView>(id).text = when (status) {
//                true -> "✅"
//                false -> "❌"
//                null -> "❓"
//            }
//        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}