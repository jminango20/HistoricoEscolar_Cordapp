package com.template.states

import com.template.model.HistoricoEscolar
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class HistoricoEscolarState(
    val historicoEscolar: HistoricoEscolar,
    val faculdadesReceptoras: List<Party> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants: List<AbstractParty>
        get() = faculdadesReceptoras + historicoEscolar.faculdade
    }
