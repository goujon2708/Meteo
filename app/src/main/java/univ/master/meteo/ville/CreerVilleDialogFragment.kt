package univ.master.meteo.ville

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.InputType
import android.widget.EditText
import univ.master.meteo.R

class CreerVilleDialogFragment: DialogFragment() {

    interface CreerVilleDialogListener {
        fun onDialogPositiveClick(nomVille: String)
        fun onDialogNegativClick()
    }

    var listener: CreerVilleDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)

        val input = EditText(context)
        with(input) {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = context.getString(R.string.creerVille_villeHint)
        }

        builder.setTitle(R.string.creerVille_titre)
            .setView(input)
            .setPositiveButton(getString(R.string.creerVille_positive),
                DialogInterface.OnClickListener {_, _ ->
                    listener?.onDialogPositiveClick(input.text.toString())
            })
            .setNegativeButton(R.string.creerVille_negative ,
                DialogInterface.OnClickListener {dialog, _ ->
                    listener?.onDialogNegativClick()
            })

        return builder.create()
    }
}