package com.genzfinance.manager.data.model

import java.util.Date

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val description: String = "",
    val date: Date = Date(),
    val userId: String = "" // For Firebase user association
) {
    // Empty constructor for Firebase
    constructor() : this(id = "", amount = 0.0)
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionCategory(val displayName: String) {
    FOOD("Food & Dining"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    TRANSPORT("Transportation"),
    BILLS("Bills & Utilities"),
    EDUCATION("Education"),
    HEALTH("Health & Wellness"),
    SALARY("Salary"),
    INVESTMENT("Investment"),
    OTHER("Other")
}
