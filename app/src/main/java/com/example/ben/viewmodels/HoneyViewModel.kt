package com.example.ben.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ben.models.HoneyRecord
import com.example.ben.repositories.DataRepository
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HoneyViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _records = MutableLiveData<List<HoneyRecord>>()
    val records: LiveData<List<HoneyRecord>> = _records

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    fun fetchRecords() {
        val uid = FirebaseUtils.currentUserUid ?: return
        repository.getHoneyRecords(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HoneyRecord>()
                for (shot in snapshot.children) {
                    shot.getValue(HoneyRecord::class.java)?.let { list.add(0, it) }
                }
                _records.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                _status.value = "Error: ${error.message}"
            }
        })
    }

    fun saveRecord(record: HoneyRecord) {
        repository.saveHoneyRecord(record).addOnCompleteListener {
            if (it.isSuccessful) _status.value = "Record saved"
            else _status.value = "Failed: ${it.exception?.message ?: "Unknown error"}"
        }
    }

    fun deleteRecord(recordId: String) {
        val uid = FirebaseUtils.currentUserUid ?: return
        repository.deleteHoneyRecord(uid, recordId).addOnCompleteListener {
            if (it.isSuccessful) _status.value = "Record deleted"
            else _status.value = "Failed to delete"
        }
    }
}
