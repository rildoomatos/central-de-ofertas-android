# Central de Ofertas — Android

Projeto Android para transformar a interface atual do Apps Script em um APK.

## Web App configurado

O APK abre diretamente:

https://script.google.com/macros/s/AKfycbyPUaZA_LsPSdkjt4DeJRYzt96l5EH3Rn6lIco5RbylyLIc5Vf6knrfhWyAPXL6lNoI/exec

## O que já está implementado

- Interface atual da Central de Ofertas dentro do APK.
- JavaScript, cookies e armazenamento local habilitados.
- Links de produto abrem fora do aplicativo.
- Botão STATUS é substituído no APK por compartilhamento Android nativo.
- O APK baixa a imagem do produto, anexa a imagem e envia texto + link ao WhatsApp.
- O WhatsApp abre na tela "Enviar para...", onde é possível selecionar "Meu status".
- Emojis de mão usados pelo APK são padronizados para o tom 👉🏾 / 👇🏾 / 👆🏾.
- Botão WHATSAPP continua usando a legenda existente, com os emojis de mão padronizados no APK.

## Importante sobre login Google

A implantação atual do Apps Script parece exigir login Google. WebViews Android podem ter limitações para login Google incorporado. Se o Google impedir o login dentro do APK, NÃO exponha simplesmente o Web App para qualquer pessoa. O próximo ajuste recomendado é adicionar uma autenticação própria do aplicativo no Apps Script antes de mudar a implantação para acesso público.

## Gerar APK pelo GitHub Actions

O arquivo `.github/workflows/build-apk.yml` gera o APK manualmente.

Depois de colocar estes arquivos no repositório:

1. Abra GitHub > Actions.
2. Escolha `Gerar APK Android`.
3. Clique `Run workflow`.
4. Quando concluir, abra a execução.
5. Em `Artifacts`, baixe `Central-de-Ofertas-APK`.
6. Dentro do ZIP estará `app-debug.apk`.

O APK Debug é suficiente para teste no seu aparelho. Para distribuição futura, deve ser criada uma assinatura Release própria.
