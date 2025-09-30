package devandroid.moacir.minhamedia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class InstructionActivity : AppCompatActivity() {

    private lateinit var buttonGotIt: Button
    private lateinit var prefs: SharedPreferences

    companion object {
        // Nome para o arquivo de SharedPreferences
        const val PREFS_NAME = "MinhaMediaPrefs"
        // Chave para verificar se é a primeira execução
        const val KEY_FIRST_RUN = "isFirstRunInstruction" // Alterado para ser específico desta tela
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Verifica se as instruções já foram mostradas
        // O valor padrão para getBoolean é 'true', significando "sim, é a primeira vez".
        if (!prefs.getBoolean(KEY_FIRST_RUN, true)) {
            // Não é a primeira vez, então navega direto para a MainActivity
            navigateToMainActivity()
            return // Essencial para não inflar o layout da InstructionActivity desnecessariamente
        }

        // Se chegou aqui, é a primeira vez (ou a flag foi resetada)
        setContentView(R.layout.activity_instruction)

        buttonGotIt = findViewById(R.id.buttonGotIt)

        buttonGotIt.setOnClickListener {
            // Marca que as instruções foram vistas (para não mostrar na próxima vez)
            prefs.edit { putBoolean(KEY_FIRST_RUN, false) }

            // Navega para a MainActivity
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Finaliza esta InstructionActivity para que o usuário não possa voltar para ela
        // usando o botão "Voltar" a partir da MainActivity.
        finish()
    }
}
