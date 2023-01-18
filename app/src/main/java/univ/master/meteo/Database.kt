package univ.master.meteo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Nom du fichier représentant la table
 */
private const val NOM_DATABASE = "meteo.db"

/**
 * Numéro de version de la base de données
 */
private const val VERSION_DATABASE = 1

/**
 * Nom de la table principale
 */
private const val NOM_VILLE_TABLE = "ville"

/**
 * Identifiant de la ville
 */
private const val ID_VILLE_CLE = "id"

/**
 * Nom de la ville
 */
private const val NOM_VILLE_CLE = "nom"

// Requête de création de la table
private const val TABLE_VILLE_CREER = """
    CREATE TABLE $NOM_VILLE_TABLE(
        $ID_VILLE_CLE INTEGER PRIMARY KEY,
        $NOM_VILLE_CLE TEXT
    )
"""

class Database(contexte: Context) : SQLiteOpenHelper(contexte, NOM_DATABASE, null, VERSION_DATABASE) {
    override fun onCreate(db: SQLiteDatabase?) {
        /* Création de la table */
        db?.execSQL(TABLE_VILLE_CREER)
    }

    override fun onUpgrade(db: SQLiteDatabase?, ancienneVersion: Int, nouvelleVersion: Int) {
    }

}