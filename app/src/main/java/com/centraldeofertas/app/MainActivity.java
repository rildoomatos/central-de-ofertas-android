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


public class MainActivity extends AppCompatActivity {

    private static final String APP_URL =
            "https://script.google.com/macros/s/AKfycbyPUaZA_LsPSdkjt4DeJRYzt96l5EH3Rn6lIco5RbylyLIc5Vf6knrfhWyAPXL6lNoI/exec";

    private WebView webView;


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


        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(
                true
        );

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );


        // Ponte HTML -> Android
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


                        // Mantém as funções do APK
                        injetarCompartilhamentoNativo();


                        // Remove visualmente o aviso do Apps Script
                        ocultarAvisoAppsScript();


                        /*
                         * O Google pode inserir a faixa
                         * alguns milissegundos depois.
                         *
                         * Por isso fazemos novas tentativas.
                         */

                        webView.postDelayed(
                                () -> ocultarAvisoAppsScript(),
                                500
                        );

                        webView.postDelayed(
                                () -> ocultarAvisoAppsScript(),
                                1200
                        );

                        webView.postDelayed(
                                () -> ocultarAvisoAppsScript(),
                                2500
                        );

                        webView.postDelayed(
                                () -> ocultarAvisoAppsScript(),
                                4500
                        );
                    }
                }
        );
    }


    // =====================================================
    // OCULTAR AVISO DO GOOGLE APPS SCRIPT
    // =====================================================

    private void ocultarAvisoAppsScript() {

        String js =

                "(function(){" +

                "try{" +

                "var frase=" +
                "'Este aplicativo foi criado por um usuário do Google Apps Script';" +


                /*
                 * Procura o bloco do aviso.
                 */

                "var elementos=" +
                "document.querySelectorAll('body *');" +


                "for(var i=0;i<elementos.length;i++){" +

                "var el=elementos[i];" +

                "var txt=" +
                "(el.innerText||'')" +
                ".replace(/\\s+/g,' ')" +
                ".trim();" +


                "if(" +
                "txt.indexOf(frase)!==-1" +
                "&&" +
                "txt.indexOf('Denunciar abuso')!==-1" +
                "&&" +
                "txt.indexOf('Saiba mais')!==-1" +
                "){" +


                "var alvo=el;" +


                /*
                 * Tenta chegar ao container completo
                 * sem esconder o restante do aplicativo.
                 */

                "while(" +
                "alvo.parentElement" +
                "&&" +
                "alvo.parentElement!==document.body" +
                "){" +

                "var pai=alvo.parentElement;" +

                "var textoPai=" +
                "(pai.innerText||'')" +
                ".replace(/\\s+/g,' ')" +
                ".trim();" +

                "var altura=" +
                "pai.getBoundingClientRect().height;" +


                "if(" +
                "textoPai.indexOf(frase)!==-1" +
                "&&" +
                "textoPai.indexOf('Denunciar abuso')!==-1" +
                "&&" +
                "textoPai.indexOf('Saiba mais')!==-1" +
                "&&" +
                "altura>0" +
                "&&" +
                "altura<280" +
                "){" +

                "alvo=pai;" +

                "}else{" +

                "break;" +

                "}" +

                "}" +


                /*
                 * Esconde completamente a faixa.
                 */

                "alvo.style.setProperty(" +
                "'display'," +
                "'none'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'height'," +
                "'0px'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'min-height'," +
                "'0px'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'max-height'," +
                "'0px'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'margin'," +
                "'0px'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'padding'," +
                "'0px'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'border'," +
                "'0px'," +
                "'important'" +
                ");" +

                "alvo.style.setProperty(" +
                "'overflow'," +
                "'hidden'," +
                "'important'" +
                ");" +


                "break;" +

                "}" +

                "}" +


                /*
                 * Segunda tentativa:
                 * remove individualmente os links
                 * caso o Google altere um pouco
                 * a estrutura do aviso.
                 */

                "var links=" +
                "document.querySelectorAll('a');" +


                "for(var j=0;j<links.length;j++){" +

                "var t=" +
                "(links[j].innerText||'').trim();" +


                "if(" +
                "t==='Denunciar abuso'" +
                "||" +
                "t==='Saiba mais'" +
                "){" +

                "var bloco=links[j].parentElement;" +


                "if(bloco){" +

                "var bt=" +
                "(bloco.innerText||'')" +
                ".replace(/\\s+/g,' ');" +


                "if(" +
                "bt.indexOf('Denunciar abuso')!==-1" +
                "||" +
                "bt.indexOf('Saiba mais')!==-1" +
                "){" +

                "bloco.style.setProperty(" +
                "'display'," +
                "'none'," +
                "'important'" +
                ");" +

                "}" +

                "}" +

                "}" +

                "}" +


                "}catch(e){" +

                "console.log(" +
                "'Aviso Apps Script não localizado'," +
                "e" +
                ");" +

                "}" +

                "})();";


        webView.evaluateJavascript(
                js,
                null
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
    // INJETAR FUNÇÕES DO APK
    // =====================================================

    private void injetarCompartilhamentoNativo() {

        String js =

                "(function(){" +


                // =========================================
                // PADRONIZAR EMOJIS DE MÃO
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
                // TEXTO DO STATUS
                // =========================================

                "function textoStatus(o){" +


                "var nome=String(o.produto||'Oferta Shopee').trim();" +


                "if(nome.length>90){" +

                "nome=nome.substring(0,87)+'...';" +

                "}" +


                "var linhas=[];" +


                "linhas.push('🔥 OFERTA NA SHOPEE!');" +

                "linhas.push('🛍️ '+nome);" +


                "if(Number(o.precoAnterior||0)>0){" +

                "linhas.push(" +
                "'❌ De: '+moeda(o.precoAnterior)" +
                ");" +

                "}" +


                "linhas.push(" +
                "'✅ Por: '+moeda(o.precoAtual)" +
                ");" +


                "if(Number(o.desconto||0)>0){" +

                "linhas.push(" +
                "'🔥 '+Math.round(Number(o.desconto))+'% OFF'" +
                ");" +

                "}" +


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


                "AndroidShare.shareStatusText(t);" +


                "}catch(e){" +

                "console.error(e);" +

                "}" +

                "};" +


                // =========================================
                // BOTÃO WHATSAPP
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
    // PONTE JAVASCRIPT -> ANDROID
    // =====================================================

    public class AndroidShareBridge {


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
