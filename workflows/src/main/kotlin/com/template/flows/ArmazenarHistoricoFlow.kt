package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.HistoricoEscolarContract
import com.template.model.HistoricoEscolar
import com.template.states.HistoricoEscolarState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

//Nao podemos ter mais de um Flow de um mesmo tipo, por isso utilizamos a estrutura “object” que no Kotlin representa
// um singleton, ou seja, uma classe que sempre que for chamada pelo seu nome, sempre retorna a mesma instância de objeto.
object ArmazenarHistoricoFlow {

    @InitiatingFlow
    class ReqFlow(val historicoEscolar: HistoricoEscolar) : FlowLogic<SignedTransaction>(){
        @Suspendable
        override fun call(): SignedTransaction {

            //A faculdade emissora do histórico escolar precisa ser a dona do node que está fazendo a inserção
            //Valida regras especificas do no
            requireThat {
                //No “FlowLogic” temos “ourIdentity” que é a representação do nosso node na rede.
                // Com esta validação, nos certificamos de que ninguém está tentando emitir um histórico
                // na nossa base utilizando o nome de outra faculdade.
                "Eu tenho que ser a faculdade emisora" using (historicoEscolar.faculdade == ourIdentity)
            }

            //Identifica o notary
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            //Criar o nosso State, que será enviado na transação
            val disciplinaState = HistoricoEscolarState(historicoEscolar)

            //Criar o comando, que necessariamente precisa ser o CriarHistoricoEscolar, especificando quais
            // são os participantes que precisam assinar.
            val comando = Command(HistoricoEscolarContract.Commands.CriarHistoricoEscolar(), disciplinaState.participants.map { it.owningKey })
            //Para a seleção dos participantes, estamos informando apenas a chave pública, desta forma,
            // apenas a chave pública é enviada para os nós envolvidos na transação, desta forma,
            // a transação é pseudo-anônima entre os participantes, apenas quem conhece a chave pública da outra
            // entidade vai reconhecer o seu dono.


            //Com estas informações podemos criar a nossa transação.
            val txBuilder = TransactionBuilder(notary)
                .addOutputState(disciplinaState,HistoricoEscolarContract::class.java.canonicalName)
                .addCommand(comando)
            //Na construção da transação, precisamos informar qual o contrato que irá governar o State que estamos criando.
            //Corda permite ter vários contratos diferentes sendo utilizado para trabalhar o mesmo State em situação diferentes

            //Depois de criar a transação, precisamos validar as regras do contrato.
            //Valida todos os contratos envolvidos na transacao
            txBuilder.verify(serviceHub)

            //Uma vez que todas as regras estão válidas, podemos assinar e armazenar os dados com segurança.
            //Assina a transacao
            val transacaoAssinada = serviceHub.signInitialTransaction(txBuilder)

            // Finaliza a transação
            // O FinalityFlow é um flow padrão do Corda que irá verificar se todos que precisavam assinar a transação
            // a assinaram, envia ao Notary na transação validar, e após a conclusão sinaliza para todas as partes que
            // já pode salvar o novo state
            // Como estamos apenas inserindo a informação e não enviando para ninguém,
            // a única assinatura necessária é a nossa própria.
            return subFlow(FinalityFlow(transacaoAssinada))

        }

    }


}