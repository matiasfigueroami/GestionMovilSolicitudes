package com.example.gestionmovilsolicitudes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gestionmovilsolicitudes.data.dao.ClienteDao
import com.example.gestionmovilsolicitudes.data.dao.EvidenciaDao
import com.example.gestionmovilsolicitudes.data.dao.HistorialDao
import com.example.gestionmovilsolicitudes.data.dao.SolicitudDao
import com.example.gestionmovilsolicitudes.data.dao.UsuarioDao
import com.example.gestionmovilsolicitudes.data.entity.ClienteEntity
import com.example.gestionmovilsolicitudes.data.entity.EvidenciaEntity
import com.example.gestionmovilsolicitudes.data.entity.HistorialEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ClienteEntity::class,
        SolicitudEntity::class,
        HistorialEntity::class,
        EvidenciaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun historialDao(): HistorialDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestion_solicitudes.db"
                ).build().also { INSTANCE = it }
            }
    }
}
