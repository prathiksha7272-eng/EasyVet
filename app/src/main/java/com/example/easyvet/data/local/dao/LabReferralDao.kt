package com.example.easyvet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.model.ReferralStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LabReferralDao {

    @Query("SELECT * FROM lab_referrals ORDER BY collectionDate DESC")
    fun getAllReferrals(): Flow<List<LabReferralEntity>>

    @Query("SELECT * FROM lab_referrals WHERE id = :id")
    suspend fun getReferralById(id: String): LabReferralEntity?

    @Query("SELECT * FROM lab_referrals WHERE trackingNumber = :trackingNumber")
    suspend fun getReferralByTrackingNumber(trackingNumber: String): LabReferralEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: LabReferralEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReferrals(referrals: List<LabReferralEntity>)

    @Update
    suspend fun updateReferral(referral: LabReferralEntity)

    @Query("UPDATE lab_referrals SET status = :status, testResults = :results, resultNotes = :notes WHERE id = :id")
    suspend fun updateReferralStatusAndResult(id: String, status: ReferralStatus, results: String?, notes: String?)

    @Query("SELECT COUNT(*) FROM lab_referrals")
    suspend fun getReferralCount(): Int
}
