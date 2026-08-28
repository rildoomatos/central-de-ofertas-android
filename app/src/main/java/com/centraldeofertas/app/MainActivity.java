package com.centraldeofertas.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private static final String APP_URL =
            "https://script.google.com/macros/s/AKfycbyPUaZA_LsPSdkjt4DeJRYzt96l5EH3Rn6lIco5RbylyLIc5Vf6knrfhWyAPXL6lNoI/exec";


    private WebView webView;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    // =====================================================
    // INICIAR APP
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );


        webView =
                findViewById(
                        R.id.webView
                );


        configurarWebView();

        configurarVoltar();


        webView.loadUrl(
                APP_URL
        );
    }


    // =====================================================
    // CONFIGURAR WEBVIEW
    // =====================================================

    @SuppressLint({
            "SetJavaScriptEnabled",
            "JavascriptInterface"
    })
    private void configurarWebView() {

        WebSettings settings =
                webView.getSettings();


        settings.setJavaScriptEnabled(
                true
        );

        settings.setDomStorageEnabled(
                true
        );

        settings.setDatabaseEnabled(
                true
        );

        settings.setAllowFileAccess(
                false
        );

        settings.setAllowContentAccess(
                true
        );

        settings.setLoadWithOverviewMode(
                true
        );

        settings.setUseWideViewPort(
                true
        );

        settings.setBuiltInZoomControls(
                false
        );

        settings.setDisplayZoomControls(
                false
        );

        settings.setMediaPlaybackRequiresUserGesture(
                true
        );


        CookieManager cookieManager =
                CookieManager.getInstance();


        cookieManager.setAcceptCookie(
                true
        );


        CookieManager
                .getInstance()
                .setAcceptThirdPartyCookies(
                        webView,
                        true
                );


        // Ponte entre HTML e Android
        webView.addJavascriptInterface(
                new AndroidShareBridge(),
                "AndroidShare"
        );


        webView.setWebChromeClient(
                new WebChromeClient()
        );


        webView.setWebViewClient(

                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {

                        Uri uri =
                                request.getUrl();


                        String host =
                                uri.getHost() == null
                                        ? ""
                                        : uri.getHost();


                        if (
                                host.endsWith(
                                        "script.google.com"
                                )
                                ||
                                host.endsWith(
                                        "googleusercontent.com"
                                )
                                ||
                                host.endsWith(
                                        "accounts.google.com"
                                )
                        ) {

                            return false;
                        }


                        abrirExterno(
                                uri
                        );


                        return true;
                    }


                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {

                        super.onPageFinished(
                                view,
                                url
                        );


                        injetarCompartilhamentoNativo();
                    }

                }
        );
    }


    // =====================================================
    // BOTÃO VOLTAR
    // =====================================================

    private void configurarVoltar() {

        getOnBackPressedDispatcher()
                .addCallback(

                        this,

                        new OnBackPressedCallback(
                                true
                        ) {

                            @Override
                            public void handleOnBackPressed() {

                                if (
                                        webView.canGoBack()
                                ) {

                                    webView.goBack();

                                } else {

                                    finish();
                                }
                            }
                        }
                );
    }


    // =====================================================
    // ABRIR LINKS EXTERNOS
    // =====================================================

    private void abrirExterno(
            Uri uri
    ) {

        try {

            startActivity(

                    new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    )
            );

        } catch (
                ActivityNotFoundException e
        ) {

            Toast.makeText(
                    this,
                    "Não foi possível abrir o link.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =====================================================
    // FUNÇÕES NATIVAS DO APK
    // =====================================================

    private void injetarCompartilhamentoNativo() {

        String js =

                "(function(){" +


                // =========================================
                // PADRONIZAR MÃOS
                // =========================================

                "function mao(t){" +

                "return String(t||'')" +

                ".replace(/👉(?:🏻|🏼|🏽|🏾|🏿)?/g,'👉🏾')" +

                ".replace(/👇(?:🏻|🏼|🏽|🏾|🏿)?/g,'👇🏾')" +

                ".replace(/👆(?:🏻|🏼|🏽|🏾|🏿)?/g,'👆🏾');" +

                "}" +


                // =========================================
                // MOEDA
                // =========================================

                "function moeda(v){" +

                "try{" +

                "return Number(v||0).toLocaleString(" +
                "'pt-BR'," +
                "{style:'currency',currency:'BRL'}" +
                ");" +

                "}catch(e){" +

                "return 'R$ '+Number(v||0).toFixed(2).replace('.',',');" +

                "}" +

                "}" +


                // =========================================
                // NÚMERO
                // =========================================

                "function numero(v,c){" +

                "try{" +

                "return Number(v||0).toLocaleString(" +
                "'pt-BR'," +
                "{minimumFractionDigits:c,maximumFractionDigits:c}" +
                ");" +

                "}catch(e){" +

                "return String(v||0);" +

                "}" +

                "}" +


                // =========================================
                // TEXTO CURTO DO STATUS
                // =========================================

                "function textoStatus(o){" +


                "var nome=String(o.produto||'Oferta Shopee').trim();" +


                // Limita nome do produto
                "if(nome.length>90){" +

                "nome=nome.substring(0,87)+'...';" +

                "}" +


                "var linhas=[];" +


                // Linha 1
                "linhas.push('🔥 OFERTA NA SHOPEE!');" +


                // Linha 2
                "linhas.push('🛍️ '+nome);" +


                // Linha 3
                "if(Number(o.precoAnterior||0)>0){" +

                "linhas.push(" +
                "'❌ De: '+moeda(o.precoAnterior)" +
                ");" +

                "}" +


                // Linha 4
                "linhas.push(" +
                "'✅ Por: '+moeda(o.precoAtual)" +
                ");" +


                // Linha 5
                "if(Number(o.desconto||0)>0){" +

                "linhas.push(" +
                "'🔥 '+Math.round(Number(o.desconto))+'% OFF'" +
                ");" +

                "}" +


                // Linha 6 - avaliação e vendas juntas
                "var detalhes=[];" +


                "if(Number(o.avaliacao||0)>0){" +

                "detalhes.push(" +
                "'⭐ '+numero(o.avaliacao,1)" +
                ");" +

                "}" +


                "if(Number(o.vendas||0)>0){" +

                "detalhes.push(" +

                "'🛒 +'+Number(o.vendas).toLocaleString('pt-BR')+' vendidos'" +

                ");" +

                "}" +


                "if(detalhes.length){" +

                "linhas.push(" +
                "detalhes.join(' • ')" +
                ");" +

                "}" +


                // Linha 7 e 8 - link
                "if(o.linkAfiliado){" +

                "linhas.push('👉🏾 Compre aqui:');" +

                "linhas.push(o.linkAfiliado);" +

                "}" +


                "return mao(" +
                "linhas.join('\\n')" +
                ");" +

                "}" +


                // =========================================
                // BOTÃO STATUS
                // =========================================

                "window.compartilharStatus=function(i){" +

                "try{" +

                "var o=ofertas[i];" +

                "if(!o){" +
                "return;" +
                "}" +


                "var t=textoStatus(o);" +


                /*
                 * IMPORTANTE:
                 * Agora envia SOMENTE texto + link.
                 * Não envia mais a imagem como arquivo.
                 *
                 * O WhatsApp fica responsável por gerar
                 * a prévia clicável da Shopee.
                 */

                "AndroidShare.shareStatusText(t);" +


                "}catch(e){" +

                "console.error(e);" +

                "}" +

                "};" +


                // =========================================
                // BOTÃO WHATSAPP
                // Continua funcionando como antes
                // =========================================

                "var oldWhatsapp=window.whatsapp;" +


                "window.whatsapp=function(i){" +

                "try{" +

                "var o=ofertas[i];" +

                "if(!o){" +
                "return;" +
                "}" +


                "var t=mao(o.legenda||'');" +


                "var u=" +
                "'https://api.whatsapp.com/send/?text='+" +
                "encodeURIComponent(t);" +


                "window.open(" +
                "u," +
                "'_blank'" +
                ");" +


                "}catch(e){" +

                "if(oldWhatsapp){" +
                "oldWhatsapp(i);" +
                "}" +

                "}" +

                "};" +


                "})();";


        webView.evaluateJavascript(
                js,
                null
        );
    }


    // =====================================================
    // PONTE JAVASCRIPT → ANDROID
    // =====================================================

    public class AndroidShareBridge {


        /*
         * NOVO STATUS:
         * somente texto + link.
         */

        @JavascriptInterface
        public void shareStatusText(
                String text
        ) {

            final String texto =
                    padronizarMaos(
                            text
                    );


            runOnUiThread(
                    () ->
                            compartilharSomenteTexto(
                                    texto
                            )
            );
        }


        /*
         * Mantemos esta função por compatibilidade
         * com versões anteriores do Index.html.
         *
         * Mesmo que o HTML tente mandar imagem,
         * o APK agora envia apenas texto + link.
         */

        @JavascriptInterface
        public void shareStatus(
                String imageUrl,
                String text,
                String title
        ) {

            final String texto =
                    padronizarMaos(
                            text
                    );


            runOnUiThread(
                    () ->
                            compartilharSomenteTexto(
                                    texto
                            )
            );
        }
    }


    // =====================================================
    // COMPARTILHAR STATUS
    // =====================================================

    private void compartilharSomenteTexto(
            String texto
    ) {

        Intent intent =
                new Intent(
                        Intent.ACTION_SEND
                );


        intent.setType(
                "text/plain"
        );


        intent.putExtra(
                Intent.EXTRA_TEXT,
                texto
        );


        /*
         * Abre diretamente o WhatsApp.
         *
         * Depois é só escolher:
         *
         * Meu status
         */

        intent.setPackage(
                "com.whatsapp"
        );


        try {

            startActivity(
                    intent
            );

        } catch (
                ActivityNotFoundException e
        ) {


            /*
             * Caso WhatsApp normal
             * não esteja instalado,
             * abre o menu do Android.
             */

            intent.setPackage(
                    null
            );


            startActivity(

                    Intent.createChooser(
                            intent,
                            "Compartilhar oferta"
                    )
            );
        }
    }


    // =====================================================
    // PADRONIZAR EMOJIS DE MÃO
    // =====================================================

    private String padronizarMaos(
            String texto
    ) {

        if (
                texto == null
        ) {

            return "";
        }


        return texto

                .replaceAll(
                        "👉(?:🏻|🏼|🏽|🏾|🏿)?",
                        "👉🏾"
                )

                .replaceAll(
                        "👇(?:🏻|🏼|🏽|🏾|🏿)?",
                        "👇🏾"
                )

                .replaceAll(
                        "👆(?:🏻|🏼|🏽|🏾|🏿)?",
                        "👆🏾"
                );
    }


    // =====================================================
    // FECHAR APP
    // =====================================================

    @Override
    protected void onDestroy() {

        executor.shutdownNow();


        if (
                webView != null
        ) {

            webView.removeJavascriptInterface(
                    "AndroidShare"
            );


            webView.destroy();
        }


        super.onDestroy();
    }
}
