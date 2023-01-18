package univ.master.meteo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import univ.master.meteo.ville.Ville

private const val NOM_DATABASE = "meteo.db"
private const val VERSION_DATABASE = 1

private const val NOM_TABLE_VILLE = "ville"
private const val ID_CLE_VILLE = "id"
private const val NOM_CLE_VILLE = "nom"

private const val VILLE_TABLE_CREER = """
    CREATE TABLE $NOM_TABLE_VILLE (
        $ID_CLE_VILLE INTEGER PRIMARY KEY,
        $NOM_CLE_VILLE TEXT
    )
"""

class Database(contexte: Context): SQLiteOpenHelper(contexte, NOM_DATABASE, null, VERSION_DATABASE) {

    val TAG = Database::class.java.simpleName

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(VILLE_TABLE_CREER)
    }

    override fun onUpgrade(db: SQLiteDatabase?, ancienneVersion: Int, nouvelleVersion: Int) {
        TODO("Not yet implemented")
    }

    fun creerVille(ville: Ville): Boolean {
        val valeurs = ContentValues()
        valeurs.put(NOM_CLE_VILLE, ville.nom)

        Log.d(TAG, "Ville créée : $valeurs")

        val id = writableDatabase.insert(NOM_TABLE_VILLE, null, valeurs)
        ville.id = id

        return id > 0
    }
}