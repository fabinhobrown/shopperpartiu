package com.fabio.shopperpartiu.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fabio.shopperpartiu.Model.Cliente
import com.fabio.shopperpartiu.Model.Motorista
import com.fabio.shopperpartiu.Model.Viagem
import com.fabio.shopperpartiu.R

class OpcaoViagemAdapter(

    private val motoristas: List<Motorista>,
    private val viagem: Viagem,
    private val cliente: Cliente,
    private val onMotoristaSelecionado: (Motorista, Viagem, Cliente) -> Unit

    ) : RecyclerView.Adapter<OpcaoViagemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val motorista: TextView = itemView.findViewById(R.id.tvMotorista)
        val valor: TextView = itemView.findViewById(R.id.tvValor)
        val review: TextView = itemView.findViewById(R.id.tvReview)
        val img: ImageView = itemView.findViewById(R.id.imgCar)
        val btnSelecionarMotorista: Button = itemView.findViewById(R.id.btnSelecionarMotorista)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_opcao_motorista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val motorista = motoristas[position]

        holder.motorista.text = motorista.name
        holder.valor.text = "R$${motorista.value}"
        val nota = motorista.review[0].first
        holder.review.text = "Nota $nota / 5"

        // Gerar o nome do recurso dinamicamente baseado no nome do motorista
        val nomeRecurso = motorista.name.lowercase().replace(" ", "_")

        // Obter o ID do recurso
        val drawableResId = holder.itemView.context.resources.getIdentifier(
            nomeRecurso, "drawable", holder.itemView.context.packageName
        )

        // Verificar se o recurso foi encontrado e definir a imagem
        if (drawableResId != 0) {
            holder.img.setImageResource(drawableResId)
        }

        holder.btnSelecionarMotorista.setOnClickListener {
            onMotoristaSelecionado(motorista, viagem, cliente)
        }
    }

    override fun getItemCount(): Int = motoristas.size
}