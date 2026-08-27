package com.centraldeofertas.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL =
            "https://script.google.com/macros/s/AKfycbyPUaZA_LsPSdkjt4DeJRYzt96l5EH3Rn6lIco5RbylyLIc5Vf6knrfhWyAPXL6lNoI/exec";

    private WebView webView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        configurarWebView();
        configurarVoltar();
        webView.loadUrl(APP_URL);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void configurarWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new AndroidShareBridge(), "AndroidShare");
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost() == null ? "" : uri.getHost();

                if (host.endsWith("script.google.com") ||
                        host.endsWith("googleusercontent.com") ||
                        host.endsWith("accounts.google.com")) {
                    return false;
                }

                abrirExterno(uri);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injetarCompartilhamentoNativo();
            }
        });
    }

    private void configurarVoltar() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private void abrirExterno(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Não foi possível abrir o link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void injetarCompartilhamentoNativo() {
        String js =
                "(function(){" +
                "function mao(t){return String(t||'')" +
                ".replace(/👉(?:🏻|🏼|🏽|🏾|🏿)?/g,'👉🏾')" +
                ".replace(/👇(?:🏻|🏼|🏽|🏾|🏿)?/g,'👇🏾')" +
                ".replace(/👆(?:🏻|🏼|🏽|🏾|🏿)?/g,'👆🏾');}" +
                "window.compartilharStatus = function(i){" +
                "try{" +
                "var o=ofertas[i]; if(!o){return;}" +
                "var t=o.textoStatus||'';" +
                "if(o.linkAfiliado && t.indexOf(o.linkAfiliado)<0){" +
                "t=t.trim()+'\\n\\n👉🏾 Confira aqui:\\n'+o.linkAfiliado;}" +
                "AndroidShare.shareStatus(o.imagemUrl||'',mao(t),o.produto||'Oferta Shopee');" +
                "}catch(e){console.error(e);}" +
                "};" +
                "var oldWhatsapp=window.whatsapp;" +
                "window.whatsapp=function(i){" +
                "try{" +
                "var o=ofertas[i]; if(!o){return;}" +
                "var t=mao(o.legenda||'');" +
                "var u='https://api.whatsapp.com/send/?text='+encodeURIComponent(t);" +
                "window.open(u,'_blank');" +
                "}catch(e){ if(oldWhatsapp){oldWhatsapp(i);} }" +
                "};" +
                "})();";

        webView.evaluateJavascript(js, null);
    }

    public class AndroidShareBridge {

        @JavascriptInterface
        public void shareStatus(String imageUrl, String text, String title) {
            final String texto = padronizarMaos(text);

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                runOnUiThread(() -> compartilharSomenteTexto(texto));
                return;
            }

            runOnUiThread(() -> Toast.makeText(
                    MainActivity.this,
                    "Preparando imagem para o Status...",
                    Toast.LENGTH_SHORT
            ).show());

            executor.execute(() -> {
                try {
                    Uri imageUri = baixarImagemParaCache(imageUrl);
                    runOnUiThread(() -> compartilharImagemWhatsApp(imageUri, texto));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                MainActivity.this,
                                "Não consegui carregar a imagem. Enviando texto e link.",
                                Toast.LENGTH_LONG
                        ).show();
                        compartilharSomenteTexto(texto);
                    });
                }
            });
        }
    }

    private Uri baixarImagemParaCache(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 Android CentralDeOfertas/1.0");
        connection.connect();

        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            connection.disconnect();
            throw new IllegalStateException("HTTP " + status);
        }

        Bitmap bitmap;
        try (InputStream input = connection.getInputStream()) {
            bitmap = BitmapFactory.decodeStream(input);
        } finally {
            connection.disconnect();
        }

        if (bitmap == null) {
            throw new IllegalStateException("Imagem inválida");
        }

        File dir = new File(getCacheDir(), "shared");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Falha ao criar cache");
        }

        File imageFile = new File(dir, "oferta_status.jpg");
        try (FileOutputStream output = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output);
        }

        return FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                imageFile
        );
    }

    private void compartilharImagemWhatsApp(Uri imageUri, String texto) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, texto);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Abre diretamente o WhatsApp normal. Dentro dele o usuário pode escolher Meu status.
        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Se WhatsApp normal não estiver instalado, abre o seletor do Android.
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "Compartilhar oferta"));
        }
    }

    private void compartilharSomenteTexto(String texto) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, texto);
        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "Compartilhar oferta"));
        }
    }

    private String padronizarMaos(String texto) {
        if (texto == null) return "";

        return texto
                .replaceAll("👉(?:🏻|🏼|🏽|🏾|🏿)?", "👉🏾")
                .replaceAll("👇(?:🏻|🏼|🏽|🏾|🏿)?", "👇🏾")
                .replaceAll("👆(?:🏻|🏼|🏽|🏾|🏿)?", "👆🏾");
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidShare");
            webView.destroy();
        }
        super.onDestroy();
    }
}
