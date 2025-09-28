package devandroid.moacir.minhamedia


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.color
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import devandroid.moacir.minhamedia.R
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
    private lateinit var buttonCalcularMedia: Button
    private lateinit var editTextResultadoMedia: TextInputEditText

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
        buttonCalcularMedia = findViewById(R.id.buttonCalcularMedia)
        editTextResultadoMedia = findViewById(R.id.editTextResultadoMedia)

        setupInputListeners()
        setupNota2Sync()
        setupNota1FocusListener() // This call is now correct as the function will be part of the class


        buttonCalcularMedia.setOnClickListener {
            calcularMedia()
        }

        // Definir um valor inicial (opcional, mas bom para consistência)
        val initialNota2 = 6.0f // Ou leia de algum savedInstanceState
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
                    // Usar Locale.US para garantir que o ponto decimal seja reconhecido
                    val floatValue = textValue.toFloatOrNull()

                    if (floatValue != null && floatValue >= sliderNota2.valueFrom && floatValue <= sliderNota2.valueTo) {
                        if (sliderNota2.value != floatValue) {
                            updateSliderNota2(floatValue)
                        }
                        // Só calcula se a atualização do EditText para o Slider foi bem-sucedida e válida
                        calcularMediaSiPosible() // Calcula média ao mudar EditText da nota 2
                    } else if (textValue.isNotEmpty()) {
                        // Se o valor não é válido para o slider, limpamos o resultado
                        // e resetamos a cor, pois não podemos calcular.
                        //editTextNota2.error = "Valor entre ${sliderNota2.valueFrom} e ${sliderNota2.valueTo}" // Opcional
                        limparResultado()
                    } else {
                        // Se o campo está vazio, também limpamos o resultado
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

        // Só tenta calcular se ambos os campos tiverem algum texto
        if (nota1Str.isNotBlank() && nota2Str.isNotBlank()) {
            val nota1 = nota1Str.toFloatOrNull()
            val nota2 = nota2Str.toFloatOrNull()

            if (nota1 != null && nota1 >= 0 && nota1 <= 10 &&
                nota2 != null && nota2 >= sliderNota2.valueFrom && nota2 <= sliderNota2.valueTo
            ) {

                // Limpar erros antigos se as entradas agora são válidas
                editTextNota1.error = null
                editTextNota2.error = null

                val media = (nota1 * 2 + nota2 * 3) / 5
                val df = DecimalFormat("#.##")
                editTextResultadoMedia.setText(df.format(media))

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
            } else {
                // Se uma das notas (mesmo preenchida) for inválida no formato ou range, limpa o resultado.
                // Erros específicos de campo serão tratados por validações mais diretas se necessário,
                // mas para o cálculo automático, simplesmente não mostramos a média.
                limparResultado()
                // Poderia adicionar feedback de erro sutil aqui ou confiar no botão "Calcular" para erros explícitos.
            }
        } else {
            // Se um dos campos estiver vazio, limpa o resultado
            limparResultado()
        }
    }

    private fun calcularMedia() {
        val nota1Str = editTextNota1.text.toString()
        val nota2Str = editTextNota2.text.toString()

        var isValid = true // Flag para checar validade geral

        if (nota1Str.isEmpty()) {
            editTextNota1.error = "Digite a Nota 1"
            isValid = false
        } else {
            val n1 = nota1Str.toFloatOrNull()
            if (n1 == null || n1 < 0 || n1 > 10) {
                editTextNota1.error = "Nota 1 inválida (0-10)"
                isValid = false
            } else {
                editTextNota1.error = null // Limpa erro se agora estiver válido
            }
        }

        if (nota2Str.isEmpty()) {
            editTextNota2.error = "Digite ou deslize para a Nota 2"
            isValid = false
        } else {
            val n2 = nota2Str.toFloatOrNull()
            if (n2 == null || n2 < sliderNota2.valueFrom || n2 > sliderNota2.valueTo) {
                editTextNota2.error =
                    "Nota 2 inválida (${valueFormat.format(sliderNota2.valueFrom)}-${
                        valueFormat.format(sliderNota2.valueTo)
                    })"
                isValid = false
            } else {
                editTextNota2.error = null // Limpa erro se agora estiver válido
            }
        }
        if (!isValid) {
            limparResultado()
            return
        }

        // Se chegou aqui, ambas as notas são válidas e não estão vazias
        val nota1 = nota1Str.toFloat() // Já validado que não é null
        val nota2 = nota2Str.toFloat() // Já validado que não é null

        val media = (nota1 * 2 + nota2 * 3) / 5
        val df = DecimalFormat("#.##")
        editTextResultadoMedia.setText(df.format(media))

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
    }

    // MOVED INSIDE THE CLASS
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
} // This is the closing brace for MainActivity class