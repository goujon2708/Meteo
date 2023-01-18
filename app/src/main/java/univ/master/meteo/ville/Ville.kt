package univ.master.meteo.ville

data class Ville(var id: Long, var nom: String) {

    /**
     * Constructeur de la classe Ville
     */
    constructor(name: String):
        this(-1, name)
}