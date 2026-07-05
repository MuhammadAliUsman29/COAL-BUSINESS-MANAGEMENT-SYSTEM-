package com.example.myapplication55.data.repository

import com.example.myapplication55.data.local.dao.*
import com.example.myapplication55.data.local.entities.*
import kotlinx.coroutines.flow.*

class CmsRepository(
    private val coalStockDao: CoalStockDao,
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val purchaseHistoryDao: PurchaseHistoryDao
) {
    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    fun setCurrentUserId(id: Long?) {
        _currentUserId.value = id
    }

    val allStocks: Flow<List<CoalStock>> = _currentUserId.flatMapLatest { id ->
        if (id != null) coalStockDao.getAllStocks(id) else flowOf(emptyList())
    }
    
    val allCustomers: Flow<List<Customer>> = _currentUserId.flatMapLatest { id ->
        if (id != null) customerDao.getAllCustomers(id) else flowOf(emptyList())
    }
    
    val allTransactions: Flow<List<Transaction>> = _currentUserId.flatMapLatest { id ->
        if (id != null) transactionDao.getAllTransactions(id) else flowOf(emptyList())
    }
    
    val totalStockQuantity: Flow<Double?> = _currentUserId.flatMapLatest { id ->
        if (id != null) coalStockDao.getTotalQuantity(id) else flowOf(0.0)
    }
    
    val totalCustomerBalance: Flow<Double?> = _currentUserId.flatMapLatest { id ->
        if (id != null) {
            allTransactions.map { transactions ->
                transactions.filter { it.userId == id }.sumOf { trans ->
                    when (trans.type) {
                        TransactionType.CASH_IN -> trans.amount
                        TransactionType.CASH_OUT -> -trans.amount
                        TransactionType.STOCK_OUT -> {
                            // Confirmed Received cash from sales
                            if (trans.paymentStatus == PaymentStatus.PAID) trans.amount else 0.0
                        }
                        TransactionType.STOCK_IN -> -trans.amount // Buying stock is a cash outflow
                    }
                }
            }
        } else flowOf(0.0)
    }

    val totalPendingRecovery: Flow<Double?> = _currentUserId.flatMapLatest { id ->
        if (id != null) {
            allTransactions.map { transactions ->
                transactions.filter { it.userId == id && it.paymentStatus == PaymentStatus.PENDING }
                    .sumOf { it.amount }
            }
        } else flowOf(0.0)
    }
    
    val currentUser: Flow<User?> = _currentUserId.flatMapLatest { id ->
        if (id != null) userDao.getUserFlow(id) else flowOf(null)
    }

    val allUsers: Flow<List<User>> = activityLogDao.getAllLogs().flatMapLatest {
        userDao.getAllUsers()
    }

    val allActivityLogs: Flow<List<ActivityLog>> = activityLogDao.getAllLogs()

    fun getLogsByUser(userId: Long): Flow<List<ActivityLog>> = activityLogDao.getLogsByUser(userId)

    val purchaseHistory: Flow<List<PurchaseHistory>> = _currentUserId.flatMapLatest { id ->
        if (id != null) purchaseHistoryDao.getAllPurchases(id) else flowOf(emptyList())
    }

    suspend fun logActivity(action: String, details: String) {
        val userId = _currentUserId.value ?: 0
        activityLogDao.insertLog(ActivityLog(userId = userId, action = action, details = details))
    }

    suspend fun getUserByCnic(cnic: String) = userDao.getUserByCnic(cnic)
    suspend fun saveUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun getUser(): User? {
        val id = _currentUserId.value ?: return null
        return userDao.getUserById(id)
    }

    suspend fun masterReset() {
        val userId = _currentUserId.value ?: return
        allTransactions.first().forEach { trans ->
            trans.imagePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {}
            }
        }
        transactionDao.deleteAllTransactions()
        customerDao.deleteAllCustomers()
        coalStockDao.deleteAllStocks()
        userDao.getUserById(userId)?.let { user ->
            userDao.updateUser(user.copy(balance = 0.0))
        }
    }

    suspend fun resetCustomerData(customerId: Long) {
        transactionDao.getTransactionsByCustomer(customerId).first().forEach { trans ->
            trans.imagePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {}
            }
        }
        transactionDao.deleteTransactionsByCustomer(customerId)
        customerDao.getCustomerById(customerId)?.let { customer ->
            customerDao.updateCustomer(customer.copy(totalBalance = 0.0))
        }
    }

    suspend fun insertStock(stock: CoalStock) = coalStockDao.insertStock(stock.copy(userId = _currentUserId.value ?: 0))
    suspend fun updateStock(stock: CoalStock) = coalStockDao.updateStock(stock)
    suspend fun deleteStock(stock: CoalStock) {
        transactionDao.getTransactionsByStock(stock.id).first().forEach { trans ->
            trans.imagePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {}
            }
        }
        transactionDao.deleteTransactionsByStock(stock.id)
        coalStockDao.deleteStock(stock)
    }

    suspend fun deletePurchase(purchase: PurchaseHistory) {
        val userId = _currentUserId.value ?: return
        
        // 1. Revert user balance (add spent amount back)
        userDao.getUserById(userId)?.let { user ->
            userDao.updateUser(user.copy(balance = user.balance + purchase.total_cost))
        }

        // 2. Deduct quantity from stock
        allStocks.first().find { it.origin == purchase.item_name }?.let { stock ->
            coalStockDao.updateStock(stock.copy(quantity = (stock.quantity - purchase.quantity).coerceAtLeast(0.0)))
        }

        // 3. Remove from purchase history
        purchaseHistoryDao.deletePurchase(purchase)

        logActivity("DELETE_PURCHASE", "Reverted purchase of ${purchase.quantity} KG ${purchase.item_name}")
    }

    fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>> = transactionDao.getTransactionsByCustomer(customerId)

    suspend fun getTransactionsInRange(start: Long, end: Long) = transactionDao.getTransactionsInRange(_currentUserId.value ?: 0, start, end)
    suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
    suspend fun getStockById(id: Long) = coalStockDao.getStockById(id)

    suspend fun insertInitialData() {
        val userId = _currentUserId.value ?: return
        // Insert some customers
        val c1 = Customer(userId = userId, name = "HARLAN COAL CORP", phoneNumber = "+1 (555) 012-4492", totalBalance = 482930.0)
        val c2 = Customer(userId = userId, name = "STEELWORKS LTD", phoneNumber = "+1 (555) 883-9921", totalBalance = -15000.0)
        val c3 = Customer(userId = userId, name = "APEX LOGISTICS", phoneNumber = "+1 (555) 443-1102", totalBalance = 5000.0)
        
        customerDao.insertCustomer(c1)
        customerDao.insertCustomer(c2)
        customerDao.insertCustomer(c3)
        
        // Insert some stocks
        val s1 = CoalStock(userId = userId, origin = "Indonesia", moistureLevel = "++", quantity = 1420.5, qualityGrade = "GAR 4200", totalCapacity = 2000.0)
        val s2 = CoalStock(userId = userId, origin = "South Africa", moistureLevel = "--", quantity = 850.0, qualityGrade = "RB1", totalCapacity = 1500.0)
        
        coalStockDao.insertStock(s1)
        coalStockDao.insertStock(s2)

        userDao.getUserById(userId)?.let { user ->
            userDao.updateUser(user.copy(balance = 500000.0))
        }
    }

    suspend fun checkAndPrepopulate() {
        _currentUserId.value?.let { userId ->
            if (customerDao.getCustomerCount(userId) == 0) {
                insertInitialData()
            }
        }
    }

    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer.copy(userId = _currentUserId.value ?: 0))
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) {
        val transactions = transactionDao.getTransactionsByCustomer(customer.id).first()
        transactions.forEach { trans ->
            trans.imagePath?.let { path ->
                try {
                    val file = java.io.File(path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {}
            }
        }
        transactionDao.deleteTransactionsByCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        val userId = _currentUserId.value ?: return
        transaction.imagePath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists()) file.delete()
            } catch (e: Exception) { }
        }
        when (transaction.type) {
            TransactionType.STOCK_IN -> {
                transaction.stockId?.let { id ->
                    coalStockDao.getStockById(id)?.let { stock ->
                        // Reconciliation: Deduct quantity that was added
                        coalStockDao.updateStock(stock.copy(quantity = stock.quantity - transaction.quantity))
                        // Revert user and supplier (customer) cash outflow
                        userDao.getUserById(userId)?.let { user ->
                            userDao.updateUser(user.copy(balance = user.balance + transaction.amount))
                        }
                        transaction.customerId?.let { cid ->
                            customerDao.getCustomerById(cid)?.let { customer ->
                                customerDao.updateCustomer(customer.copy(
                                    totalBalance = customer.totalBalance + transaction.amount
                                ))
                            }
                        }
                    }
                }
            }
            TransactionType.STOCK_OUT -> {
                transaction.stockId?.let { id ->
                    coalStockDao.getStockById(id)?.let { stock ->
                        // Reconciliation: Add back quantity that was sold
                        coalStockDao.updateStock(stock.copy(quantity = stock.quantity + transaction.quantity))
                        // Revert cash inflow if it was PAID
                        if (transaction.paymentStatus == PaymentStatus.PAID) {
                            userDao.getUserById(userId)?.let { user ->
                                userDao.updateUser(user.copy(balance = user.balance - transaction.amount))
                            }
                        }
                    }
                }
                transaction.customerId?.let { cid ->
                    customerDao.getCustomerById(cid)?.let { customer ->
                        val isPending = transaction.paymentStatus == PaymentStatus.PENDING
                        // Segmented Logic:
                        // If PAID: Subtract from totalBalance (cash inflow removed)
                        // If PENDING: Subtract from pendingBalance (debt removed)
                        val newTotalBalance = if (isPending) customer.totalBalance else customer.totalBalance - transaction.amount
                        val newPendingBalance = if (isPending) (customer.pendingBalance - transaction.amount).coerceAtLeast(0.0) else customer.pendingBalance
                        
                        customerDao.updateCustomer(customer.copy(
                            totalBalance = newTotalBalance,
                            pendingBalance = newPendingBalance
                        ))
                    }
                }
            }
            TransactionType.CASH_IN -> {
                transaction.customerId?.let { id ->
                    customerDao.getCustomerById(id)?.let { customer ->
                        // Revert direct cash payment
                        customerDao.updateCustomer(customer.copy(
                            totalBalance = customer.totalBalance - transaction.amount,
                            pendingBalance = customer.pendingBalance + transaction.amount
                        ))
                        userDao.getUserById(userId)?.let { user ->
                            userDao.updateUser(user.copy(balance = user.balance - transaction.amount))
                        }
                    }
                }
            }
            TransactionType.CASH_OUT -> {
                transaction.customerId?.let { id ->
                    customerDao.getCustomerById(id)?.let { customer ->
                        // Revert lending
                        customerDao.updateCustomer(customer.copy(
                            totalBalance = customer.totalBalance + transaction.amount,
                            pendingBalance = (customer.pendingBalance - transaction.amount).coerceAtLeast(0.0)
                        ))
                        userDao.getUserById(userId)?.let { user ->
                            userDao.updateUser(user.copy(balance = user.balance + transaction.amount))
                        }
                    }
                }
            }
        }
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val oldTransaction = transactionDao.getTransactionById(transaction.id)
        val userId = _currentUserId.value ?: return

        transactionDao.insertTransaction(transaction)

        // Sync balances if amount or status changed
        if (oldTransaction != null) {
            val amountDiff = transaction.amount - oldTransaction.amount
            val statusChanged = oldTransaction.paymentStatus != transaction.paymentStatus
            val isPaidNow = transaction.paymentStatus == PaymentStatus.PAID
            
            // This is complex because changing amount AND status simultaneously is tricky.
            // Simplest approach: revert old, apply new.
            
            // Revert Old Balances
            userDao.getUserById(userId)?.let { user ->
                val revertedUserBalance = if (oldTransaction.paymentStatus == PaymentStatus.PAID) {
                    user.balance - oldTransaction.amount
                } else {
                    user.balance
                }
                
                // Apply New Balance
                val newUserBalance = if (isPaidNow) {
                    revertedUserBalance + transaction.amount
                } else {
                    revertedUserBalance
                }
                userDao.updateUser(user.copy(balance = newUserBalance))
            }

            customerDao.getCustomerById(transaction.customerId ?: 0)?.let { customer ->
                // Revert Old
                var tempTotal = customer.totalBalance
                var tempPending = customer.pendingBalance
                
                if (oldTransaction.paymentStatus == PaymentStatus.PAID) {
                    tempTotal -= oldTransaction.amount
                } else {
                    tempPending -= oldTransaction.amount
                }
                
                // Apply New
                if (isPaidNow) {
                    tempTotal += transaction.amount
                } else {
                    tempPending += transaction.amount
                }
                
                customerDao.updateCustomer(customer.copy(
                    totalBalance = tempTotal,
                    pendingBalance = tempPending.coerceAtLeast(0.0),
                    lastTransactionDate = System.currentTimeMillis()
                ))
            }
        }
        
        logActivity("UPDATE_TRANSACTION", "Updated transaction ID: ${transaction.id}")
    }

    suspend fun markTransactionAsPaid(transactionId: Long, receiptPath: String?) {
        val userId = _currentUserId.value ?: return
        val transaction = transactionDao.getTransactionById(transactionId) ?: return
        if (transaction.paymentStatus == PaymentStatus.PAID) return

        val updatedTransaction = transaction.copy(
            paymentStatus = PaymentStatus.PAID,
            receiptImagePath = receiptPath ?: transaction.receiptImagePath,
            timestamp = System.currentTimeMillis() // Update timestamp to payment time?
        )
        
        transactionDao.insertTransaction(updatedTransaction)

        // Update User Balance (Integrated Liquidity)
        userDao.getUserById(userId)?.let { user ->
            userDao.updateUser(user.copy(balance = user.balance + transaction.amount))
        }

        // Update Customer Balance
        transaction.customerId?.let { cid ->
            customerDao.getCustomerById(cid)?.let { customer ->
                customerDao.updateCustomer(customer.copy(
                    totalBalance = customer.totalBalance + transaction.amount,
                    pendingBalance = (customer.pendingBalance - transaction.amount).coerceAtLeast(0.0),
                    lastTransactionDate = System.currentTimeMillis()
                ))
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val userId = _currentUserId.value ?: 0
        val finalTransaction = transaction.copy(userId = userId)

        if (finalTransaction.amount < 0 || finalTransaction.quantity < 0) {
            throw IllegalArgumentException("Transaction amount or quantity cannot be negative")
        }

        when (finalTransaction.type) {
            TransactionType.STOCK_IN -> {
                finalTransaction.stockId?.let { id ->
                    coalStockDao.getStockById(id)?.let { stock ->
                        if (stock.quantity + finalTransaction.quantity > stock.totalCapacity) {
                            throw IllegalStateException("Insufficient storage capacity for this quantity.")
                        }
                        
                        // Integrated Liquidity: Subtract purchase cost from User balance
                        val cost = finalTransaction.amount
                        val currentUser = userDao.getUserById(userId)
                        if (currentUser != null && currentUser.balance < cost) {
                            throw IllegalStateException("Insufficient Balance for this transaction.")
                        }

                        coalStockDao.updateStock(stock.copy(quantity = stock.quantity + finalTransaction.quantity))
                        
                        if (currentUser != null) {
                            userDao.updateUser(currentUser.copy(balance = currentUser.balance - cost))
                        }

                        // NEW: Record in PurchaseHistory
                        purchaseHistoryDao.insertPurchase(
                            PurchaseHistory(
                                userId = userId,
                                item_name = stock.origin,
                                quantity = finalTransaction.quantity,
                                unit_price = finalTransaction.pricePerUnit,
                                total_cost = cost,
                                image_url = finalTransaction.imagePath
                            )
                        )

                        // Also subtract from Supplier (Customer) cash record
                        finalTransaction.customerId?.let { sid ->
                            customerDao.getCustomerById(sid)?.let { supplier ->
                                customerDao.updateCustomer(supplier.copy(
                                    totalBalance = supplier.totalBalance - cost
                                ))
                            }
                        }

                        logActivity("STOCK_IN", "Purchased ${finalTransaction.quantity} KG of ${stock.origin} for Rs. ${finalTransaction.amount}")
                    }
                }
            }
            TransactionType.STOCK_OUT -> {
                finalTransaction.stockId?.let { id ->
                    coalStockDao.getStockById(id)?.let { stock ->
                        if (stock.quantity < finalTransaction.quantity) {
                            throw IllegalStateException("Insufficient stock. Balance cannot be negative.")
                        }
                        coalStockDao.updateStock(stock.copy(quantity = stock.quantity - finalTransaction.quantity))
                        
                        // Integrated Liquidity: Add revenue to User balance if PAID
                        if (finalTransaction.paymentStatus == PaymentStatus.PAID) {
                            val revenue = finalTransaction.amount
                            userDao.getUserById(userId)?.let { user ->
                                userDao.updateUser(user.copy(balance = user.balance + revenue))
                            }
                        }

                        finalTransaction.customerId?.let { cid ->
                            customerDao.getCustomerById(cid)?.let { customer ->
                                val isPending = finalTransaction.paymentStatus == PaymentStatus.PENDING
                                
                                // Segmented Logic:
                                // If PAID: totalBalance increases (Cash in Hand).
                                // If PENDING: pendingBalance increases (Debt/Due).
                                val newTotalBalance = if (isPending) customer.totalBalance else customer.totalBalance + finalTransaction.amount
                                val newPendingBalance = if (isPending) customer.pendingBalance + finalTransaction.amount else customer.pendingBalance
                                
                                customerDao.updateCustomer(customer.copy(
                                    totalBalance = newTotalBalance,
                                    pendingBalance = newPendingBalance,
                                    lastTransactionDate = System.currentTimeMillis()
                                ))
                                logActivity("STOCK_OUT", "Sold ${finalTransaction.quantity} KG of ${stock.origin} for Rs. ${finalTransaction.amount} - Sent to: ${customer.name} - Status: Completed")
                            }
                        }
                    }
                }
            }
            TransactionType.CASH_IN -> {
                finalTransaction.customerId?.let { id ->
                    customerDao.getCustomerById(id)?.let { customer ->
                        // Add cash and reduce pending debt
                        val newBalance = customer.totalBalance + finalTransaction.amount
                        val newPending = (customer.pendingBalance - finalTransaction.amount).coerceAtLeast(0.0)
                        
                        customerDao.updateCustomer(customer.copy(
                            totalBalance = newBalance,
                            pendingBalance = newPending,
                            lastTransactionDate = System.currentTimeMillis()
                        ))
                        // Integrated Liquidity: Add cash in to User balance
                        userDao.getUserById(userId)?.let { user ->
                            userDao.updateUser(user.copy(balance = user.balance + finalTransaction.amount))
                        }
                        transactionDao.insertTransaction(finalTransaction.copy(balanceAfter = newBalance))
                        logActivity("CASH_IN", "Received Rs. ${finalTransaction.amount} from ${customer.name}")
                        return@addTransaction
                    }
                }
            }
            TransactionType.CASH_OUT -> {
                finalTransaction.customerId?.let { id ->
                    val currentUser = userDao.getUserById(userId)
                    if (currentUser != null && currentUser.balance < finalTransaction.amount) {
                        throw IllegalStateException("Insufficient Balance for this transaction.")
                    }

                    customerDao.getCustomerById(id)?.let { customer ->
                        // Reduce cash and increase recovery debt (since it's credit given/lending)
                        val newBalance = customer.totalBalance - finalTransaction.amount
                        val newPending = customer.pendingBalance + finalTransaction.amount

                        customerDao.updateCustomer(customer.copy(
                            totalBalance = newBalance,
                            pendingBalance = newPending,
                            lastTransactionDate = System.currentTimeMillis()
                        ))
                        // Integrated Liquidity: Subtract cash out from User balance
                        userDao.getUserById(userId)?.let { user ->
                            userDao.updateUser(user.copy(balance = user.balance - finalTransaction.amount))
                        }
                        transactionDao.insertTransaction(finalTransaction.copy(balanceAfter = newBalance))
                        logActivity("CASH_OUT", "Given Rs. ${finalTransaction.amount} to ${customer.name}")
                        return@addTransaction
                    }
                }
            }
        }
        transactionDao.insertTransaction(finalTransaction)
    }
}
