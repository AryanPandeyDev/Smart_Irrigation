package com.example.smartirrigation.domain.repositories

import com.example.smartirrigation.data.network.dto.IrrigatorInfo

interface IrrigationRepository {

    suspend fun getStatus(): IrrigatorInfo?

}