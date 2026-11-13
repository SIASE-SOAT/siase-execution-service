# language: pt
Funcionalidade: Saga de execução de serviços
  Como execution-service, participo do Saga coreografado:
  recebo ORCAMENTO_APROVADO para enfileirar a OS, publico os eventos de
  execução para o os-service e compenso quando a OS é cancelada.

  Cenario: OS aprovada entra na fila e completa a execução
    Dado que o orçamento da OS foi aprovado
    Então a OS entra na fila de execução
    Quando o mecânico inicia a execução
    Então o evento EXECUCAO_INICIADA é publicado para o os-service
    E o mecânico finaliza a execução
    Então o evento EXECUCAO_FINALIZADA é publicado para o os-service

  Cenario: Cancelamento da OS compensa a execução
    Dado que o orçamento da OS foi aprovado
    E a OS entra na fila de execução
    Quando a OS é cancelada pelo cliente
    Então a execução é cancelada e o evento EXECUCAO_CANCELADA é publicado
