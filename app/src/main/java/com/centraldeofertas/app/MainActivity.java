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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        configurarWebView();
        configurarVoltar();

        webView.loadUrl(APP_URL);
    }


    @SuppressLint({
            "SetJavaScriptEnabled",
            "JavascriptInterface"
    })
    private void configurarWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );


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

                        Uri uri = request.getUrl();

                        String host =
                                uri.getHost() == null
                                        ? ""
                                        : uri.getHost();


                        if (
                                host.endsWith("script.google.com")
                                ||
                                host.endsWith("googleusercontent.com")
                                ||
                                host.endsWith("accounts.google.com")
                        ) {

                            return false;
                        }


                        abrirExterno(uri);

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

                        injetarFuncoesNativas();
                    }
                }
        );
    }


    private void configurarVoltar() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (webView.canGoBack()) {

                                    webView.goBack();

                                } else {

                                    finish();
                                }
                            }
                        }
                );
    }


    private void abrirExterno(Uri uri) {

        try {

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    )
            );

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    this,
                    "Não foi possível abrir o link.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =====================================================
    // FUNÇÕES NATIVAS
    // =====================================================

    private void injetarFuncoesNativas() {

        String js =

                "(function(){" +

                "function mao(t){" +

                "return String(t||'')" +

                ".replace(/👉(?:🏻|🏼|🏽|🏾|🏿)?/g,'👉🏾')" +

                ".replace(/👇(?:🏻|🏼|🏽|🏾|🏿)?/g,'👇🏾')" +

                ".replace(/👆(?:🏻|🏼|🏽|🏾|🏿)?/g,'👆🏾');" +

                "}" +


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

                "return mao(linhas.join('\\n'));" +

                "}" +


                "window.compartilharStatus=function(i){" +

                "try{" +

                "var o=ofertas[i];" +

                "if(!o){return;}" +

                "AndroidShare.shareStatusText(" +
                "textoStatus(o)" +
                ");" +

                "}catch(e){" +

                "console.error(e);" +

                "}" +

                "};" +


                "var oldWhatsapp=window.whatsapp;" +

                "window.whatsapp=function(i){" +

                "try{" +

                "var o=ofertas[i];" +

                "if(!o){return;}" +

                "var t=mao(o.legenda||'');" +

                "var u=" +
                "'https://api.whatsapp.com/send/?text='+" +
                "encodeURIComponent(t);" +

                "window.open(u,'_blank');" +

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


    public class AndroidShareBridge {

        @JavascriptInterface
        public void shareStatusText(
                String text
        ) {

            final String texto =
                    padronizarMaos(text);


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
                    padronizarMaos(text);


            runOnUiThread(
                    () ->
                            compartilharSomenteTexto(
                                    texto
                            )
            );
        }
    }


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

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            intent.setPackage(null);

            startActivity(
                    Intent.createChooser(
                            intent,
                            "Compartilhar oferta"
                    )
            );
        }
    }


    private String padronizarMaos(
            String texto
    ) {

        if (texto == null) {
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


    @Override
    protected void onDestroy() {

        if (webView != null) {

            webView.removeJavascriptInterface(
                    "AndroidShare"
            );

            webView.destroy();
        }

        super.onDestroy();
    }
}
