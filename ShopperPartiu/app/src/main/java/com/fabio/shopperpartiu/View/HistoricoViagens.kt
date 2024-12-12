package com.fabio.shopperpartiu.View

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.shopperpartiu.Controller.APIs
import com.fabio.shopperpartiu.Controller.Utils
import com.fabio.shopperpartiu.Model.Motorista
import com.fabio.shopperpartiu.Model.ViagemHistorico
import com.fabio.shopperpartiu.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HistoricoViagens : AppCompatActivity() {

    private lateinit var spnMotoristas: Spinner
    private lateinit var btnListarHistoricoViagens: Button
    private lateinit var txInputIdClienteHistorico: TextInputLayout
    private lateinit var edtIdClienteHistorico: TextInputEditText
    private lateinit var tvQtdViagens: TextView
    private lateinit var motorista: Motorista
    private lateinit var adapterHistoricoViagem : HistoricoViagemAdapter
    private lateinit var recyclerViewHistoricoViagem: RecyclerView
    private lateinit var pgCarregarHistorico: ProgressBar
    private lateinit var motoristas: ArrayList<Motorista>
    private var apis = APIs()
    private var utils = Utils()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historico_viagens)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_historico)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startComponents()

        getListDriversFromMainActivity()

        spnMotoristas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                motorista = motoristas[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        btnListarHistoricoViagens.setOnClickListener {
            getTravelHistory()
        }
    }

    // Função para validar os campos de entrada
    fun validateInputs(txInputIdClienteHistorico: TextInputLayout): Boolean{
        if(edtIdClienteHistorico.text.toString().isEmpty()){
            txInputIdClienteHistorico.error = "Campo obrigatório"
            return false
        }
        return true
    }

    // Função para consultar o histórico de viagens
    fun getTravelHistory(){

        txInputIdClienteHistorico.error = null

        if(!validateInputs(txInputIdClienteHistorico)){
            return
        }

        pgCarregarHistorico.visibility = View.VISIBLE
        btnListarHistoricoViagens.isEnabled = false

        // Esconde o teclado de forma segura
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this) // Fallback para uma nova View
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

        CoroutineScope(Dispatchers.Main).launch {
            val response = withContext(Dispatchers.IO) {
                val cliente = edtIdClienteHistorico.text.toString()
                apis.consultaHistoricoViagemNativo(cliente, motorista)
            }

            response?.let {
                pgCarregarHistorico.visibility = View.GONE
                val historico = utils.parseJsonToHistorico(it, motorista)

                // Atualizar o text view para mostrar a quantidade de viagens
                if(historico.size > 0){
                    tvQtdViagens.text = "Total de viagens [${historico.size}]"
                }
                else{
                    tvQtdViagens.text = "Total de viagens [0]"
                }

                adapterHistoricoViagem = HistoricoViagemAdapter(historico)
                recyclerViewHistoricoViagem.adapter = adapterHistoricoViagem
                btnListarHistoricoViagens.isEnabled = true

            }?: run {
                btnListarHistoricoViagens.isEnabled = true
                pgCarregarHistorico.visibility = View.GONE
                Toast.makeText(baseContext, "Erro ao consultar histórico de viagens", Toast.LENGTH_SHORT).show()
                tvQtdViagens.text = "Total de viagens [0]"
            }
        }
    }

    // Função para obter a lista de motoristas do MainActivity
    fun getListDriversFromMainActivity(){
        motoristas = intent.getSerializableExtra("LISTA_MOTORISTAS") as ArrayList<Motorista>

        val motoristaTODOS = Motorista(1, "TODOS", "", "", "", listOf())

        motoristas?.add(motoristas.size, motoristaTODOS)

        var nomesMotoristas = motoristas!!.map { it.name }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesMotoristas)
        spnMotoristas.adapter = adapter
    }

    // Função para iniciar os componentes da tela
    fun startComponents(){
        btnListarHistoricoViagens = findViewById(R.id.btnListarHistoricoViagens)
        edtIdClienteHistorico = findViewById(R.id.edtIdClienteHistorico)
        txInputIdClienteHistorico = findViewById(R.id.txInputIdClienteHistorico)
        spnMotoristas = findViewById(R.id.spnMotoristas)
        tvQtdViagens = findViewById(R.id.tvQtdViagens)
        pgCarregarHistorico = findViewById(R.id.pgCarregarHistorico)
        recyclerViewHistoricoViagem = findViewById(R.id.recyclerViewHistoricoViagens)
        recyclerViewHistoricoViagem.layoutManager = LinearLayoutManager(this)
    }
}