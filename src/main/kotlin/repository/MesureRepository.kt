package com.home.repository

import db.MesureEntity

interface MesureRepository {

    suspend fun findAll(): List<MesureEntity>?
    suspend fun save(mesure: MesureEntity)
}