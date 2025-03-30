package com.example.diplomamobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object InfoHolder {
    var wasActivated: Boolean = false


    private val _isConnected = MutableLiveData<Boolean?>()
    val isConnectedLiveData : LiveData<Boolean?> get() = _isConnected
    var isConnected: Boolean?
        get() = _isConnected.value
        set(value) { _isConnected.postValue(value) }

    private val _isServerOnline = MutableLiveData<Boolean?>()
    val isServerOnlineLiveData : LiveData<Boolean?> get() = _isServerOnline
    var isServerOnline: Boolean?
        get() = _isServerOnline.value
        set(value) { _isServerOnline.postValue(value) }

    private val _isTokenRegistered = MutableLiveData<Boolean?>()
    val isTokenRegisteredLiveData : LiveData<Boolean?> get() = _isTokenRegistered
    var isTokenRegistered: Boolean?
        get() = _isTokenRegistered.value
        set(value) { _isTokenRegistered.postValue(value) }

    private val _isTokenActive = MutableLiveData<Boolean?>()
    val isTokenActiveLiveData : LiveData<Boolean?> get() = _isTokenActive
    var isTokenActive: Boolean?
        get() = _isTokenActive.value
        set(value) { _isTokenActive.postValue(value) }



    private val _errString = MutableLiveData<String?>()
    val errStringLiveData : LiveData<String?> get() = _errString
    var errString: String?
        get() = _errString.value
        set(value) { _errString.postValue(value) }



    private val _historyString = MutableLiveData<List<HistoryRecord>?>(null)
    val historyStringLiveData : LiveData<List<HistoryRecord>?> get() = _historyString
    var historyString: List<HistoryRecord>?
        get() = _historyString.value
        set(value) { _historyString.postValue(value) }


//    fun updateHistory(newHistory: List<HistoryRecord>?) {
//        historyStringLiveData = newHistory
//    }
//
//    fun resetHistory() {
//        historyStringLiveData = null
//    }



    val homeErrLiveData = MutableLiveData(HomeErrStrings())
    fun updateHomeErr(labelText : String, labelEmoji: String = "❓") {
        homeErrLiveData.postValue(HomeErrStrings(labelText, labelEmoji))
    }

    fun reset(resetStatusStrings: Boolean = true) {
        isConnected = null
        isServerOnline = null
        isTokenRegistered = null
        isTokenActive = null
        errString = null
        historyString = null
        if (resetStatusStrings)
            homeErrLiveData.postValue(HomeErrStrings())
    }


}

data class HistoryRecord(
    val datetime: String,
    val ip: String,
    val msg: String
)

data class HistoryResponse(
    val history: List<HistoryRecord>
)

data class HomeErrStrings(
    val errMainText: String = "Пошлите запрос",
    val errEmoji: String = "❓"
)