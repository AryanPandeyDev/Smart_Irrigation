package com.example.smartirrigation.domain.repositories

import com.example.smartirrigation.data.network.dto.IrrigatorInfo
import kotlinx.coroutines.flow.Flow

interface IrrigationRepository {

    suspend fun getStatus(): Flow<IrrigatorInfo?>

    suspend fun setThreshold(threshold: Int) : Boolean

    suspend fun setControlMode(isManual: Boolean) : Boolean

    suspend fun turnOnPump(pumpStatus : Boolean) : Boolean

}