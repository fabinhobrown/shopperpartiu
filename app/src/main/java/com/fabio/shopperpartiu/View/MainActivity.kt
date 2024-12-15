package com.fabio.shopperpartiu.View

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.shopperpartiu.Controller.APIs
import com.fabio.shopperpartiu.Controller.Utils
import com.fabio.shopperpartiu.Model.Cliente
import com.fabio.shopperpartiu.Model.Motorista
import com.fabio.shopperpartiu.Model.Viagem
import com.fabio.shopperpartiu.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtIdCliente: TextInputEditText
    private lateinit var txInputIdCliente: TextInputLayout
    private lateinit var edtOrigem: TextInputEditText
    private lateinit var txInputOrigem: TextInputLayout
    private lateinit var edtDestino: TextInputEditText
    private lateinit var txInputDestino: TextInputLayout
    private lateinit var btnBuscar: Button
    private lateinit var btnMain: Button
    private lateinit var tvTimeTravel: TextView
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var pgCarregarCorridas: ProgressBar

    private var mGoogleMap: GoogleMap? = null
    private lateinit var adapter: OpcaoViagemAdapter
    private var apis = APIs()
    private var utils = Utils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        startComponents()

        configInitial()

        btnBuscar.setOnClickListener {
            searchTravel()
        }

        btnMain.setOnClickListener{
            funMain()
        }
    }

    // Valida os campos de entrada
    fun validateInputs(
        txInputIdCliente: TextInputLayout,
        txInputOrigem: TextInputLayout,
        txInputDestino: TextInputLayout
    ): Boolean{

        if(edtIdCliente.text.toString().isEmpty()){
            txInputIdCliente.error = "Campo obrigatório"
            return false
        }

        if(edtOrigem.text.toString().isEmpty()){
            txInputOrigem.error = "Campo obrigatório"
            return false
        }

        if(edtDestino.text.toString().isEmpty()){
            txInputDestino.error = "Campo obrigatório"
            return false
        }

        return true
    }

    // Busca a viagem
    fun searchTravel(){
        // Esconde o teclado de forma segura
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this) // Fallback para uma nova View
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

        txInputIdCliente.error = null
        txInputOrigem.error = null
        txInputDestino.error = null

        if(!validateInputs(txInputIdCliente, txInputOrigem, txInputDestino)){
            return
        }

        txInputIdCliente.visibility = View.GONE
        txInputOrigem.visibility = View.GONE
        txInputDestino.visibility = View.GONE
        btnBuscar.visibility = View.GONE
        pgCarregarCorridas.visibility = View.VISIBLE

        Toast.makeText(baseContext, "Consultando...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            // Chamar API de consulta de viagem
            val response = withContext(Dispatchers.IO) {
                apis.consultaViagemNativo(
                    customerId = edtIdCliente.text.toString(),
                    origem = edtOrigem.text.toString(),
                    destino = edtDestino.text.toString()
                )
            }

            // Parseia a resposta e atualiza o RecyclerView
            response?.let {
                // Alimentar RecyclerView
                val listaMotoristas = utils.parseJsonToMotorista(it)
                val infoViagem = utils.parseInfoViagem(it, edtOrigem, edtDestino)
                val cliente = Cliente(edtIdCliente.text.toString())

                if(listaMotoristas.size > 0){
                    adapter = OpcaoViagemAdapter(listaMotoristas, infoViagem, cliente){ motoristas, infoViagem, cliente ->
                        callAPIRegisterTravel(listaMotoristas, motoristas, infoViagem, cliente)
                    }

                    tvTimeTravel.visibility = View.VISIBLE

                    tvTimeTravel.text = "${infoViagem.distance} - ${infoViagem.durantion}"
                    btnBuscar.visibility = View.GONE
                    pgCarregarCorridas.visibility = View.GONE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                    recyclerView.adapter = adapter

                    // Traçar a rota no mapa
                    val pontosDaRota = utils.parsePolyline(it)

                    mGoogleMap?.apply {
                        clear() // Limpa o mapa anterior
                        addPolyline(
                            PolylineOptions().addAll(pontosDaRota).width(10f).color(Color.BLUE)
                        )

                        // Adicionar marcador de origem (ponto A)
                        val pontoOrigem = LatLng(pontosDaRota.first().latitude, pontosDaRota.first().longitude)
                        addMarker(
                            MarkerOptions()
                                .position(pontoOrigem)
                                .title("Origem")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Cor do marcador
                        )

                        // Adicionar marcador de destino (ponto B)
                        val pontoDestino = LatLng(pontosDaRota.last().latitude, pontosDaRota.last().longitude)
                        addMarker(
                            MarkerOptions()
                                .position(pontoDestino)
                                .title("Destino")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Cor do marcador
                        )

                        // Ajustar a câmera do mapa para focar na rota traçada
                        val boundsBuilder = LatLngBounds.Builder()
                        pontosDaRota.forEach { point ->
                            boundsBuilder.include(point)
                        }
                        val bounds = boundsBuilder.build()
                        moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 1))

                        // Ajustar o BottomSheet para metade da tela, mas mantendo a rota visível acima dele
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

                        // Ajustar o deslocamento da câmera do mapa
                        moveCamera(CameraUpdateFactory.scrollBy(-5f, 150f)) // Ajusta a câmera para cima
                    }
                }else{
                    showMessage(R.string.msg_error_if_null_drivers, "error", true)

                    btnMain.visibility = View.VISIBLE

                    bottomSheetBehavior.peekHeight = (0).toInt()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

            } ?: run {
                showMessage(R.string.msg_error_search_travel, "error", true)

                btnMain.visibility = View.VISIBLE

                bottomSheetBehavior.peekHeight = (0).toInt()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    // Configurações iniciais
    fun configInitial(){
        mapFragment.getMapAsync(this)

        // Configuração do RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurações do BottomSheet
        bottomSheetBehavior.peekHeight = (0).toInt()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED // Estado inicial (metade da tela)
    }

    // Configurações iniciais
    fun funMain(){
        btnMain.visibility = View.GONE
        bottomSheetBehavior.peekHeight = (resources.displayMetrics.heightPixels * 0.15).toInt() // Metade da tela
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        txInputIdCliente.visibility = View.VISIBLE
        txInputOrigem.visibility = View.VISIBLE
        txInputDestino.visibility = View.VISIBLE
    }

    // Exibe uma mensagem na tela
    fun showMessage(msg: Int, status: String, interaction: Boolean) {
        btnBuscar.visibility = View.VISIBLE
        pgCarregarCorridas.visibility = View.GONE

        val rootView = findViewById<View>(android.R.id.content)

        var time = 0

        if(interaction){
            time = Snackbar.LENGTH_INDEFINITE
        }else{
            time = Snackbar.LENGTH_SHORT
        }

        val snackbar = Snackbar.make(rootView, msg, time)
        snackbar.setAction("Fechar") {
            snackbar.dismiss() // Agora o Snackbar está acessível
        }

        when (status) {
            "success" -> snackbar.setBackgroundTint(Color.GREEN)
            "error" -> snackbar.setBackgroundTint(Color.RED)
            "warning" -> snackbar.setBackgroundTint(Color.YELLOW)
        }

        snackbar.setTextColor(Color.WHITE)
        snackbar.setActionTextColor(Color.YELLOW)

        snackbar.show()
    }

    // Registra a viagem
    private fun callAPIRegisterTravel(
        listaMotoristas: List<Motorista>,
        motorista: Motorista,
        viagem: Viagem,
        cliente: Cliente
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar viagem com ${motorista.name}?")
        builder.setMessage(
            "${motorista.description}\n\n" +
                    "Veículo: ${motorista.vehicle}\n\n" +
                    "Avaliação: ${motorista.review[0].second}"
        )
        builder.setPositiveButton("OK") { dialog, which ->
            // Criar o primeiro AlertDialog
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_review, null)

            // Criar o segundo AlertDialog
            val builder2 = AlertDialog.Builder(this)
            builder2.setView(dialogView)
            builder2.setTitle("Como foi a viagem com ${motorista.name}?")
            builder2.setPositiveButton("OK") { dialog2, which2 ->

                CoroutineScope(Dispatchers.Main).launch {
                    val response = withContext(Dispatchers.IO) {
                        apis.registraViagemNativo(
                            motorista, viagem, cliente
                        )
                    }

                    response?.let {
                        Toast.makeText(baseContext, "Viagem registrada com sucesso!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(baseContext, HistoricoViagens::class.java).apply {
                            putExtra("LISTA_MOTORISTAS", ArrayList(listaMotoristas))
                        }
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(baseContext, "Erro ao registrar viagem", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Exibir o segundo AlertDialog após fechar o primeiro
            val dialog2 = builder2.create()
            dialog2.show()
        }
        builder.setNegativeButton("Cancelar") { dialog, which -> }

        val dialog = builder.create()
        dialog.show()
    }

    // Mapa
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }

    // Configurações dos componentes
    fun startComponents(){
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet))
        recyclerView = findViewById(R.id.recyclerView)
        btnMain = findViewById(R.id.btnMain)
        edtIdCliente = findViewById(R.id.edtIdCliente)
        txInputIdCliente = findViewById(R.id.txInputIdCliente)
        edtOrigem = findViewById(R.id.edtOrigem)
        txInputOrigem = findViewById(R.id.txInputOrigem)
        edtDestino = findViewById(R.id.edtDestino)
        txInputDestino = findViewById(R.id.txInputDestino)
        btnBuscar = findViewById(R.id.btnBuscar)
        tvTimeTravel = findViewById(R.id.tvTimeTravel)
        pgCarregarCorridas = findViewById(R.id.pgCarregarCorridas)
    }
}