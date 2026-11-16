# Débitos técnicos

## Página de analytics responde 200 para código inexistente

- **Onde:** `frontend/src/app/analytics/[code]/page.tsx`
- **Problema:** com `cacheComponents` (default no Next 16), os dados da página carregam dentro de um `<Suspense>` e o HTML começa a ser enviado em streaming. Quando `notFound()` é chamado, o status 200 já foi enviado: o usuário vê a página "not found", mas o HTTP é 200 (soft 404).
- **Impacto:** baixo para o usuário; crawlers e monitoramento enxergam 200.
- **Correção sugerida:** buscar os stats fora do `<Suspense>` (ou desativar `cacheComponents` nessa rota) para que `notFound()` rode antes do streaming e devolva 404.

## Tema visual do frontend incompleto

- **Onde:** `frontend/src/app/globals.css` / `layout.tsx`
- **Problema:** a página renderiza com fonte serifada padrão do browser e cores neutras no gráfico; o tema do shadcn/ui (fontes e `--chart-*`) não está sendo aplicado por completo.
- **Impacto:** apenas estético; dados e funcionalidade corretos.
- **Correção sugerida:** revisar o import do tema no `globals.css` e a fonte no `layout.tsx`; definir cor do `ChartConfig` com `var(--chart-1)`.
