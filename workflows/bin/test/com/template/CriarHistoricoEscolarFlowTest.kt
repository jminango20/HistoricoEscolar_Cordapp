package com.template

import com.template.flows.ArmazenarHistoricoFlow
import com.template.model.HistoricoEscolar
import com.template.states.HistoricoEscolarState
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

class CriarHistoricoEscolarFlowTest {
    lateinit var network: MockNetwork
    lateinit var a : StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
            ))
        )
        a = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }


    @Test
    fun `deve criar historico escolar`() {
        val disciplina = HistoricoEscolar(idAluno = 1,
            cargaHoraria = 0,
            dataInicio = Instant.now(),
            faculdade = a.info.legalIdentities.first(),
            nomeCurso = "Corda",
            nota = 10)

        val flow = ArmazenarHistoricoFlow.ReqFlow(disciplina)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.get()
        val output = signedTransaction.coreTransaction.outputsOfType<HistoricoEscolarState>().single()
        assertEquals(output.historicoEscolar, disciplina)
    }

}


