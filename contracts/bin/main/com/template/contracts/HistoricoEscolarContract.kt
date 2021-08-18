package com.template.contracts

import com.template.states.HistoricoEscolarState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.lang.IllegalStateException

//Um Contract não representa dados, e sim regras, por isso vamos utilizar apenas “class” na sua definição

class HistoricoEscolarContract: Contract {

    override fun verify(tx: LedgerTransaction) {

        val command = tx.commandsOfType<Commands>().single()
        when(command.value){
            is Commands.CriarHistoricoEscolar -> verifyCriarHistoricoEscolar(tx)
            is Commands.EnviarHistoricoEscolar -> verifyEnviarHistoricoEscolar(tx)
            else -> throw IllegalStateException("Nao reconheco este comando.")
        }

        //Este passo é importante pois, como não temos consenso distribuído igual nas plataformas de blockchain,
        // precisamos ter certeza que todas as partes envolvidas realmente concordaram em realizar a alteração e estão
        // cientes que o dado já não está no mesmo estado, assim garantimos que todas as partes vão sempre estar
        // enxergando a mesma verdade.
        val output = tx.outputsOfType<HistoricoEscolarState>().single()
        command.signers.containsAll((output.participants).map { it.owningKey })

    }

    //FUNCOES VERIFICACAO
    fun verifyCriarHistoricoEscolar(tx: LedgerTransaction){
        requireThat {
            //Regras entrada
            "Tem que ter um e apenas um Output." using (tx.outputsOfType<HistoricoEscolarState>().size==1)
            "Nao pode haver input." using (tx.inputsOfType<HistoricoEscolarState>().isEmpty())

            val outputs = tx.outputsOfType<HistoricoEscolarState>()
            //Regras de criacao de HistoricoEscolar
            "É necessario que tenha um id de aluno" using outputs.all { it.historicoEscolar.idAluno > 0 }
            "A nota nao pode ser negativa" using outputs.none{ it.historicoEscolar.nota < 0 }
        }
    }

    fun verifyEnviarHistoricoEscolar(tx:LedgerTransaction){
        requireThat {
            "Tem que ter um e apenas um input" using (tx.inputsOfType<HistoricoEscolarState>().size==1)
            "Tem que ter um e apenas um output" using (tx.outputsOfType<HistoricoEscolarState>().size==1)

            val input = tx.inputsOfType<HistoricoEscolarState>().single()
            val output = tx.inputsOfType<HistoricoEscolarState>().single()
            //Regras de envio de HistoricoEscolar
            "Nao pode ser removida uma faculdade da lista de faculdades receptoras." using (output.faculdadesReceptoras.containsAll(input.faculdadesReceptoras))
            //"A lista de faculdades receptoras precisa ter mais uma faculdade" using ((output.faculdadesReceptoras.size == input.faculdadesReceptoras.size + 1))
            "O historicoEscolar nao pode ser alterada." using (output.historicoEscolar == input.historicoEscolar)
        }
    }

    //COMANDOS
    //Um “CommandData” é a estrutura que fala para Corda que as classes internas da sua estrutura Commands são comandos
    // deste contrato
    interface Commands: CommandData {
        class CriarHistoricoEscolar: Commands
        class EnviarHistoricoEscolar: Commands
    }
}

