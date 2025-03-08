package com.genzfinance.manager.data.repository

import com.genzfinance.manager.data.model.Transaction
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    private val transactionsRef = database.getReference("transactions")

    suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val key = transactionsRef.push().key ?: return Result.failure(Exception("Failed to generate key"))
            val newTransaction = transaction.copy(id = key)
            transactionsRef.child(key).setValue(newTransaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionsRef.child(transaction.id).setValue(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            transactionsRef.child(transactionId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTransactionsFlow(userId: String): Flow<List<Transaction>> = flow {
        try {
            val snapshot = transactionsRef
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()
            
            val transactions = snapshot.children.mapNotNull { 
                it.getValue<Transaction>() 
            }
            emit(transactions)
        } catch (e: Exception) {
            // In a real app, you might want to emit an error state or handle errors differently
            emit(emptyList())
        }
    }

    suspend fun getTransactionById(transactionId: String): Result<Transaction> {
        return try {
            val snapshot = transactionsRef.child(transactionId).get().await()
            val transaction = snapshot.getValue<Transaction>()
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.failure(Exception("Transaction not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Result<List<Transaction>> {
        return try {
            val snapshot = transactionsRef
                .orderByChild("date")
                .startAt(startDate.toDouble())
                .endAt(endDate.toDouble())
                .get()
                .await()

            val transactions = snapshot.children
                .mapNotNull { it.getValue<Transaction>() }
                .filter { it.userId == userId }

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
