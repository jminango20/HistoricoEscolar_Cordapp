package com.template.model

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@CordaSerializable //Informa a Corda que esta classe pode ser serializada e armazenada
data class HistoricoEscolar(
    val idAluno: Int,
    val nomeCurso: String,
    val dataInicio: Instant, //Java representa un instante no tempo: Data, Hora, Minuto, Segundo, Milisegundo
    val nota: Int,
    val cargaHoraria: Int,
    val faculdade: Party //No dentro da rede
)
