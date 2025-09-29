package devandroid.moacir.minhamedia


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Unnecessary imports based on the provided code, you might want to remove them if not used elsewhere
// import kotlin.text.format
// import kotlin.text.isNotEmpty
// import kotlin.text.toFloat
class MainActivity : AppCompatActivity() {

    private lateinit var editTextNota1: TextInputEditText
    private lateinit var editTextNota2: TextInputEditText
    private lateinit var sliderNota2: Slider
    private lateinit var editTextResultadoMedia: TextInputEditText
    private lateinit var buttonZerar: Button

    private var isUpdatingFromSlider = false
    private var isUpdatingFromEditText = false
    private val valueFormat = DecimalFormat("#.0") // Para formatar com uma casa decimal
    private val decimalFormatter = DecimalFormat("0.0", DecimalFormatSymbols(Locale.US))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextNota1 = findViewById(R.id.editTextNota1)
        editTextNota2 = findViewById(R.id.editTextNota2)
        sliderNota2 = findViewById(R.id.sliderNota2)
        //buttonCalcularMedia = findViewById(R.id.buttonCalcularMedia)
        editTextResultadoMedia = findViewById(R.id.editTextResultadoMedia)
        buttonZerar = findViewById(R.id.buttonZerar)

        buttonZerar.setOnClickListener {
            redefinirCampos()
        }


        val initialNota1 = 0.0f
        val initialNota2 = 6.0f

        editTextNota1.setText(decimalFormatter.format(initialNota1))
        updateEditTextNota2(initialNota2)

        setupInputListeners()
        setupNota2Sync()
        setupNota1FocusListener() // This call is now correct as the function will be part of the class

