package com.fabio.shopperpartiu.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fabio.shopperpartiu.Model.ViagemHistorico
import com.fabio.shopperpartiu.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HistoricoViagemAdapter(private val viagemHistorico: List<ViagemHistorico>, ) : RecyclerView.Adapter<HistoricoViagemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataHoraViagem: TextView = itemView.findViewById(R.id.tvDataHoraViagem)
        val motorista: TextView = itemView.findViewById(R.id.tvMotorista)
        val origem: TextView = itemView.findViewById(R.id.tvOrigem)
        val destino: TextView = itemView.findViewById(R.id.tvDestino)
        val distancia: TextView = itemView.findViewById(R.id.tvDistancia)
        val tempoValor: TextView = itemView.findViewById(R.id.tvTempoValor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historico_viagem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val viagem = viagemHistorico[position]

        // Formatar a data e hora
        val originalDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val targetDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dataHoraFormatada = try {
            val date = originalDateFormat.parse(viagem.date)
            targetDateFormat.format(date)
        } catch (e: Exception) {
            viagem.date // Em caso de erro, exibe o texto original
        }

        // Formatar o valor
        val valorFormatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(viagem.value)

        // Formatar a distância
        val distanciaFormatada = String.format("%.2f km", viagem.distance)

        // Formatar a duração
        val duracaoFormatada = viagem.duration.split(":").let { partes ->
            val minutos = partes.getOrNull(0)?.toIntOrNull() ?: 0
            val segundos = partes.getOrNull(1)?.toIntOrNull() ?: 0
            String.format("%02dm %02ds", minutos, segundos)
        }

        // Preencher os valores nos TextViews
        holder.dataHoraViagem.text = dataHoraFormatada
        holder.motorista.text = viagem.driver
        holder.origem.text = "${viagem.origin}"
        holder.destino.text = "${viagem.destination}"
        holder.distancia.text = "Distância: $distanciaFormatada"
        holder.tempoValor.text = "$valorFormatado - Duração: $duracaoFormatada - $distanciaFormatada"
    }

    override fun getItemCount(): Int = viagemHistorico.size
}