# language: pt
Funcionalidade: Fluxo de Execução de Serviços
  Como mecânico da oficina
  Quero gerenciar a fila e execução das OS aprovadas
  Para garantir que os serviços sejam executados corretamente

  Cenário: Enfileirar OS ao receber evento ORCAMENTO_APROVADO
    Dado que não existe OS na fila com id "OS-ID-001"
    Quando o evento "ORCAMENTO_APROVADO" for recebido para a OS "OS-20250101-TEST01"
    Então a OS deve estar na fila com status "NA_FILA"

  Cenário: Idempotência — não enfileirar OS duplicada
    Dado que a OS "OS-ID-002" já está na fila
    Quando o evento "ORCAMENTO_APROVADO" for recebido novamente para a OS "OS-20250101-TEST02"
    Então a fila deve conter apenas uma entrada para a OS

  Cenário: Iniciar execução de OS na fila
    Dado que a OS "OS-ID-003" está na fila com status "NA_FILA"
    Quando o mecânico "João Mecânico" iniciar a execução da OS "OS-ID-003"
    Então a OS deve ter status de execução "EM_EXECUCAO"
    E o evento "EXECUCAO_INICIADA" deve ser publicado

  Cenário: Finalizar execução de OS em andamento
    Dado que a OS "OS-ID-004" está em execução com status "EM_EXECUCAO"
    Quando a execução da OS "OS-ID-004" for finalizada
    Então a OS deve ter status de execução "FINALIZADA"
    E o evento "EXECUCAO_FINALIZADA" deve ser publicado

  Cenário: Cancelar execução ao receber OS_CANCELADA
    Dado que a OS "OS-ID-005" está na fila com status "NA_FILA"
    Quando o evento "OS_CANCELADA" for recebido para a OS "OS-ID-005"
    Então a OS deve ter status de execução "CANCELADA"

  Cenário: Não deve iniciar execução de OS já em execução
    Dado que a OS "OS-ID-006" está em execução com status "EM_EXECUCAO"
    Quando o mecânico "Maria Mecânica" tentar iniciar a execução da OS "OS-ID-006"
    Então deve retornar erro de negócio "Apenas itens na fila"

  Cenário: Não deve finalizar execução de OS na fila
    Dado que a OS "OS-ID-007" está na fila com status "NA_FILA"
    Quando tentar finalizar a execução da OS "OS-ID-007"
    Então deve retornar erro de negócio "Apenas execuções em andamento"