        // Definir um valor inicial (opcional, mas bom para consistência)
        updateEditTextNota2(initialNota2)
        updateSliderNota2(initialNota2)
        calcularMediaSiPosible()
    }

    private fun setupInputListeners() {
        // Listener para nota1 para recalcular quando ela mudar
        editTextNota1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularMediaSiPosible() // Tenta calcular a média
            }
        })
    }

    private fun setupNota2Sync() {
        // Listener para quando o Slider muda
        sliderNota2.addOnChangeListener { _, value, fromUser ->
            if (fromUser && !isUpdatingFromEditText) {
                isUpdatingFromSlider = true
                updateEditTextNota2(value)
                isUpdatingFromSlider = false
                calcularMediaSiPosible()
            }
        }

        // Listener para quando o EditText muda
        editTextNota2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!isUpdatingFromSlider) {
                    isUpdatingFromEditText = true
                    val textValue = s.toString()
                    val floatValue = textValue.toFloatOrNull()

                    if (floatValue != null && floatValue >= sliderNota2.valueFrom && floatValue <= sliderNota2.valueTo) {
                        if (sliderNota2.value != floatValue) {
                            updateSliderNota2(floatValue)
                        }
                        // VERIFICAÇÃO ADICIONAL AQUI TAMBÉM (para consistência se o usuário digitar na Nota 2)
                        val nota1Str = editTextNota1.text.toString()
                        if (nota1Str.isBlank()) {
                            editTextNota1.error = "Por favor, digite a Nota 1 primeiro."
                            // editTextNota1.requestFocus()
                            limparResultado()
                        } else {
                            editTextNota1.error = null
                            calcularMediaSiPosible()
                        }
                    } else if (textValue.isNotEmpty()) {
                        limparResultado()
                    } else {
                        limparResultado()
                    }
                    isUpdatingFromEditText = false

                }
            }
        })
    }

    private fun updateEditTextNota2(value: Float) {
        // Formata para garantir uma casa decimal e usa ponto como separador
        editTextNota2.setText(String.format(Locale.US, "%.1f", value))
        // Você pode querer mover o cursor para o final se estiver editando
        // editTextNota2.setSelection(editTextNota2.text?.length ?: 0)
    }

    private fun updateSliderNota2(value: Float) {
        // Garante que o valor está dentro dos limites do slider antes de definir
        val clampedValue = value.coerceIn(sliderNota2.valueFrom, sliderNota2.valueTo)
        if (sliderNota2.value != clampedValue) {
            sliderNota2.value = clampedValue
        }
    }

    private fun limparResultado() {
        editTextResultadoMedia.setText("")
        // Use uma cor padrão ou transparente. Se o seu EditText tem um fundo padrão do tema,
        // você pode não precisar setar uma cor específica aqui, ou pode usar android.R.color.transparent
        editTextResultadoMedia.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.corPadraoFundoFesultado
            )
        ) // Supondo que você definiu esta cor
    }

    private fun calcularMediaSiPosible() {
        val nota1Str = editTextNota1.text.toString()
        val nota2Str = editTextNota2.text.toString()

        // Caso 1: Nota 1 vazia, mas Nota 2 tem algo (usuário interagiu com Nota 2 primeiro)
        if (nota1Str.isBlank() && nota2Str.isNotBlank()) {
            editTextNota1.error = "Por favor, digite a Nota 1."
            limparResultado()
            return // Importante: Sai da função aqui
        } else if (nota1Str.isNotBlank() && editTextNota1.error != null) {
            // Se a nota1 está preenchida mas tinha um erro, vamos tentar limpá-lo.
            // A validação de range abaixo pode definir um novo erro se necessário.
            // Isso evita que um erro antigo persista se o usuário corrigir a entrada.
            val tempNota1 = nota1Str.toFloatOrNull()
            if (tempNota1 != null && tempNota1 >= 0 && tempNota1 <= 10) {
                editTextNota1.error = null
            }
        }


        // Caso 2: Ambas as notas têm algum texto. Vamos validar e calcular.
        if (nota1Str.isNotBlank() && nota2Str.isNotBlank()) {
            val nota1 = nota1Str.toFloatOrNull()
            val nota2 = nota2Str.toFloatOrNull()

            var nota1Valida = false
            var nota2Valida = false

            // Validação da Nota 1
            if (nota1 != null) {
                if (nota1 >= 0 && nota1 <= 10) {
                    nota1Valida = true
                    editTextNota1.error = null
                } else {
                    editTextNota1.error = "Nota 1 inválida (0-10)"
                    nota1Valida = false
                }
            } else { // nota1Str não é blank, mas toFloatOrNull() retornou null (ex: "abc")
                editTextNota1.error = "Nota 1: valor numérico inválido"
                nota1Valida = false
            }

            // Validação da Nota 2
            if (nota2 != null) {
                // Usar os limites do slider para validação é uma boa prática
                if (nota2 >= sliderNota2.valueFrom && nota2 <= sliderNota2.valueTo) {
                    nota2Valida = true
                    editTextNota2.error = null
                } else {
                    editTextNota2.error =
                        "Nota 2 inválida (${valueFormat.format(sliderNota2.valueFrom)}-${
                            valueFormat.format(sliderNota2.valueTo)
                        })"
                    nota2Valida = false
                }
            } else { // nota2Str não é blank, mas toFloatOrNull() retornou null
                editTextNota2.error = "Nota 2: valor numérico inválido"
                nota2Valida = false
            }

            if (nota1Valida && nota2Valida) {
                val media =
                    (nota1!! * 2 + nota2!! * 3) / 5 // Usar !! é seguro aqui devido às validações
                editTextResultadoMedia.setText(decimalFormatter.format(media))

                if (media >= 6.0f) {
                    editTextResultadoMedia.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.corAprovadoFundo
                        )
                    )
                } else {
                    editTextResultadoMedia.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.corReprovadoFundo
                        )
                    )
                }
                return // Importante: Sai da função após calcular e exibir com sucesso
            }
            // else (se não for nota1Valida && nota2Valida) -> vai para o limparResultado() abaixo
        }

        // Caso 3: Pelo menos uma das notas está em branco (e não é o Caso 1)
        // OU as notas não são válidas (caiu do if acima).
        // Neste ponto, a média não pôde ser calculada, então limpamos o resultado.
        limparResultado()
    }

    private fun setupNota1FocusListener() {
        editTextNota1.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) { // Quando o EditText PERDE o foco
                val text = editTextNota1.text.toString()
                if (text.isNotEmpty()) {
                    try {
                        // Tenta converter para Float para validar e normalizar
                        val number = text.toFloat()
                        // Formata para ter sempre uma casa decimal
                        val formattedText = decimalFormatter.format(number)
                        editTextNota1.setText(formattedText)
                    } catch (e: NumberFormatException) {
                        // O texto não é um número válido, pode limpar ou mostrar erro
                        // editTextNota1.error = "Número inválido"
                        // ou deixar como está, dependendo da sua lógica de validação
                    }
                }
            }
        }
    }

    private fun redefinirCampos() {
        val initialNota1 = 0.0f
        val initialNota2 = 6.0f // Ou o valor inicial que você preferir para Nota2

        // Limpar erros antigos (importante!)
        editTextNota1.error = null
        editTextNota2.error = null

        // Redefinir Nota 1
        editTextNota1.setText(decimalFormatter.format(initialNota1)) // Ex: "0.0"

        // Redefinir Nota 2 (usando suas funções existentes para consistência)
        // É importante desabilitar temporariamente os listeners para evitar loops ou cálculos indesejados
        // durante a redefinição programática, embora com suas flags `isUpdating*` possa já ser seguro.
        // Mas para garantir, podemos adicionar uma flag geral.
        // No entanto, suas funções updateEditTextNota2 e updateSliderNota2 já parecem
        // ser chamadas com cuidado em relação aos listeners.

        updateEditTextNota2(initialNota2)
        updateSliderNota2(initialNota2) // Garante que o slider também reflita o valor

        // Recalcular a média com os valores redefinidos
        calcularMediaSiPosible()

        // Opcional: Mover o foco para o primeiro campo
        // editTextNota1.requestFocus()
    }
} // This is the closing brace for MainActivity class